# Proposal

## Why

O frontend atual tem uma casca Premium Dark e serviços de API, mas ainda concentra no menu módulos secundários e usa rotas de placeholder para a maior parte da operação. A especificação `MASTER_FRONTEND_PREMIUM_DARK_EXCEL_DASHBOARD.md` redefine o frontend em torno do fluxo homologado Excel por setor → Dashboard, com dados reais do Backend V1, drill-downs contextuais e uma navegação simples.

## What Changes

- Reorganizar a navegação principal para apenas Visão Geral, Setores, Importar Excel e Relatórios.
- Substituir o dashboard atual baseado em `analytics/resumo` por uma visão core que consuma dashboard geral, setores, evolução e totais homologados.
- Implementar rotas e telas reais para dashboard geral, setor, funcionário, peça, importação e relatório simplificado.
- Integrar a seleção dinâmica de setores, upload/validação/importação XLSX, resultado detalhado e prevenção de clique duplicado/reimportação.
- Preservar autenticação, logout, guards, interceptor, tratamento de 401/403 e correlation ID.
- Consolidar tokens Premium Dark e componentes compartilhados, com estados loading, empty, error e forbidden.
- Garantir responsividade, acessibilidade, pt-BR e preservação de códigos como `00147`.
- Criar testes de contrato, componentes, permissões e E2E com a massa homologada: 350 total, 00147=200, 00152=150, PEC-001=270, PEC-002=80 e 185 minutos.
- Atualizar documentação de arquitetura, integração core, Design System e homologação.
- **BREAKING**: retirar módulos secundários do fluxo e da sidebar principal; o backend e seus endpoints não serão removidos.

## Capabilities

### New Capabilities

- `frontend-premium-dark-excel-dashboard`: shell, navegação, dashboard geral/setor, drill-downs, importação Excel, relatório simplificado, estados de UI, segurança de sessão, responsividade, acessibilidade e homologação do fluxo core.

### Modified Capabilities

Nenhuma capability permanente foi encontrada por `openspec list --specs`. A mudança será registrada como uma nova capability, aproveitando o código existente e os contratos já homologados pelo backend.

## Impact

- Frontend Angular em `frontend/src/app`, incluindo `app.ts`, `app.html`, `app.scss`, rotas, serviços, modelos, guards e testes.
- API consumida: `/auth/login`, `/auth/me`, setores, `/analytics/dashboard`, `/analytics/dashboard/setor/{id}`, analytics de funcionário/peça, importação de produção e `/relatorios/simplificado`.
- Design tokens, componentes compartilhados, lazy loading e estados de tela.
- Documentação em `frontend/README.md` e `docs/`.
- Backend não será alterado nesta mudança; qualquer divergência de contrato será registrada e tratada sem inventar endpoint ou DTO.
