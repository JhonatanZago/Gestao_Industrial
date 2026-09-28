# Proposal

## Why

Os registros de produção e paradas já persistem a operação, mas ainda exigem consultas manuais para responder quanto foi produzido, quanto tempo foi parado e quais dimensões concentraram a produção. Esta change cria uma camada analítica consistente e reutilizável por dashboards, relatórios e IA futura, sem introduzir frontend, julgamento de desempenho ou métricas de OEE.

## What Changes

- Criar um serviço analítico com período obrigatório, contexto de Empresa e filtros combináveis por Setor, Funcionário, Peça, Máquina e Turno.
- Expor resumo geral com quantidade, apontamentos, duração bruta, tempo parado, tempo produtivo, taxas e percentual de parada.
- Expor agregações paginadas por Setor, Funcionário, Peça, Máquina, Turno e Motivo de Parada, além de detalhamento analítico de Funcionário.
- Expor série temporal diária, semanal e mensal, com granularidade técnica estável e regra documentada para períodos sem produção.
- Expor comparativo entre dois períodos, incluindo valores atual/anterior, diferença absoluta e variação percentual com tratamento explícito de denominador zero.
- Considerar apenas produções e paradas ativas por padrão, preservando referências históricas mesmo quando cadastros relacionados forem posteriormente desativados.
- Calcular sob demanda por consultas agregadas, projections ou query builder, evitando carregar toda a base e evitando N+1.
- Revisar índices e criar migration somente quando justificar as consultas analíticas.
- Adicionar testes determinísticos das fórmulas, filtros, isolamento por Empresa, desativações, importação/manual e regressão das TASKS 01–06.
- Manter fora do escopo frontend, gráficos, faturamento, metas, tempo padrão, ranking definitivo, OEE completo, relatórios exportáveis, autenticação e IA.

## Capabilities

### New Capabilities

- `indicadores-producao`: resumo geral e métricas operacionais calculadas a partir de produções e paradas persistidas.
- `agregacoes-producao`: agrupamentos, detalhamento de Funcionário, série temporal e paginação/ordenação analítica.
- `comparativos-periodo`: comparação explícita entre períodos e variações com divisão por zero documentada.

### Modified Capabilities

Nenhuma. As APIs existentes de cadastro, produção, paradas e importação permanecem com o comportamento atual; a nova camada apenas consulta seus dados.

## Impact

- Novos DTOs, repositories/queries agregadas, serviços e resources em `src/main/java`.
- Possíveis índices e migration Flyway posterior a V11, sem alterar migrations já aplicadas.
- Novos endpoints sob `/api/v1/empresas/{empresaId}/analytics`.
- Novos testes de integração/MockMvc e cenários matemáticos determinísticos em H2.
- Nenhuma dependência externa obrigatória prevista; a implementação deve reutilizar Spring Data JPA, BigDecimal e o modelo atual de Empresa, Setor, RegistroProducao, ParadaProducao, MotivoParada, Máquina e Turno.
