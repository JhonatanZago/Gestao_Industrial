# Design

## Context

O domínio atual persiste `RegistroProducao` com `Empresa`, `Setor`, `Funcionario`, `Peca`, `Maquina` e `Turno`, além de `ParadaProducao` e `MotivoParada`. Produção e parada usam `LocalDateTime` no fuso operacional da planta, e a regra existente considera registros/paradas ativos para operações correntes. As consultas de produção já usam `JpaSpecificationExecutor`, mas as respostas de listagem calculam paradas depois da paginação; isso não é suficiente para agregações analíticas de grande volume.

As specs de `indicadores-producao`, `agregacoes-producao` e `comparativos-periodo` definem o contrato externo. Este design detalha como consultar os dados sem criar uma tabela persistente de KPI e sem modificar a semântica dos endpoints operacionais.

## Goals / Non-Goals

**Goals:**

- Centralizar regras de período, filtros, fórmulas, escala e arredondamento em um serviço analítico.
- Executar agregações e soma de paradas em consultas em lote, com projections e sem N+1 evidente.
- Reaproveitar o mesmo núcleo de métricas para resumo, dimensões, série temporal e comparativos.
- Manter rastreabilidade para produções manuais e importadas sem filtrar pela origem.
- Permitir evolução para cache ou snapshots sem acoplar a API a uma tabela de dashboard.

**Non-Goals:**

- Não criar frontend, gráficos, exportação, metas, tempo padrão, custos, faturamento, OEE ou ranking semântico.
- Não alterar os endpoints de CRUD de produção/paradas nem reativar registros automaticamente.
- Não fazer rateio proporcional de quantidade para registros que atravessam o limite do período.

## Decisions

### 1. Serviço analítico compartilhado e DTOs próprios

Criar um `AnalyticsService` responsável por validar Empresa/período/filtros, executar consultas e montar DTOs específicos. Resources apenas convertem parâmetros HTTP e delegam ao serviço. DTOs analíticos não reutilizam entidades JPA nem os DTOs operacionais, evitando respostas monolíticas e lazy loading fora da transação.

Alternativa descartada: colocar somas e fórmulas nos Controllers ou reutilizar `RegistroProducaoService.list`; isso duplicaria regras e faria a agregação depender de paginação operacional.

### 2. Regra única de inclusão temporal

Os limites `inicio` e `fim` serão inclusivos. Um registro entra quando `registro.inicio >= inicio` e `registro.inicio <= fim`; quando entra, sua duração completa (`fim - inicio`) e suas paradas ativas completas são consideradas. Registros que apenas atravessam o início do filtro ficam fora, sem truncar duração ou quantidade. Os comparativos usarão exatamente a mesma regra em ambos os períodos.

O parâmetro é `LocalDateTime` no fuso operacional já adotado pelo sistema (`America/Sao_Paulo`). Não haverá conversão implícita de timezone na primeira versão.

### 3. Núcleo de métricas com precisão decimal

Uma estrutura interna de métricas manterá quantidade como `BigDecimal`, duração em segundos ou minutos decimais sem arredondamento intermediário e contagens como `long`. A resposta exporá durações em minutos decimais, taxas em peças/hora e percentuais; valores apresentados terão escala 2 com `RoundingMode.HALF_UP`. As fórmulas serão aplicadas uma única vez no fechamento do agregado:

- `taxaBruta = quantidade / (duracaoBrutaMinutos / 60)`;
- `taxaProdutiva = quantidade / (tempoProdutivoMinutos / 60)`;
- `percentualParado = tempoParadoMinutos / duracaoBrutaMinutos * 100`;
- qualquer denominador zero produz `null`.

`tempoProdutivo` será limitado por zero após a subtração. A API manterá nomes que expressem taxa operacional; não serão usados termos como eficiência ou OEE.

### 4. Consultas agregadas em duas camadas, sem join multiplicador

O repositório analítico terá queries/projections agrupadas por Empresa, período e dimensão. A produção será agregada em uma consulta que retorna quantidade, contagem e duração. As paradas ativas serão agregadas em consulta separada por `registro_producao_id` ou pela mesma dimensão e combinadas em memória por chave do agregado. Isso evita multiplicar a duração da produção quando houver várias paradas e continua sendo um número fixo de consultas por endpoint, não uma consulta por registro.

As expressões de duração e bucket temporal serão isoladas em uma camada de query compatível com PostgreSQL e H2 de testes. Se a revisão de planos demonstrar que uma expressão precisa de dialeto específico, ela ficará restrita ao adapter de consulta, sem espalhar SQL pelo serviço.

Alternativa descartada: buscar todos os registros e suas paradas para fazer o `group by` em Java; isso viola o limite de memória e o requisito de evitar N+1.

### 5. Filtros e isolamento de Empresa

Toda query começa por `empresa_id`. Antes da execução, IDs opcionais serão resolvidos/validados dentro da Empresa; um ID existente em outra Empresa produzirá erro de contexto, e não um resultado vazio ambíguo. Filtros de dimensão serão aplicados à produção antes do agrupamento. Dimensões desativadas não serão filtradas nos joins históricos, mas produção e parada inativas serão filtradas por padrão.

Máquina e Turno nulos formarão grupos técnicos `SEM_MAQUINA` e `SEM_TURNO`, apenas na resposta; nenhuma entidade será criada.

### 6. API analítica

Os resources ficarão sob `/api/v1/empresas/{empresaId}/analytics`:

- `/resumo` para métricas gerais e filtros;
- `/setores`, `/funcionarios`, `/pecas`, `/maquinas` e `/turnos` para listas paginadas;
- `/funcionarios/{funcionarioId}` para detalhe agregado;
- `/paradas/motivos` para motivos ativos agrupados;
- `/serie-temporal` com enum `DIA`, `SEMANA`, `MES`;
- `/comparativo` com os quatro limites explícitos.

Todos os endpoints principais exigirão período e terão ordenação padrão determinística. Listas usarão `Pageable` com tamanho máximo configurado para impedir respostas gigantescas.

### 7. Índices orientados por plano

Antes de criar migration, revisar os índices existentes: produção já possui índices por Empresa/início, Setor/início, Funcionário/período e Peça/início; parada possui índices por produção/período e Empresa/início. Serão executados testes/EXPLAIN com filtros combinados. Somente se houver ganho claro será criada uma migration V12 para índices adicionais, preferencialmente envolvendo `empresa_id`, `ativo`, `inicio` e a dimensão mais usada. Migrations V1–V11 não serão editadas.

### 8. Comparativos e séries

O comparativo chamará o mesmo caso de uso de métricas duas vezes, com filtros idênticos, e então calculará diferença absoluta e percentual. A resposta incluirá os períodos efetivamente usados. Séries sem produção omitirão baldes vazios, evitando a criação de intervalos gigantes; a ordem será cronológica.

### 9. Observabilidade e falhas

O serviço registrará somente duração da consulta, Empresa, filtros resumidos, endpoint e quantidade de resultados; não serão registrados payloads nem listas de códigos. Período inválido, granularidade desconhecida e dimensão fora da Empresa usarão o `GlobalExceptionHandler` existente com mensagens seguras.

## Risks / Trade-offs

- **[Risco]** Expressões de duração/bucket variam entre PostgreSQL e H2. → **Mitigação:** encapsular as expressões e cobrir os cenários matemáticos em integração; manter SQL específico somente no adapter.
- **[Risco]** Agregar paradas separadamente pode exigir uma segunda consulta. → **Mitigação:** aceitar duas consultas em lote para evitar duplicidade de soma e N+1; medir planos antes de otimizar.
- **[Risco]** A regra baseada no início exclui registros que atravessam o limite. → **Mitigação:** documentar no contrato, retornar períodos explícitos e testar limites e travessias; não inventar rateio de quantidade.
- **[Risco]** Dimensões desativadas podem parecer inconsistentes em telas de cadastro atuais. → **Mitigação:** manter o snapshot relacional e separar claramente o filtro de status atual da inclusão histórica.
- **[Risco]** Agregações grandes podem exigir ordenação custosa. → **Mitigação:** paginação, limite máximo, ordenação determinística e revisão de índices baseada em plano.

## Migration Plan

1. Implementar DTOs, parâmetros, núcleo de fórmulas e queries analíticas sem alterar tabelas.
2. Adicionar endpoints e testes determinísticos usando o schema atual e a suíte de regressão.
3. Avaliar planos em volume sintético; criar V12 somente se os índices atuais forem insuficientes.
4. Publicar a API com período obrigatório e limites de página documentados.
5. Em rollback, remover/desativar apenas resources e serviços analíticos; manter qualquer índice adicional compatível e sem alterar dados operacionais.

## Open Questions

Nenhuma questão bloqueante. A escala, o arredondamento, a regra temporal, o tratamento de lacunas e os grupos nulos estão definidos neste design e refletidos nas specs.
