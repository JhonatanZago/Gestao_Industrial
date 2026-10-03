# Tasks

## 1. Baseline e contratos

- [x] 1.1 Registrar o estado atual dos testes, build, rotas e payloads reais usados pelo frontend; verificar com `npm test`, `npm run build` e inventário dos serviços. Baseline: 21 testes passaram e o build passou após execução fora do sandbox.
- [x] 1.2 Mapear quais campos reais suportam KPIs, série temporal, Top funcionários, Top peças, resumo e duração; verificar que cada campo previsto na tela tem origem documentada e que lacunas ficam explicitamente indisponíveis. Registro em `docs/FRONTEND_DASHBOARD_INTERATIVO_CONTRATOS.md`.

## 2. Tokens, tipografia e primitives

- [x] 2.1 Consolidar tokens de background, surface, borda, texto, esmeralda, estados, raio, sombra, espaçamento e tipografia; verificar ausência de divergências visuais entre as telas core. Tokens globais e overrides Premium Dark aplicados.
- [x] 2.2 Criar ou ajustar primitives para ícone semântico, KPI, estado de painel, tooltip, filtro, ranking e ação de retry; verificar testes de renderização, foco e estados loading/empty/error. `UiIconComponent`, estados por painel e testes Angular passam.
- [x] 2.3 Implementar regra de movimento reduzido e contraste/foco acessíveis; verificar com teste de teclado e inspeção em `prefers-reduced-motion`. Regra CSS e foco visível aplicados.

## 3. Shell e consulta compartilhada

- [x] 3.1 Extrair o shell autenticado para sidebar, header e drawer mobile sem alterar guards, logout ou permissões; verificar que apenas as quatro áreas core aparecem e que logout remove dados protegidos. Shell, drawer e logout preservado; testes existentes passam.
- [x] 3.2 Compactar header com título, subtítulo, seletor de período e Atualizar; verificar transições Atualizar/Atualizando/Atualizado e bloqueio de clique concorrente. Estados do botão e período integrado ao header.
- [x] 3.3 Centralizar o estado de período, limites de data, seleção de setor e cancelamento/deduplicação de consultas; verificar que uma mudança de filtro não permite resposta obsoleta sobrescrever a tela. Período único, limites mensais e request id implementados.
- [x] 3.4 Adicionar testes responsivos do shell em 1920x1080, 1366x768, tablet e mobile; verificar drawer, primeira dobra e ausência de rolagem horizontal. E2E responsivo: 4/4 passou.

## 4. Dashboard geral

- [x] 4.1 Implementar os quatro KPIs reais com ícones, descrição e microvisual condicional; verificar que nenhum valor fictício ou comparação sem campo real aparece. Campos opcionais exibem `-` e não há comparação inventada.
- [x] 4.2 Reorganizar evolução da produção em painel com linha, área, pontos, grid, tooltip e texto equivalente; verificar loading, empty, error e `prefers-reduced-motion`. `UiChartComponent` usa somente `evolucao` real e estado vazio explícito.
- [x] 4.3 Implementar produção por setor ordenada por quantidade real, limite de 5–7 itens, Ver todos e linha clicável; verificar nomes/IDs dinâmicos e navegação para dashboard do setor. Home limita a sete e usa IDs do payload.
- [x] 4.4 Implementar Top funcionários, Top peças e resumo do período com preservação de códigos textuais; verificar ranking limitado, links autorizados e omissão segura de métricas não deriváveis. Rankings limitados a cinco e códigos permanecem strings.
- [x] 4.5 Executar testes de componente do dashboard com respostas reais representativas, vazias, inválidas, 401, 403 e erro recuperável; verificar retry e correlationId quando disponível. Cobertura de dashboard/erro/correlationId incluída; suíte: 25/25.

## 5. Setores e drill-downs

- [x] 5.1 Redesenhar a lista de setores com cards compactos, busca, dados reais e Ver detalhes; verificar que setores e IDs não são hardcoded. Cards e dados dinâmicos aplicados.
- [x] 5.2 Redesenhar o Dashboard do Setor mantendo período, KPIs disponíveis, evolução, produção por funcionário e por peça; verificar navegação de retorno e estados por painel. Dashboard contextual e evolução real integrados.
- [x] 5.3 Integrar drill-down de funcionário e peça preservando setor, período e códigos como `00147`; verificar URL, requisições e conteúdo sem conversão numérica. Rotas e serviços existentes preservam o código textual e foram mantidos.
- [x] 5.4 Executar testes de permissão, setor inexistente, setor vazio e drill-down não autorizado; verificar estados forbidden/not found/empty sem dados fictícios. Guards agora têm cobertura explícita de redirecionamento para `/login` e `/forbidden`; estados vazios continuam sem valores fictícios.

## 6. Importação e relatórios

- [x] 6.1 Redesenhar Importar Excel em quatro passos com setor dinâmico, drag-and-drop XLSX, modelo oficial e progresso; verificar extensão, seleção e prevenção de clique/reimportação duplicada. Quatro etapas, drag-and-drop e estados existentes preservados.
- [x] 6.2 Exibir resultados de importação concluída, parcial, duplicada e rejeitada com ações seguras; verificar que a mensagem usa o backend e não expõe stack trace, token ou detalhes sensíveis. Estados existentes foram mantidos e correlationId passou a ser exibido com segurança.
- [x] 6.3 Redesenhar Relatórios com período, setor, gerar relatório e resultado premium; verificar reconciliação com dashboard e ausência de exportação não suportada. Filtros usam o endpoint simplificado real.
- [x] 6.4 Executar testes de componente para importação e relatório cobrindo loading, sucesso, vazio, erro, 401 e 403; verificar retry apenas onde a operação for segura. Testes de validação/importação e relatório executados na suíte verde.

## 7. Integração visual e documentação

- [x] 7.1 Aplicar a linguagem visual compartilhada às rotas core e remover estilos duplicados/legados equivalentes; verificar consistência de tokens, hierarquia tipográfica e primeira dobra. Shell, dashboard, setor, importação e relatório usam a composição compartilhada.
- [x] 7.2 Atualizar documentação do frontend com origem dos dados, estados suportados, atalhos de navegação e limitações de métricas; verificar que os comandos documentados executam. Contratos documentados em `docs/FRONTEND_DASHBOARD_INTERATIVO_CONTRATOS.md`.
- [ ] 7.3 Executar E2E do fluxo login → dashboard → setor → funcionário/peça → relatório → importação conforme massa homologada; verificar totais reais e logout sem regressão.
- [x] 7.4 Executar integração final com `npm test`, `npm run build` e validação OpenSpec estrita; verificar zero erro de teste, template, TypeScript ou contrato visual obrigatório. Resultado: 27 testes, build e OpenSpec estrito passaram.
