# Design

## Context

O projeto possui `RegistroProducao` com quantidade, peça, setor, máquina, turno, origem e início/fim, além de analytics que carregam produção e paradas em lote. A TASK 08 adicionou padrões de tempo e metas, mas não existe ainda uma referência monetária. Ver `proposal.md` e as três especificações desta change para os contratos comportamentais.

## Goals / Non-Goals

**Goals:**

- Persistir valores monetários versionados e semanticamente identificados.
- Resolver valor histórico por registro com precedência de máquina e fallback de setor.
- Calcular e agregar valor sem misturar moedas ou conceitos de valor.
- Expor cobertura financeira e taxas por hora sem ocultar ausência de referência.
- Reutilizar filtros, períodos e dados manuais/importados existentes sem N+1.

**Non-Goals:**

- Modelar pedido, nota fiscal, faturamento real, custos, margem, lucro, câmbio ou contabilidade.
- Calcular perda financeira por parada ou transformar valor industrial em remuneração de funcionário.

## Decisions

### Modelo e precisão

Criar `ValorProducao` com empresa, peça, setor, máquina opcional, `TipoValor`, `valorUnitario` em `BigDecimal`, `moeda` com três caracteres, vigência, ativo e auditoria. Criar `TipoValor` com `VALOR_PROCESSAMENTO`, `VALOR_PRODUCAO` e `RECEITA_ESTIMADA`. Persistir valor unitário em `DECIMAL(19,6)` para suportar referências como 0.0085; arredondar somente nos DTOs de apresentação.

A moeda será armazenada como código ISO em texto normalizado em maiúsculas. Não será criada tabela de câmbio nem conversão implícita. A alternativa de usar uma moeda global fixa foi descartada porque impediria USD/EUR e mudanças futuras da moeda padrão da empresa.

### Vigência e validação

O serviço validará valor positivo, moeda ISO de três letras, datas válidas, pertencimento à empresa e compatibilidade entre máquina e setor. A sobreposição ativa será rejeitada para empresa+peça+setor+máquina+tipo+moeda, com fim nulo tratado como intervalo aberto. Índices cobrirão empresa, contexto, moeda, tipo e vigência; a regra completa continuará no serviço para permitir intervalos abertos.

Ativação e desativação serão endpoints lógicos. Um valor inativo não será candidato a novas resoluções, mas permanecerá consultável e não será removido fisicamente.

### Resolução histórica em lote

Criar um `ValorProducaoResolver` que carregue uma vez todos os valores ativos candidatos ao intervalo analítico, filtrando empresa, moeda e tipo quando informados. Em memória, indexar por peça/setor e ordenar por especificidade de máquina e início de vigência; para cada produção, usar `inicio.toLocalDate()` como data de referência, selecionar máquina específica e então fallback sem máquina.

O resultado por registro terá valor unitário, moeda, tipo e estado de cobertura. A consulta em lote será análoga à resolução dos tempos padrão da TASK 08, sem buscar um valor individualmente dentro do loop de produção.

### Semântica financeira

O cálculo por registro será `quantidade * valorUnitario`. A agregação será feita por moeda e tipo; quando o filtro de moeda for omitido, a resposta conterá grupos separados por moeda, nunca uma soma heterogênea. Para evitar misturar valor processado com receita, o filtro `tipoValor` será opcional com padrão `VALOR_PRODUCAO`; respostas sempre carregarão o tipo efetivamente calculado.

`valorPorHoraBruta` usará valor dividido por duração bruta em horas e `valorPorHoraProdutiva` usará tempo produtivo em horas. Denominador zero retorna nulo. `RECEITA_ESTIMADA` aparecerá em campo/linha própria e sempre conservará a semântica de estimativa; nenhum DTO usará o termo faturamento para os dados desta change.

### Analytics e contratos

Criar DTOs específicos para cadastro, resumo, agregações por dimensão, série temporal e comparativos. Os endpoints financeiros compartilharão filtros de período, setor, funcionário, peça, máquina, turno, moeda e tipo. Resumos serão listas agrupadas por moeda/tipo quando não houver filtro; agregações serão páginas com moeda/tipo por linha.

Para série temporal, agrupar por DIA/SEMANA/MES e moeda/tipo. Para comparativos, calcular valor atual, anterior, variação absoluta e percentual por cada moeda/tipo; percentual com base anterior zero será nulo. Quantidade e valor serão campos independentes para evidenciar que mais unidades não significam necessariamente mais valor.

### Migração e integração

Criar uma única migração Flyway nova após as migrations existentes, sem editar arquivos antigos, com FKs para empresa, peça, setor e máquina, checks de valor/moeda/datas, auditoria e índices. Produções importadas e manuais usam exatamente a mesma resolução porque o algoritmo considera apenas o registro produtivo e sua origem não altera a fórmula.

## Risks / Trade-offs

- **[Risco]** Usuários podem interpretar valor industrial como receita real. **Mitigação:** tipos semânticos obrigatórios, nomes explícitos, documentação e ausência deliberada de termos fiscais.
- **[Risco]** Respostas com várias moedas podem ser somadas por consumidores desatentos. **Mitigação:** moeda obrigatória em cada linha, agrupamento separado e filtro disponível.
- **[Risco]** Muitos valores históricos podem aumentar memória na resolução em lote. **Mitigação:** restringir candidatos ao período, empresa, tipo e moeda e usar índices de vigência/contexto.
- **[Risco]** Valores unitários com muitas casas podem divergir por arredondamento de apresentação. **Mitigação:** manter escala intermediária alta e arredondar apenas ao serializar a resposta.
- **[Risco]** Uma peça em vários processos pode aparentar receita final duplicada. **Mitigação:** apresentar tipo `VALOR_PROCESSAMENTO`/`VALOR_PRODUCAO` e documentar que a soma representa valor processado, não receita final automaticamente.

## Migration Plan

1. Aplicar a nova migration para criar `valor_producao`, constraints, FKs e índices.
2. Publicar entidade, validações, CRUD e testes de isolamento/vigência.
3. Publicar resolver em lote e analytics financeiros, preservando endpoints físicos anteriores.
4. Executar `clean test`, `build`, validação OpenSpec e revisão de respostas por moeda/tipo.

Rollback de aplicação não exige remover dados. Se o banco precisar ser revertido, usar migration de compensação aprovada para remover somente a tabela desta change; nunca editar migrations históricas já aplicadas.

## Open Questions

Nenhuma questão aberta altera o contrato: ausência de filtro de moeda sempre resulta em grupos separados, e ausência de `tipoValor` usa `VALOR_PRODUCAO`.
