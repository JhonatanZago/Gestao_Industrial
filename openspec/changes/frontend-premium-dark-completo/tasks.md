# Tasks

## 1. Fase A â€” AnÃ¡lise do Backend V1

- [x] 1.1 Inventariar controllers, DTOs, enums, permissÃµes, paginaÃ§Ã£o, filtros, uploads, downloads, IA/RAG e erros; verificar a matriz endpoint â†’ mÃ©todo â†’ contrato â†’ permissÃ£o em `docs/FRONTEND_INTEGRACAO_API.md`
- [x] 1.2 Confirmar versÃµes disponÃ­veis de Node/npm/Angular e escolher uma biblioteca de grÃ¡ficos compatÃ­vel; verificar o registro das decisÃµes no design e no `frontend/README.md`
- [x] 1.3 Definir ambiente local, base URL, proxy e estratÃ©gia de mocks contratuais para testes; verificar que nenhum mock permanente serÃ¡ usado em produÃ§Ã£o

## 2. Fase B â€” Arquitetura Angular

- [x] 2.1 Criar o workspace frontend standalone com scripts de desenvolvimento, teste, lint quando aplicÃ¡vel e build; verificar `npm install` e a estrutura inicial
- [x] 2.2 Criar `core`, `shared`, `layout` e `features` com lazy loading e modelos separados entre DTO, view model e estado de UI; verificar navegaÃ§Ã£o de uma rota inicial
- [x] 2.3 Centralizar environments, `apiBaseUrl`, configuraÃ§Ãµes de timeout aplicÃ¡veis e proxy `/api`; verificar que componentes nÃ£o contÃªm URLs hardcoded

## 3. Fase C â€” Design System Premium Dark

- [x] 3.1 Criar tokens de cores, tipografia, espaÃ§amento, radius, sombras, estados e breakpoints; verificar uma pÃ¡gina de catÃ¡logo visual no tema Premium Dark
- [x] 3.2 Implementar Button, IconButton, Card, StatCard, Badge, inputs, select, date range, modal, drawer, toast e confirm dialog; verificar testes unitÃ¡rios dos estados crÃ­ticos
- [x] 3.3 Implementar Table, Pagination, FilterBar, MetricDelta, ChartCard, skeleton, spinner, empty, error e forbidden states; verificar teclado, labels, foco e responsividade
- [x] 3.4 Criar wrappers de grÃ¡ficos e Ã­cones com uma Ãºnica biblioteca compatÃ­vel; verificar tooltip, legenda, estado vazio e fallback sem dados
- [x] 3.5 Documentar tokens, componentes, estados e exemplos em `docs/FRONTEND_DESIGN_SYSTEM.md`; verificar que o documento corresponde ao catÃ¡logo implementado

## 4. Fase D â€” AutenticaÃ§Ã£o e Shell

- [x] 4.1 Implementar AuthService, armazenamento seguro de sessÃ£o e logout conforme o contrato real; verificar login vÃ¡lido, invÃ¡lido e logout sem registrar token
- [x] 4.2 Implementar auth guard, permission guard e interceptor de autenticaÃ§Ã£o; verificar 401, 403, sessÃ£o expirada e tentativa direta em rota restrita
- [x] 4.3 Implementar interceptor/tratador de erros para 429, 500, 503 e correlation ID; verificar mensagens seguras sem stack trace
- [x] 4.4 Implementar login, shell, sidebar recolhÃ­vel/drawer, topbar, perfil e breadcrumbs; verificar desktop, mobile e menu derivado das permissÃµes reais
- [x] 4.5 Criar testes unitÃ¡rios de AuthService, guards, interceptors e componentes crÃ­ticos; verificar suÃ­te frontend verde

## 5. Fase E â€” Dashboard Executivo

- [x] 5.1 Implementar filtros globais e consumo de analytics/resumo, sÃ©ries, comparativos e setores reais; verificar requests e parÃ¢metros contra a matriz de contratos
- [x] 5.2 Implementar cards, grÃ¡ficos, resumo por setor, paradas, metas, qualidade e financeiro condicional; verificar ausÃªncia de dados e bloqueio financeiro
- [x] 5.3 Implementar drill-down para setor, funcionÃ¡rio, peÃ§a e mÃ¡quina usando somente endpoints disponÃ­veis; verificar que IDs e escopos nÃ£o sÃ£o alterados no cliente
- [x] 5.4 Criar testes de integraÃ§Ã£o do login â†’ dashboard â†’ filtro â†’ drill-down; verificar respostas mockadas conforme DTOs reais e um smoke contra backend local quando disponÃ­vel

## 6. Fase F â€” ProduÃ§Ã£o e Setores

- [x] 6.1 Implementar telas de produÃ§Ã£o e setores com filtros, tabelas, paginaÃ§Ã£o e sÃ©ries temporais; verificar filtros por perÃ­odo, setor, funcionÃ¡rio, peÃ§a, mÃ¡quina e turno quando suportados
- [x] 6.2 Implementar CRUDs de setores e estados ativo/inativo existentes; verificar validaÃ§Ã£o, duplicidade, erro de negÃ³cio e atualizaÃ§Ã£o apÃ³s mutaÃ§Ã£o
- [x] 6.3 Criar testes e documentaÃ§Ã£o da integraÃ§Ã£o de produÃ§Ã£o/setores; verificar cobertura dos estados loading, empty, error e forbidden

## 7. Fase G â€” FuncionÃ¡rios

- [x] 7.1 Implementar lista, busca, paginaÃ§Ã£o, CRUD e ativo/inativo de funcionÃ¡rios conforme DTOs reais; verificar permissÃµes e mensagens de validaÃ§Ã£o
- [x] 7.2 Implementar detalhe do funcionÃ¡rio com produÃ§Ã£o, tempos, desempenho e cobertura disponÃ­veis; verificar que nÃ£o hÃ¡ ranking subjetivo ou dados fora do escopo
- [x] 7.3 Criar testes de permissÃ£o, integraÃ§Ã£o e responsividade da feature; verificar navegaÃ§Ã£o dashboard â†’ funcionÃ¡rio

## 8. Fase H â€” PeÃ§as e MÃ¡quinas

- [x] 8.1 Implementar lista, busca, CRUD e estado de peÃ§as; verificar produÃ§Ã£o, setores, tempo e valor somente quando retornados e autorizados
- [x] 8.2 Implementar lista, busca, CRUD e estado de mÃ¡quinas/turnos/alocaÃ§Ãµes disponÃ­veis; verificar ausÃªncia de telemetria inventada
- [x] 8.3 Criar testes e documentaÃ§Ã£o das integraÃ§Ãµes de peÃ§as e mÃ¡quinas; verificar paginaÃ§Ã£o server-side e estados vazios

## 9. Fase I â€” ImportaÃ§Ãµes

- [x] 9.1 Implementar Ã¡rea de upload Excel com limite e extensÃ£o conforme backend; verificar validaÃ§Ã£o antes do envio e acessibilidade do drag-and-drop
- [x] 9.2 Implementar ciclo validar â†’ enviar â†’ processar â†’ resultado e histÃ³rico; verificar contagens, status, linhas invÃ¡lidas e detalhe sem stack trace
- [x] 9.3 Criar testes para fixture vÃ¡lida e fixtures negativas do backend; verificar que arquivo vazio, formato invÃ¡lido, coluna ausente e valores invÃ¡lidos nÃ£o sÃ£o apresentados como persistidos
- [x] 9.4 Documentar o fluxo em `docs/FRONTEND_INTEGRACAO_API.md` e `frontend/README.md`; verificar comandos e limites reais

## 10. Fase J â€” Paradas, Metas e Desempenho

- [x] 10.1 Implementar consulta e CRUD das paradas/motivos existentes; verificar duraÃ§Ã£o, motivo, percentual, filtros e estados ativo/inativo
- [x] 10.2 Implementar metas e vigÃªncia; verificar atingimento, histÃ³rico, ausÃªncia de meta e nÃ£o conversÃ£o artificial para zero
- [x] 10.3 Implementar desempenho e tempo padrÃ£o com cobertura contextual; verificar filtros e escopos por setor, funcionÃ¡rio, peÃ§a e mÃ¡quina
- [x] 10.4 Criar testes de integraÃ§Ã£o e documentaÃ§Ã£o das trÃªs features; verificar igualdade de parÃ¢metros com Analytics

## 11. Fase K â€” Qualidade

- [x] 11.1 Implementar resumo, problemas, duplicidades e detalhe de qualidade de dados; verificar cobertura, alertas, crÃ­ticos e inconsistÃªncias reais
- [x] 11.2 Criar filtros, tabela de problemas, badges e estados de qualidade; verificar que qualidade de dados nÃ£o Ã© apresentada como qualidade fÃ­sica do produto
- [x] 11.3 Criar testes de contrato e permissÃ£o da tela de qualidade; verificar respostas e estados sem dados

## 12. Fase L â€” RelatÃ³rios

- [x] 12.1 Implementar seleÃ§Ã£o de relatÃ³rio, perÃ­odo, filtros e formatos realmente suportados; verificar que formatos inexistentes nÃ£o aparecem
- [x] 12.2 Implementar geraÃ§Ã£o, download e histÃ³rico somente quando o backend oferecer; verificar nome/tipo de arquivo e erro seguro
- [x] 12.3 Criar testes que comparem filtros e mÃ©tricas de relatÃ³rio com Dashboard/Analytics; verificar documentaÃ§Ã£o dos endpoints e formatos

## 13. Fase M â€” Auditoria

- [x] 13.1 Implementar listagem/detalhe de auditoria conforme contrato e permissÃ£o; verificar ator, data, aÃ§Ã£o, recurso, empresa e antes/depois disponÃ­vel
- [x] 13.2 Aplicar sanitizaÃ§Ã£o visual para nÃ£o exibir segredos; verificar teste de autorizaÃ§Ã£o e ausÃªncia de credenciais nos componentes
- [x] 13.3 Documentar a integraÃ§Ã£o e validar rota direta sem permissÃ£o; verificar estado forbidden

## 14. Fase N â€” Assistente IA/RAG

- [x] 14.1 Implementar chat e chamadas do assistente usando o contrato real; verificar loading, resposta, fontes, perÃ­odo, avisos e nenhum chain-of-thought
- [x] 14.2 Implementar estados `SEM_DADOS`, `SEM_PERMISSAO`, `PRECISA_CLARIFICACAO` e `INDISPONIVEL`; verificar que o ERP continua navegÃ¡vel com IA degradada
- [x] 14.3 Implementar documentos RAG somente se a permissÃ£o e os endpoints existirem; verificar escopo empresarial/setorial e bloqueio cross-company/cross-sector
- [x] 14.4 Criar testes com fake/mock determinÃ­stico para perguntas homologadas, prompt injection e tool injection; verificar grounding nos nÃºmeros do backend

## 15. Fase O â€” ConfiguraÃ§Ãµes

- [x] 15.1 Implementar somente perfil, preferÃªncias locais e informaÃ§Ãµes suportadas; verificar que configuraÃ§Ãµes administrativas inexistentes nÃ£o sÃ£o exibidas
- [x] 15.2 Criar testes de autorizaÃ§Ã£o e documentaÃ§Ã£o das configuraÃ§Ãµes; verificar persistÃªncia somente quando houver contrato explÃ­cito

## 16. Fase P â€” IntegraÃ§Ã£o e UX

- [x] 16.1 Revisar todos os mÃ³dulos para loading skeleton, empty state, retry, forbidden, 401/403/429/500/503 e correlation ID; verificar checklist visual completo
- [x] 16.2 Validar responsividade em 1920x1080, 1366x768, tablet e mobile; verificar overflow, sidebar, tabelas, grÃ¡ficos e diÃ¡logos
- [x] 16.3 Validar acessibilidade bÃ¡sica por teclado, foco, contraste, labels e ARIA; verificar auditoria automatizada/manual disponÃ­vel no ambiente
- [x] 16.4 Otimizar lazy loading, code splitting, track/sinais, debounce e cancelamento de requests; verificar ausÃªncia de chamadas duplicadas observÃ¡veis
- [x] 16.5 Atualizar matriz endpoint â†’ mÃ©todo â†’ request/response â†’ permissÃ£o â†’ feature e registrar divergÃªncias; verificar cada linha contra cÃ³digo backend/documentaÃ§Ã£o

## 17. Fase Q â€” Testes, DocumentaÃ§Ã£o e HomologaÃ§Ã£o

- [x] 17.1 Criar E2E do fluxo login â†’ dashboard â†’ filtro â†’ setor â†’ funcionÃ¡rio â†’ importaÃ§Ã£o â†’ resultado â†’ relatÃ³rio â†’ assistente â†’ logout; verificar ambiente controlado sem IA paga
- [x] 17.2 Executar testes unitÃ¡rios, integraÃ§Ã£o, permissÃµes e E2E; verificar relatÃ³rio por categoria e falhas reproduzÃ­veis
- [x] 17.3 Executar `npm test`, lint se configurado e `npm run build`; verificar TypeScript sem erros e artefato de produÃ§Ã£o gerado
- [x] 17.4 Criar `frontend/README.md`, `docs/FRONTEND_ARQUITETURA.md`, `docs/FRONTEND_INTEGRACAO_API.md` e `docs/FRONTEND_HOMOLOGACAO.md`; verificar comandos executÃ¡veis e limitaÃ§Ãµes reais
- [x] 17.5 Executar homologaÃ§Ã£o visual/funcional final e preencher matriz de aceite; verificar que nenhum requisito obrigatÃ³rio Ã© marcado sem evidÃªncia
- [x] 17.6 Emitir decisÃ£o explÃ­cita `FRONTEND: GO` somente com todos os gates aprovados, ou `FRONTEND: NO-GO` com bloqueadores exatos; verificar relatÃ³rio final versionado

