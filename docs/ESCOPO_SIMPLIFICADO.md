# Escopo simplificado do sistema

## Fluxo principal

1. Usuário autenticado seleciona empresa e setor autorizado.
2. Usuário envia `docs/templates/PRODUCAO_PADRAO.xlsx` ou outro XLSX no mesmo padrão.
3. Backend valida arquivo, cabeçalho, linhas, referências e duplicidade.
4. Importação aprovada grava registros de produção com origem e linha de origem.
5. Analytics e relatório simplificado agregam os mesmos registros.

## Visões core

- Visão geral: `/api/v1/empresas/{empresaId}/analytics/dashboard`.
- Visão do setor: `/api/v1/empresas/{empresaId}/analytics/dashboard/setor/{setorId}`.
- Agrupamentos: `/analytics/setores`, `/analytics/funcionarios` e `/analytics/pecas`.
- Funcionário: `/analytics/funcionarios/{funcionarioId}`.
- Importação: `/setores/{setorId}/importacoes/producao/validar` e `/setores/{setorId}/importacoes/producao`.
- Relatório: `/relatorios/simplificado`.

## Filtros

Todas as visões core exigem `inicio` e `fim`. Quando aplicável, aceitam `setorId`, `funcionarioId` e `pecaId`. Máquinas e turnos continuam disponíveis nas APIs existentes, mas não são necessários para o XLSX oficial.

## Fora do caminho crítico

Paradas, máquinas, turnos, metas, desempenho, qualidade, financeiro, auditoria e IA/RAG permanecem no sistema, porém não podem impedir uma importação válida baseada nas seis colunas oficiais.
