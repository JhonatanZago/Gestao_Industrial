# Tasks

## 1. Baseline, contratos e fundação

- [x] 1.1 Registrar no frontend os contratos confirmados de dashboard geral, dashboard de setor, relatório simplificado e importação, mantendo códigos como string; verificar com testes de mapeamento para respostas completas e campos opcionais.
- [x] 1.2 Separar modelos, serviços e estado de UI por feature sem alterar AuthService, guards, interceptor ou ApiService; verificar com `npm test -- --watch=false` e build verde.
- [x] 1.3 Criar a estrutura de rotas standalone lazy do fluxo core e uma matriz de permissões/401/403; verificar que cada URL principal resolve para sua tela e que URLs protegidas redirecionam corretamente.

## 2. Design System e shell autenticado

- [x] 2.1 Consolidar tokens Premium Dark e extrair Button, IconButton, Card, StatCard, Badge, Input, Select, DateRange, SearchInput, Table, Pagination, ChartCard, Tooltip, Modal, Toast, Skeleton, EmptyState, ErrorState, PageHeader e FilterBar; verificar snapshots/testes de estados e ausência de CSS duplicado.
- [x] 2.2 Migrar o shell para sidebar/topbar responsivos com apenas Visão Geral, Setores, Importar Excel e Relatórios, mantendo perfil, logout e drawer mobile; verificar teste de navegação, foco por teclado e viewport mobile.
- [x] 2.3 Remover do fluxo visual principal os módulos secundários sem excluir seus endpoints backend; verificar teste que a sidebar contém exatamente quatro áreas e que build não possui links quebrados.
- [x] 2.4 Corrigir textos e arquivos tocados para UTF-8, pt-BR e formatação de números, datas e duração; verificar teste de locale e revisão visual nas resoluções 1920x1080 e 1366x768.

## 3. Dashboard geral e setores

- [x] 3.1 Implementar serviço e tela de Visão Geral usando `/analytics/dashboard`, com total de produção, setores, funcionários, peças, evolução e duração somente quando fornecidos; verificar testes de contrato com a massa 350, 00147=200 e 00152=150.
- [x] 3.2 Implementar loading, empty, error, forbidden, retry e correlação na Visão Geral sem KPI inventado; verificar testes para resposta vazia, 401, 403, erro de rede e `correlationId`.
- [x] 3.3 Implementar lista dinâmica de setores e dashboard por setor usando `/analytics/dashboard/setor/{setorId}`, filtros suportados e contexto para drill-down; verificar teste sem IDs hardcoded e reconciliação de totais.
- [x] 3.4 Adicionar testes de componente e documentar no `docs/FRONTEND_INTEGRACAO_CORE.md` os endpoints, campos opcionais e estados das telas de dashboard; verificar que os exemplos correspondem ao serviço implementado.

## 4. Drill-downs operacionais

- [x] 4.1 Implementar rotas e telas de funcionário e peça com setor, período e código original; verificar teste de URL/requisição preservando `00147` e ausência de avaliação subjetiva.
- [x] 4.2 Integrar relações de peça e funcionários somente quando retornadas e autorizadas, com estados empty/error/forbidden; verificar testes de autorização, peça sem relações e erro recuperável.
- [x] 4.3 Adicionar filtros compartilhados, paginação, debounce, cancelamento de requisições obsoletas e `trackBy` nas listas; verificar teste de sequência rápida de filtros e acessibilidade da tabela.
- [x] 4.4 Documentar a navegação dashboard → setor → funcionário/peça em `docs/FRONTEND_PREMIUM_DARK.md`; verificar links e nomes de rotas contra `app.routes.ts`.

## 5. Importação Excel

- [x] 5.1 Implementar seleção dinâmica de setor, drag-and-drop/seleção de `.xlsx`, template com as seis colunas oficiais e validação client-side; verificar testes de extensão, arquivo vazio, colunas inválidas e setor ausente.
- [x] 5.2 Implementar a máquina de estados selecionado, validando, enviando, processando, sucesso, partial, duplicate e error usando a resposta real do backend; verificar testes de cada transição e mensagens sem stack trace.
- [x] 5.3 Bloquear clique duplo, reimportação do mesmo arquivo e submissão concorrente, preservando retry seguro após falha; verificar teste de idempotência e que uma operação mutável não é repetida automaticamente.
- [x] 5.4 Documentar preparação, validação, resposta por linha e recuperação de falhas no `frontend/README.md`; verificar que o procedimento documentado funciona com o template homologado.

## 6. Relatórios e segurança transversal

- [x] 6.1 Implementar tela de Relatórios com período/setor, consulta a `/relatorios/simplificado` e formatos realmente suportados; verificar teste de download válido, resposta vazia e reconciliação com total 350.
- [x] 6.2 Aplicar tratamento global de 401/403, mensagens seguras, correlation ID e limpeza de sessão sem logar senha/JWT; verificar testes de interceptor, logout e inspeção de logs de teste.
- [x] 6.3 Implementar estados compartilhados de loading/empty/error/forbidden em relatório, importação e drill-downs; verificar teste de acessibilidade, foco e ação de retry em cada estado.
- [x] 6.4 Registrar em `docs/FRONTEND_HOMOLOGACAO.md` critérios de segurança, permissões, dados reais e reconciliação; verificar que cada critério possui comando ou evidência de execução.

## 7. Homologação integrada

- [x] 7.1 Preservar os 15 testes frontend existentes e adicionar cobertura de contratos, componentes, permissões, leading zeros e estados; verificar `npm test -- --watch=false` sem falhas.
- [x] 7.2 Configurar Playwright e ambiente E2E com backend/massa oficial, sem mocks no fluxo final; verificar instalação, descoberta do teste e diagnóstico claro quando a massa não estiver disponível.
- [ ] 7.3 Executar E2E login → importar template em Pintura a pó → dashboard → setor → funcionário/peça → relatório → logout; verificar total 350, funcionários 200/150, peças 270/80 e duração 185 minutos.
- [x] 7.4 Executar regressão do backend com os 154 testes, build frontend e validação OpenSpec estrita; verificar `npm run build`, suíte backend e `openspec validate "frontend-premium-dark-excel-dashboard" --type change --strict`.
- [x] 7.5 Emitir o resultado final de homologação `FRONTEND EXCEL → DASHBOARD: GO` somente se todos os testes, build, acessibilidade, segurança, documentação e E2E estiverem verdes; caso contrário registrar NO-GO com evidências e bloqueios.
