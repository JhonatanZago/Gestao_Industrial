# Tasks

## 1. Baseline e tokens

- [x] 1.1 Registrar baseline de `npm test`, `npm run build`, endpoints e screenshots atuais.
- [x] 1.2 Aplicar tokens exatos da referência, tipografia, sidebar 232px, espaçamentos e sombras; verificar build.

## 2. Dashboard pixel-accurate

- [x] 2.1 Ajustar shell/sidebar/header para a geometria da referência; verificar rota ativa, logout e drawer.
- [x] 2.2 Ajustar quatro KPIs coloridos com ícones SVG, dimensões e hover; verificar apenas campos reais.
- [x] 2.3 Ajustar gráfico real e painel de setores lado a lado; verificar percentuais, tooltip, empty e error.
- [x] 2.4 Ajustar três painéis inferiores com microbarras e resumo derivável; verificar ausência de números ilustrativos.
- [x] 2.5 Adicionar tabela/distribuição apenas quando contratos reais suportarem; verificar estado indisponível sem mock.

## 3. Responsividade e regressão

- [x] 3.1 Validar 1366x768 por screenshot e corrigir até a primeira dobra atender a referência.
- [x] 3.2 Validar 1920x1080 por screenshot e corrigir proporções, densidade e espaçamento.
- [x] 3.3 Validar tablet/mobile, acessibilidade, movimento reduzido e ausência de overflow.
- [x] 3.4 Executar `npm test`, `npm run build` e E2E configurado; registrar resultados e declarar GO somente com visual PASS.

> Observação: o E2E responsivo passou nas quatro resoluções. O fluxo autenticado depende de credenciais E2E e do backend operacional; a validação visual usou interceptação temporária apenas no script de captura, sem dados fictícios no frontend.
