# Design

## Context

O frontend Angular já possui serviços e contratos reais, mas a composição atual ainda não reproduz a imagem de referência. O trabalho será feito sobre `App`, tokens globais, `UiIconComponent`, `UiChartComponent` e os testes existentes, sem criar outro frontend.

## Goals / Non-Goals

**Goals:** reproduzir a geometria da referência, melhorar densidade e hierarquia visual, manter estados acessíveis e validar por screenshot em duas resoluções.

**Non-Goals:** alterar backend, endpoints, autenticação, autorização, importação, relatórios ou adicionar dados fictícios.

## Decisions

- Usar CSS Grid com sidebar de 232px, conteúdo em duas colunas `1.9fr/.8fr`, três painéis inferiores e breakpoints definidos na SPEC.
- Centralizar tokens no stylesheet global e usar SVG inline leve para ícones, evitando nova biblioteca.
- Alimentar gráfico, rankings e percentuais pelos modelos `CoreDashboard`; widgets sem payload real permanecem vazios.
- Capturar screenshots com Playwright em 1366x768 e 1920x1080 e ajustar dimensões após inspeção visual.

## Risks / Trade-offs

- [Payload real sem tabela/distribuição] → manter o espaço reservado com estado indisponível, sem mock.
- [Divergência entre viewport e referência] → repetir screenshot nas duas resoluções e corrigir CSS antes do GO.
- [Mudança visual quebrar testes antigos] → preservar seletores, serviços, rotas e executar `npm test` após cada bloco.

## Migration Plan

Aplicar tokens e shell, depois dashboard e widgets inferiores; atualizar testes E2E; executar servidor Angular, screenshots, ajustes, `npm test` e `npm run build`. Rollback consiste em reverter somente arquivos frontend alterados.

## Open Questions

Nenhuma; a imagem é a referência visual principal e campos ausentes devem permanecer indisponíveis.
