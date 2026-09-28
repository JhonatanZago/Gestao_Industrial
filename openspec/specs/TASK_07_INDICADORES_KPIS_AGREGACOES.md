# TASK EXECUTÁVEL 07 --- Indicadores, KPIs e Agregações de Produção

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--06.

Antes de alterar código:

1.  leia o projeto completo;
2.  leia todas as specs e changes OpenSpec;
3.  execute todos os testes existentes;
4.  confirme que o build está estável;
5.  analise o modelo real criado nas tasks anteriores;
6.  preserve contratos e regras existentes;
7.  crie a nova change OpenSpec;
8.  implemente;
9.  teste resultados numéricos com cenários determinísticos;
10. corrija qualquer divergência;
11. execute regressão completa.

**Não implementar frontend.** **Não implementar gráficos.** **Não
implementar faturamento ainda.** **Não iniciar a próxima task
automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Transformar os registros operacionais já existentes em informações
consolidadas para consumo futuro por Dashboard, relatórios e IA.

Até aqui o sistema possui dados como:

``` text
Empresa
Setor
Funcionário
Peça
Máquina
Turno
Registro de Produção
Paradas
Importações Excel
```

Agora deverá conseguir responder, no backend:

``` text
Quantas peças foram produzidas?
Quanto tempo foi utilizado?
Quanto tempo ficou parado?
Quanto tempo foi efetivamente produtivo?
Qual foi a taxa de produção?
Qual setor produziu mais?
Qual funcionário produziu mais?
Qual peça foi mais processada?
Qual máquina teve maior produção?
Qual turno teve maior produção?
Como um período se compara ao anterior?
```

------------------------------------------------------------------------

# 2. OpenSpec

Criar change:

``` text
indicadores-kpis-producao
```

Criar artefatos conforme a versão OpenSpec instalada.

Capacidades sugeridas:

``` text
indicadores-producao
agregacoes-producao
comparativos-periodo
```

O design deve registrar claramente:

-   fórmulas;
-   unidade de cada indicador;
-   regras de inclusão/exclusão;
-   filtros;
-   tratamento de dados desativados;
-   períodos;
-   timezone;
-   arredondamento;
-   performance.

------------------------------------------------------------------------

# 3. Princípio fundamental

Esta task cria a **camada analítica do backend**, não um Dashboard
visual.

Fluxo:

``` text
DADOS OPERACIONAIS
        ↓
CONSULTAS / AGREGAÇÕES
        ↓
SERVIÇO ANALÍTICO
        ↓
DTOs DE INDICADORES
        ↓
API ANALÍTICA
        ↓
[futuramente]
Dashboard / Relatório / RAG
```

Não colocar cálculos analíticos no Controller.

------------------------------------------------------------------------

# 4. Fonte de verdade

Os indicadores devem utilizar os registros reais persistidos.

Não criar valores simulados, mocks em produção ou números hardcoded.

Por padrão, considerar apenas registros de produção ativos e paradas
ativas.

Registros históricos vinculados a funcionário, peça, setor, máquina ou
turno posteriormente inativados não devem desaparecer das análises
históricas.

------------------------------------------------------------------------

# 5. Filtro obrigatório por Empresa

Toda consulta analítica MUST possuir contexto de Empresa.

Nenhum indicador poderá misturar dados de empresas diferentes.

Base sugerida:

``` http
/api/v1/empresas/{empresaId}/analytics
```

------------------------------------------------------------------------

# 6. Período

Os endpoints analíticos devem aceitar período explícito:

``` text
inicio
fim
```

Exemplo:

``` http
?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:59
```

Validar:

``` text
fim >= inicio
```

Definir no design se os limites são inclusivos/exclusivos e utilizar a
mesma regra em todas as consultas.

------------------------------------------------------------------------

# 7. Sem período

Não executar consulta histórica ilimitada silenciosamente.

Definir comportamento seguro:

-   exigir período nos endpoints de analytics; ou
-   utilizar período padrão claramente documentado e limitado.

Preferência: período explícito para análises principais.

------------------------------------------------------------------------

# 8. Indicadores principais

Implementar cálculo de pelo menos:

``` text
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxaProducaoBruta
taxaProducaoProdutiva
percentualTempoParado
```

Não chamar essas métricas de eficiência/OEE sem possuir os dados
necessários.

------------------------------------------------------------------------

# 9. Quantidade produzida

Fórmula:

``` text
quantidadeProduzida
=
SUM(registroProducao.quantidade)
```

Considerar apenas registros válidos conforme as regras analíticas.

------------------------------------------------------------------------

# 10. Quantidade de registros

Fórmula:

``` text
quantidadeRegistros
=
COUNT(registros de produção)
```

Isso representa apontamentos, não peças.

Não confundir:

``` text
350 peças
```

com:

``` text
1 registro
```

------------------------------------------------------------------------

# 11. Duração bruta

Para cada registro:

``` text
duracaoBruta = fim - inicio
```

Agregado:

``` text
duracaoBrutaTotal
=
SUM(duração bruta dos registros)
```

Expressar internamente de maneira precisa e retornar unidade explícita,
preferencialmente minutos e/ou segundos conforme padrão escolhido.

------------------------------------------------------------------------

# 12. Tempo parado

Fórmula:

``` text
tempoParado
=
SUM(duração das paradas ativas)
```

Não contar parada desativada.

Evitar dupla contagem.

As regras anteriores já impedem sobreposição de paradas da mesma
produção; preservar essa garantia.

------------------------------------------------------------------------

# 13. Tempo produtivo

Fórmula:

``` text
tempoProdutivo
=
duracaoBruta
-
tempoParado
```

Garantir:

``` text
tempoProdutivo >= 0
```

------------------------------------------------------------------------

# 14. Taxa de produção bruta

Fórmula:

``` text
taxaProducaoBruta
=
quantidadeProduzida
/
(duracaoBrutaMinutos / 60)
```

Unidade:

``` text
peças/hora bruta
```

Se duração for zero, não dividir por zero.

Definir retorno como `null`, zero ou estado indisponível de forma
explícita. Preferência: `null` quando matematicamente não calculável.

------------------------------------------------------------------------

# 15. Taxa de produção produtiva

Fórmula:

``` text
taxaProducaoProdutiva
=
quantidadeProduzida
/
(tempoProdutivoMinutos / 60)
```

Unidade:

``` text
peças/hora produtiva
```

Não chamar automaticamente de "desempenho".

------------------------------------------------------------------------

# 16. Percentual de tempo parado

Fórmula:

``` text
percentualTempoParado
=
tempoParado
/
duracaoBruta
* 100
```

Tratar divisão por zero.

------------------------------------------------------------------------

# 17. Precisão e arredondamento

Não utilizar `double` indiscriminadamente para indicadores apresentados.

Utilizar estratégia consistente com `BigDecimal` quando necessário.

Definir escala e arredondamento.

Exemplo:

``` text
238.64 peças/hora
14.58 %
```

Não arredondar valores intermediários cedo demais.

------------------------------------------------------------------------

# 18. Agregação por Setor

Criar endpoint/serviço capaz de retornar produção agrupada por setor.

Para cada setor:

``` text
setorId
codigo
nome
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxaProducaoBruta
taxaProducaoProdutiva
percentualTempoParado
```

------------------------------------------------------------------------

# 19. Agregação por Funcionário

Permitir análise por funcionário.

Retornar:

``` text
funcionarioId
codigo
nome
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxas
```

Isso permitirá futuramente que o proprietário clique no funcionário e
veja seus dados.

NÃO implementar ranking de desempenho definitivo nesta task.

------------------------------------------------------------------------

# 20. Detalhamento do funcionário

Criar endpoint analítico para um funcionário específico.

Exemplo:

``` http
GET /api/v1/empresas/{empresaId}/analytics/funcionarios/{funcionarioId}
```

Com período.

Retornar resumo e dados suficientes para responder:

``` text
quanto produziu
quais peças processou
em quais setores trabalhou
quanto tempo bruto
quanto tempo parado
quanto tempo produtivo
taxas de produção
```

Não retornar milhares de registros brutos dentro do resumo. Usar
endpoints paginados existentes para detalhe operacional quando
necessário.

------------------------------------------------------------------------

# 21. Agregação por Peça

Para cada peça:

``` text
pecaId
codigo
descricao
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxas
```

------------------------------------------------------------------------

# 22. Agregação por Máquina

Quando houver máquina:

``` text
maquinaId
codigo
nome
setor
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxas
```

Registros sem máquina devem ser tratados explicitamente.

Não inventar máquina "Desconhecida" persistida.

------------------------------------------------------------------------

# 23. Agregação por Turno

Quando houver turno:

``` text
turnoId
codigo
nome
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxas
```

Registros sem turno devem seguir estratégia documentada.

------------------------------------------------------------------------

# 24. Agregação por Motivo de Parada

Criar análise de tempo parado agrupado por motivo.

Retornar:

``` text
motivoId
codigo
nome
categoria
quantidadeOcorrencias
tempoParadoMinutos
percentualDoTempoParado
```

Isso permitirá identificar principais causas de perda.

------------------------------------------------------------------------

# 25. Série temporal

Criar endpoint capaz de agregar produção ao longo do tempo.

Granularidades mínimas:

``` text
DIA
SEMANA
MES
```

Utilizar enum técnico estável para granularidade.

Exemplo de retorno diário:

``` json
[
  {
    "periodo": "2026-09-25",
    "quantidadeProduzida": 1250,
    "tempoProdutivoMinutos": 420
  },
  {
    "periodo": "2026-09-26",
    "quantidadeProduzida": 1480,
    "tempoProdutivoMinutos": 455
  }
]
```

------------------------------------------------------------------------

# 26. Não preencher lacunas silenciosamente

Se não houver produção em determinado dia, definir se a API:

-   omite o período; ou
-   retorna zero.

Registrar decisão.

Para consumo futuro de gráficos, preferir resposta previsível, mas não
criar intervalos gigantes desnecessariamente.

------------------------------------------------------------------------

# 27. Comparativo entre períodos

Implementar comparação entre:

``` text
período atual
vs
período comparativo
```

Exemplo:

``` text
01/09 → 30/09
vs
01/08 → 31/08
```

Retornar para indicadores relevantes:

``` text
valorAtual
valorAnterior
variacaoAbsoluta
variacaoPercentual
```

------------------------------------------------------------------------

# 28. Variação percentual

Fórmula:

``` text
((atual - anterior) / anterior) * 100
```

Se anterior = 0, não dividir por zero.

Retornar estado não calculável de maneira explícita.

------------------------------------------------------------------------

# 29. Comparativo automático opcional

Pode existir helper para período anterior equivalente.

Exemplo:

``` text
últimos 7 dias
vs
7 dias anteriores
```

Mas não criar comportamento obscuro.

O endpoint deve deixar claro quais períodos foram efetivamente
utilizados.

------------------------------------------------------------------------

# 30. Filtros combináveis

Os analytics devem permitir filtros opcionais quando fizer sentido:

``` text
setorId
funcionarioId
pecaId
maquinaId
turnoId
```

Todos devem ser validados contra a empresa.

Não permitir ID de outra empresa retornar simplesmente dados misturados.

------------------------------------------------------------------------

# 31. Sem N+1

As agregações devem acontecer preferencialmente no banco.

Não fazer:

``` text
buscar 100.000 produções
↓
loop Java
↓
buscar paradas uma por uma
```

Utilizar queries agregadas, projections, specifications/query builder ou
abordagem apropriada à arquitetura atual.

------------------------------------------------------------------------

# 32. Não criar tabela de KPI desnecessariamente

Nesta primeira versão, indicadores podem ser calculados sob demanda.

Não criar tabela persistente de "dashboard" sem necessidade.

Se performance futura exigir snapshots/materialização, isso será change
específica.

------------------------------------------------------------------------

# 33. Índices

Revisar os índices existentes considerando:

``` text
empresa + inicio
empresa + setor + inicio
empresa + funcionario + inicio
empresa + peca + inicio
empresa + maquina + inicio
empresa + turno + inicio
parada + motivo
```

Criar migration somente para índices realmente necessários.

Não alterar migration antiga aplicada.

------------------------------------------------------------------------

# 34. API --- Resumo geral

Implementar endpoint conceitual:

``` http
GET /api/v1/empresas/{empresaId}/analytics/resumo
```

Parâmetros:

``` text
inicio
fim
setorId?
funcionarioId?
pecaId?
maquinaId?
turnoId?
```

------------------------------------------------------------------------

# 35. API --- Setores

``` http
GET /api/v1/empresas/{empresaId}/analytics/setores
```

Com período e filtros aplicáveis.

------------------------------------------------------------------------

# 36. API --- Funcionários

``` http
GET /api/v1/empresas/{empresaId}/analytics/funcionarios
```

E:

``` http
GET /api/v1/empresas/{empresaId}/analytics/funcionarios/{funcionarioId}
```

------------------------------------------------------------------------

# 37. API --- Peças

``` http
GET /api/v1/empresas/{empresaId}/analytics/pecas
```

------------------------------------------------------------------------

# 38. API --- Máquinas

``` http
GET /api/v1/empresas/{empresaId}/analytics/maquinas
```

------------------------------------------------------------------------

# 39. API --- Turnos

``` http
GET /api/v1/empresas/{empresaId}/analytics/turnos
```

------------------------------------------------------------------------

# 40. API --- Paradas

``` http
GET /api/v1/empresas/{empresaId}/analytics/paradas/motivos
```

------------------------------------------------------------------------

# 41. API --- Série temporal

``` http
GET /api/v1/empresas/{empresaId}/analytics/serie-temporal
```

Parâmetros:

``` text
inicio
fim
granularidade
filtros opcionais
```

------------------------------------------------------------------------

# 42. API --- Comparativo

``` http
GET /api/v1/empresas/{empresaId}/analytics/comparativo
```

Parâmetros conceituais:

``` text
inicioAtual
fimAtual
inicioAnterior
fimAnterior
```

------------------------------------------------------------------------

# 43. DTOs analíticos

Criar DTOs específicos.

Não reutilizar entidades JPA.

Exemplos:

``` text
ResumoProducaoResponse
IndicadorResponse
ProducaoPorSetorResponse
ProducaoPorFuncionarioResponse
ProducaoPorPecaResponse
ProducaoPorMaquinaResponse
ProducaoPorTurnoResponse
ParadaPorMotivoResponse
SerieTemporalResponse
ComparativoResponse
```

Evitar DTO monolítico gigantesco.

------------------------------------------------------------------------

# 44. Cenário matemático obrigatório

Criar dados determinísticos:

## Produção A

``` text
Funcionário: 00147
Setor: PINT
Peça: 54871
Quantidade: 300
08:00 → 10:00
Duração bruta: 120 min
Parada: 20 min
Tempo produtivo: 100 min
```

## Produção B

``` text
Funcionário: 00147
Setor: PINT
Peça: 54871
Quantidade: 200
10:00 → 12:00
Duração bruta: 120 min
Parada: 10 min
Tempo produtivo: 110 min
```

Esperado:

``` text
Quantidade = 500
Registros = 2
Duração bruta = 240 min
Tempo parado = 30 min
Tempo produtivo = 210 min
```

Taxa bruta:

``` text
500 / 4h = 125 peças/h
```

Taxa produtiva:

``` text
500 / 3.5h
≈ 142.86 peças/h
```

Percentual parado:

``` text
30 / 240 * 100
= 12.50%
```

Criar teste exato para esses resultados.

------------------------------------------------------------------------

# 45. Testes por Setor

Criar pelo menos dois setores com produções distintas.

Validar que:

-   cada setor recebe somente seus dados;
-   total geral corresponde à soma;
-   empresa diferente não interfere.

------------------------------------------------------------------------

# 46. Testes por Funcionário

Criar dois funcionários.

Validar:

-   quantidades;
-   tempos;
-   peças;
-   setores;
-   filtros;
-   detalhamento individual.

------------------------------------------------------------------------

# 47. Testes por Peça

Criar múltiplas peças.

Validar agregação correta mesmo quando a mesma peça passa por setores
diferentes.

------------------------------------------------------------------------

# 48. Testes por Máquina e Turno

Criar:

``` text
Máquina A
Máquina B
T1
T2
```

Validar agrupamentos e tratamento de registros sem máquina/turno.

------------------------------------------------------------------------

# 49. Testes de Paradas

Criar motivos:

``` text
MAN-001
MAT-001
```

Distribuir paradas e validar:

``` text
ocorrências
tempo total
percentual
```

------------------------------------------------------------------------

# 50. Testes de período

Validar:

-   produção antes do período;
-   dentro;
-   depois;
-   exatamente nos limites;
-   registros atravessando limites.

Definir no design como registros parcialmente sobrepostos ao período são
tratados.

IMPORTANTE: não truncar duração silenciosamente sem especificação.

Escolher regra clara e testar.

------------------------------------------------------------------------

# 51. Registros atravessando o período

Exemplo:

``` text
Filtro:
08:00 → 10:00

Produção:
07:30 → 08:30
```

Decidir explicitamente se:

A)  o registro inteiro é incluído por início;
B)  é incluído por qualquer interseção;
C)  somente registros totalmente contidos entram;
D)  duração/quantidade são proporcionalmente truncadas.

Não inventar proporcionalidade de quantidade sem base.

Recomendação inicial: utilizar critério consistente baseado no início do
registro para agregações operacionais, documentando a regra.

------------------------------------------------------------------------

# 52. Testes de comparativo

Criar:

``` text
Período A: 500 peças
Período B: 400 peças
```

Esperado:

``` text
diferença = +100
variação = +25%
```

Testar também:

``` text
anterior = 0
```

------------------------------------------------------------------------

# 53. Testes de dados desativados

Criar produção válida.

Depois desativar:

-   funcionário;
-   peça;
-   máquina;
-   setor.

O histórico da produção deve continuar aparecendo no período histórico.

Depois desativar o próprio Registro de Produção.

Ele não deve entrar no indicador padrão.

------------------------------------------------------------------------

# 54. Testes de importação

Produções criadas via TASK 06 devem gerar exatamente os mesmos
indicadores que produções criadas manualmente.

A origem não pode alterar a matemática.

------------------------------------------------------------------------

# 55. Performance

Criar teste ou avaliação controlada com volume razoável de dados
sintéticos.

Verificar:

-   número de queries;
-   ausência de N+1 evidente;
-   consultas agregadas;
-   paginação onde aplicável.

Não exigir benchmark complexo nesta task.

------------------------------------------------------------------------

# 56. Paginação e ordenação

Listas analíticas potencialmente grandes, como
funcionários/peças/máquinas, devem possuir paginação ou limite
controlado.

Permitir ordenação por métricas quando tecnicamente apropriado, por
exemplo:

``` text
quantidadeProduzida
tempoParado
tempoProdutivo
```

Não criar ranking semântico de "melhor funcionário" ainda.

------------------------------------------------------------------------

# 57. Sem julgamento indevido de funcionário

Os números devem ser apresentados como indicadores operacionais.

Não classificar automaticamente:

``` text
bom
ruim
ineficiente
```

A produtividade depende de peça, processo, máquina, complexidade, meta e
condições.

A próxima task poderá implementar desempenho com contexto adequado.

------------------------------------------------------------------------

# 58. Cache

Não adicionar cache distribuído nesta task sem necessidade.

Projetar Services de forma que cache possa ser adicionado
posteriormente.

------------------------------------------------------------------------

# 59. Tratamento de erros

Reutilizar `@RestControllerAdvice`.

Exemplos:

``` text
Período inválido.
O setor informado não pertence à empresa.
O funcionário informado não pertence à empresa.
A máquina informada não pertence à empresa.
Granularidade inválida.
```

------------------------------------------------------------------------

# 60. Observabilidade

Adicionar logs técnicos úteis para consultas analíticas lentas, sem
poluir logs.

Não logar grandes payloads.

Se o projeto já possuir padrão de métricas/logs, reutilizá-lo.

------------------------------------------------------------------------

# 61. Fora do escopo

NÃO implementar:

-   frontend;
-   Angular;
-   gráficos;
-   Dashboard visual;
-   faturamento;
-   preço da peça;
-   custo;
-   margem;
-   meta de produção;
-   tempo padrão da peça;
-   OEE completo;
-   ranking definitivo;
-   classificação de funcionário;
-   relatório PDF/Excel;
-   autenticação/autorização;
-   IA;
-   RAG.

------------------------------------------------------------------------

# 62. Regressão

Todos os testes das TASKS 01--06 devem continuar passando.

Executar antes e depois.

------------------------------------------------------------------------

# 63. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar validação OpenSpec oficial se disponível.

------------------------------------------------------------------------

# 64. Não mascarar falhas

É proibido:

-   hardcode de indicadores;
-   usar valores fictícios;
-   ignorar divisão por zero;
-   esconder registros para fazer teste passar;
-   apagar/desabilitar testes;
-   carregar toda a base em memória sem necessidade;
-   misturar empresas;
-   considerar build quebrado como concluído;
-   alterar specs silenciosamente para justificar bug.

------------------------------------------------------------------------

# 65. Critérios de aceite

A task somente estará concluída quando:

1.  resumo geral funcionar;
2.  quantidade produzida estiver correta;
3.  quantidade de registros estiver correta;
4.  duração bruta estiver correta;
5.  tempo parado estiver correto;
6.  tempo produtivo estiver correto;
7.  taxa bruta estiver correta;
8.  taxa produtiva estiver correta;
9.  percentual parado estiver correto;
10. divisão por zero estiver tratada;
11. agregação por setor funcionar;
12. agregação por funcionário funcionar;
13. detalhe do funcionário funcionar;
14. agregação por peça funcionar;
15. agregação por máquina funcionar;
16. agregação por turno funcionar;
17. paradas por motivo funcionarem;
18. série temporal funcionar;
19. comparativo funcionar;
20. filtros respeitarem empresa;
21. registros históricos continuarem analisáveis;
22. produções desativadas forem excluídas por padrão;
23. produção importada e manual tiverem mesma matemática;
24. queries evitarem N+1 evidente;
25. testes matemáticos passarem;
26. testes anteriores passarem;
27. `clean test` passar;
28. `build` passar;
29. OpenSpec refletir a implementação.

------------------------------------------------------------------------

# 66. Entrega esperada do Codex

Ao concluir, apresentar:

## OpenSpec

-   change;
-   proposal;
-   design;
-   tasks;
-   specs;
-   validação.

## Fórmulas

Documentar exatamente: - quantidade; - duração bruta; - tempo parado; -
tempo produtivo; - taxa bruta; - taxa produtiva; - percentual parado; -
comparativos.

## APIs

Listar endpoints e filtros.

## Banco

Informar novas migrations/índices, se houver.

## Performance

Explicar estratégia das queries e prevenção de N+1.

## Testes

Informar: - cenários matemáticos; - agregações; - filtros; -
comparativos; - regressão; - falhas e correções.

## Build

Informar resultado de:

``` text
clean test
build
```

## Pendências

Somente pendências reais.

Não iniciar a próxima task.

------------------------------------------------------------------------

# 67. Próxima etapa prevista

Após aprovação:

``` text
TASK 08 — Metas, Tempo Padrão, Desempenho e Comparativos Industriais
```

Ela permitirá começar a contextualizar a produção:

``` text
Produção real
      +
Tempo produtivo
      +
Peça
      +
Setor
      +
Meta
      +
Tempo padrão
      ↓
DESEMPENHO CONTEXTUALIZADO
```

Depois serão implementados valorização/faturamento,
segurança/permissões, relatórios gerenciais, APIs finais de Dashboard,
auditoria/qualidade e preparação para IA/RAG.

**O frontend continuará bloqueado até o backend estar completo, testado
e estabilizado.**
