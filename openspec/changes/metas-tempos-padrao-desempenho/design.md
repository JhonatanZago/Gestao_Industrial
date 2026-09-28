# Design

## Context

O domínio já possui `RegistroProducao`, `ParadaProducao` e serviços de analytics que calculam quantidade, duração bruta, paradas e tempo produtivo. A nova capacidade precisa preservar esses resultados, acrescentando referências históricas de tempo padrão e metas sem acoplar entidades JPA aos contratos REST. Ver `proposal.md` para a motivação e as especificações desta change para o comportamento observável.

## Goals / Non-Goals

**Goals:**

- Persistir padrões e metas com histórico, vigência, isolamento por empresa e ciclo de vida lógico.
- Resolver padrões por registro sem N+1 e calcular métricas derivadas com precisão previsível.
- Reutilizar filtros, períodos e dimensões dos analytics atuais.
- Expor cobertura e ausência de denominadores de forma explícita.

**Non-Goals:**

- Reescrever os analytics existentes ou alterar a semântica dos indicadores base.
- Criar motor de OEE, qualidade, manutenção, faturamento, frontend ou ranking de desempenho.

## Decisions

### Modelo de domínio e persistência

Criar as entidades `TempoPadraoProducao` e `MetaProducao`, com referências às entidades existentes e campos de auditoria, vigência e `ativo`. Criar enums para `BaseTempoPadrao`, `TipoMeta` e `PeriodicidadeMeta`. Usar duas migrações Flyway novas, uma por tabela, sem editar migrações anteriores; incluir chaves estrangeiras, índices por empresa/contexto/vigência e índices adequados às consultas analíticas.

A alternativa de guardar padrões e metas em JSON foi descartada porque impediria validações relacionais, filtros indexados e integridade de histórico. A alternativa de alterar `RegistroProducao` com uma cópia do padrão foi descartada porque a resolução precisa considerar a data histórica e não duplicar configuração mutável no registro.

### Vigência, conflitos e ciclo de vida

Validar sobreposição no serviço transacional antes de persistir ou atualizar, considerando a mesma empresa, peça, setor, máquina e intervalo inclusivo; máquina nula representa o padrão geral e não conflita com uma máquina específica. O banco receberá índices para busca, enquanto a regra completa ficará no domínio/aplicação para tratar intervalos abertos e nulos.

Ativação e desativação serão endpoints explícitos. A desativação não removerá linhas nem referências, mas padrões e metas inativos não serão candidatos a novas resoluções. Consultas históricas poderão solicitá-los por identificador ou contexto histórico conforme o contrato do endpoint.

### Resolução do tempo padrão

Implementar um serviço de resolução que receba empresa, peça, setor, máquina e data de `RegistroProducao.inicio`. Buscar em lote os candidatos do período analítico e construir um mapa por registro; para cada registro, escolher primeiro o padrão ativo específico da máquina e depois o padrão geral do setor. Isso evita uma consulta por registro e mantém a precedência determinística.

Para base `POR_UNIDADE`, o valor em segundos será diretamente o denominador. Para `POR_LOTE`, converter o tempo do lote pela quantidade de referência persistida. O resultado da resolução carregará também a origem do fallback e a vigência usada, permitindo auditoria do cálculo.

### Cálculo e integração com analytics

Estender o serviço de analytics com uma etapa derivada após a obtenção dos registros e paradas existentes. Cada registro será enriquecido com tempo produtivo, padrão resolvido, esperado e estado de cobertura; só depois os valores serão agregados por funcionário, setor, máquina ou peça. Assim, um funcionário que fabrica peças diferentes não terá seu total dividido por um único padrão.

Usar `BigDecimal` com escala 2 e arredondamento `HALF_UP` apenas na saída; não arredondar parcelas intermediárias. Desempenho será `real / esperado * 100`, atingimento será `real / meta * 100`, valores acima de 100 serão válidos e denominadores nulos ou zero produzirão indicador nulo/não calculável. A quantidade real e os indicadores base continuam disponíveis mesmo quando não houver padrão.

Adicionar metadados de cobertura (`registrosTotais`, `registrosComPadrao`, `registrosSemPadrao`, `quantidadeComPadrao`, percentual) aos DTOs analíticos derivados. As consultas por período usarão os mesmos filtros e granularidades atuais; a data de início do registro determina o padrão, enquanto a meta aplicável será a do contexto e periodicidade do período agregado, sem rateio automático de metas mensais em períodos parciais.

### Metas e contratos REST

Criar repositórios e serviços separados para tempos e metas, com DTOs de entrada/saída e mapeamento explícito, sem expor entidades. Os endpoints CRUD e de ciclo de vida ficarão em `/api/v1/empresas/{empresaId}/tempos-padrao` e `/metas-producao`; filtros de vigência, ativo e contexto serão paginados.

Criar recursos de analytics em `/analytics/desempenho`, `/analytics/desempenho/funcionarios`, `/setores`, `/maquinas`, `/pecas` e `/analytics/metas`. Os comparativos retornarão métricas, cobertura e identificadores, sem gerar rótulos de melhor/pior. A seleção de meta será feita por tipo/contexto e periodicidade; conflitos impedem que exista mais de uma candidata equivalente no mesmo intervalo.

### Testes e desempenho

Cobrir validação de campos, isolamento por empresa, conflitos de intervalos, fallback específico/geral, troca histórica de padrões, desativação, todos os tipos de meta, periodicidades e fórmulas de 100%, 110%, ausência de denominador e cobertura parcial. Incluir testes de integração dos endpoints e de regressão dos analytics existentes.

As consultas de candidatos serão em lote por empresa, período, peças, setores e máquinas, com projeções quando adequado. O serviço não fará lazy loading dentro de loops nem consulta individual por registro; índices serão verificados com os filtros reais.

## Risks / Trade-offs

- **[Risco]** Regras de intervalo com fim nulo podem permitir conflitos difíceis de detectar apenas com índice SQL. **Mitigação:** validação transacional explícita e testes para intervalos abertos, consecutivos e sobrepostos.
- **[Risco]** Grande volume de registros pode tornar a resolução em memória custosa. **Mitigação:** carregamento em lote limitado ao período/filtros, projeções e agregação após uma única resolução por registro.
- **[Risco]** Resultados parciais podem ser interpretados como desempenho completo. **Mitigação:** tornar cobertura obrigatória nos DTOs derivados e manter registros sem padrão explicitamente contados.
- **[Risco]** Metas com periodicidades diferentes podem ser confundidas em telas consumidoras. **Mitigação:** retornar periodicidade, vigência e contexto da meta aplicada em cada resultado.

## Migration Plan

1. Aplicar as novas migrações Flyway para criar tabelas, relações e índices; não há backfill obrigatório porque padrões e metas não existiam.
2. Publicar domínio, validações e endpoints de cadastro.
3. Publicar resolução em lote e analytics derivados mantendo endpoints atuais compatíveis.
4. Executar testes de regressão e validação OpenSpec antes do release.

Em caso de rollback da aplicação, as tabelas novas podem permanecer sem efeito. O rollback de banco deverá remover somente as tabelas desta change em uma migração de compensação aprovada; nunca editar ou reverter migrações históricas já aplicadas.

## Open Questions

Nenhuma questão em aberto altera o contrato ou a abordagem definidos nesta change.
