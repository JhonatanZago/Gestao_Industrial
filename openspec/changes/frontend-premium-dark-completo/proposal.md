# Proposal

## Why

O Backend V1 está homologado, mas o repositório ainda não possui uma aplicação frontend para consumir seus contratos de autenticação, produção, analytics, relatórios, qualidade e IA/RAG. Esta change cria a primeira experiência operacional do ERP, com uma base Angular organizada, visual Premium Dark e integração guiada exclusivamente pelos endpoints e permissões reais do backend.

## What Changes

- Criar uma aplicação frontend Angular standalone, com configuração de ambientes, proxy de desenvolvimento, build e testes reproduzíveis.
- Criar um shell responsivo com login, logout, sidebar, topbar, guards, interceptors, tratamento de erros e exibição segura de correlation ID.
- Criar um Design System Premium Dark com tokens centralizados e componentes reutilizáveis para cards, tabelas, filtros, formulários, diálogos, toasts, estados vazios/erro e gráficos.
- Implementar progressivamente as áreas autorizadas: dashboard executivo, produção, setores, funcionários, peças, máquinas, importações Excel, paradas, metas, desempenho, qualidade, relatórios, auditoria, assistente IA/RAG e configurações suportadas.
- Integrar paginação, filtros, downloads e uploads conforme os contratos existentes, sem hardcode de dados produtivos, regras de negócio, permissões ou endpoints.
- Implementar responsividade, acessibilidade básica, loading/empty/error states, proteção de dados financeiros e menu/rotas condicionados às permissões reais.
- Criar testes unitários, de integração, de permissões e E2E do fluxo principal, além de documentação de arquitetura, Design System, integração API e homologação.

## Capabilities

### New Capabilities

- `frontend-application-shell`: aplicação Angular, configuração, autenticação, autorização, layout, navegação e tratamento transversal de requisições/erros.
- `frontend-design-system`: tokens visuais Premium Dark e componentes UI reutilizáveis, acessíveis e responsivos.
- `frontend-operational-modules`: telas e fluxos de dashboard, produção, cadastros industriais, importações, paradas, metas, desempenho, qualidade e drill-downs.
- `frontend-reports-and-audit`: consumo de relatórios/exportações e visualização de auditoria conforme permissões reais.
- `frontend-ai-assistant`: interface do assistente IA/RAG, estados de resposta, grounding, fontes e bloqueios de autorização.
- `frontend-validation-and-documentation`: testes, homologação visual/funcional, build, documentação e matriz de integração com a API.

### Modified Capabilities

Nenhuma capability existente possui spec permanente neste repositório. A integração consumirá os contratos atuais do Backend V1 sem alterá-los.

## Impact

- Novo diretório de aplicação frontend e seus artefatos de build/teste.
- Integração com os endpoints `/api/v1`, JWT stateless, paginação, uploads Excel, exportações CSV/XLSX, analytics, qualidade, auditoria e assistente IA/RAG.
- Dependências frontend para Angular, componentes/gráficos e testes, escolhidas de forma compatível com o ambiente do projeto.
- Documentação nova em `frontend/` e `docs/`, sem alteração de regras de negócio ou implementação do backend.
