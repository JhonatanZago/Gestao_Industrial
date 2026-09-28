# Proposal: Metas, tempos padrão e desempenho industrial

## Why

O sistema já registra produção, paradas e indicadores agregados, mas ainda não possui uma referência versionada de tempo esperado nem metas de produção contextualizadas. Sem esses conceitos, não é possível comparar o realizado com o esperado de forma histórica, explicar a cobertura dos cálculos ou preservar a validade das análises quando padrões e recursos mudam.

## What Changes

- Adicionar tempos padrão de produção versionados por empresa, peça, setor e máquina opcional, com vigência, base de cálculo por unidade ou lote e resolução com fallback.
- Adicionar metas de produção com tipo de contexto, periodicidade, vigência, ciclo de vida lógico e validação de conflitos.
- Calcular quantidade esperada, desempenho, atingimento de metas e cobertura dos registros que possuem padrão aplicável.
- Disponibilizar CRUD, ativação/desativação e consultas analíticas para desempenho geral e comparativos por funcionário, setor, máquina e peça.
- Reaproveitar os filtros, períodos, agregações e tempos produtivos dos analytics existentes, mantendo os resultados históricos baseados na data de início da produção.
- Criar migrações, índices, DTOs, serviços, repositórios e testes de backend necessários.

## Capabilities

### New Capabilities

- `tempos-padrao`: cadastro, vigência, resolução, fallback e histórico dos tempos padrão.
- `metas-producao`: cadastro, aplicabilidade, periodicidade, conflitos e ciclo de vida das metas.
- `desempenho-industrial`: cálculos de esperado, desempenho, atingimento e cobertura sobre a produção registrada.
- `comparativos-desempenho`: consultas comparativas por dimensões industriais sem impor ranking subjetivo.

### Modified Capabilities

Nenhuma especificação principal existente será modificada. A mudança integra-se aos analytics já implementados e acrescenta métricas derivadas sem alterar o significado dos indicadores atuais.

## Impact

- **Persistência:** novas tabelas `tempo_padrao_producao` e `meta_producao`, enums, índices e migrações Flyway; migrações existentes não serão editadas.
- **Domínio e aplicação:** novas entidades, repositórios, serviços de validação/resolução, DTOs e recursos REST sob `/api/v1/empresas/{empresaId}`.
- **Analytics:** novos endpoints de desempenho e campos derivados, incluindo cobertura parcial quando alguns registros não tiverem padrão aplicável.
- **Testes:** cobertura de CRUD, conflitos, vigência, fallback, isolamento por empresa, fórmulas, ausência de denominador e comparativos.
- **Fora de escopo:** frontend, faturamento, OEE completo, qualidade, manutenção, ranking automático de pessoas/equipes e alteração física de registros históricos.
