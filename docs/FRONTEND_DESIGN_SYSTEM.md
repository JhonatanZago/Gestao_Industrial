# Design System Premium Dark

## Tokens

O tema usa grafite profundo para a aplicação, superfícies elevadas discretas, verde esmeralda como acento, cinza frio para texto secundário e cores semânticas para sucesso, aviso, erro e informação. Tokens e regras de foco ficam centralizados em `frontend/src/styles.scss` e `frontend/src/app/app.scss`.

## Componentes e estados

O shell demonstra cards de métrica, filtros, badges, tabela, progress bars, gráfico SVG, botões, login, sidebar, drawer mobile, assistente, estado vazio/informativo e mensagens de erro. Os componentes devem preservar labels, foco visível, contraste, `aria-label` em ações iconográficas e teclado.

## Responsividade

Desktop e notebook priorizam leitura executiva; em tablet os grids colapsam; no mobile a sidebar vira drawer, cards reduzem colunas e tabelas usam rolagem controlada.
