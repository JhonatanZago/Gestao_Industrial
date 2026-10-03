# Proposal

## Why

O frontend já possui o fluxo core homologado e contratos reais para dashboard, setores, drill-downs, importação e relatórios, mas a experiência ainda não acompanha a referência Premium Dark adicionada em `SPEC_REDESIGN_PREMIUM_DARK_DASHBOARD_INTERATIVO.md`. O dashboard concentra pouca informação útil acima da dobra, usa sinais visuais provisórios e não conecta completamente período, atualização, rankings, navegação contextual e estados de painel.

## What Changes

- Redesenhar o shell autenticado com sidebar desktop compacta, drawer mobile, ícones semânticos e header de baixa altura.
- Reorganizar a Visão Geral em KPIs reais, evolução temporal, produção por setor, Top funcionários, Top peças e resumo do período.
- Integrar período, filtros compartilhados e ação Atualizar aos mesmos dados reais do backend, com estados Atualizando e Atualizado.
- Adicionar hover, foco, tooltips, transições curtas, navegação por setor/funcionário/peça e ações Ver todos sem rankings subjetivos ou KPIs inventados.
- Redesenhar a página de Setores e o Dashboard do Setor mantendo dados dinâmicos, filtros, drill-down e preservação de códigos como `00147`.
- Redesenhar Importar Excel em quatro etapas, incluindo drag-and-drop, progresso e resultado real de validação/importação.
- Redesenhar Relatórios com filtros de período/setor e somente formatos suportados pelo backend.
- Centralizar tokens, tipografia, estados de painel, responsividade e acessibilidade, respeitando `prefers-reduced-motion`.
- Adicionar cobertura automatizada dos estados e interações e validar com `npm test` e `npm run build`.
- **BREAKING**: alterar a composição visual e a organização das telas core; os contratos de autenticação, autorização e dados homologados não serão removidos nem alterados.

## Capabilities

### New Capabilities

- `frontend-premium-dark-dashboard-interativo`: experiência visual e interativa do dashboard, shell, setores, importação e relatórios sobre os contratos reais já homologados.

### Modified Capabilities

Nenhuma capability permanente foi encontrada por `openspec list --specs`. A mudança anterior `frontend-premium-dark-excel-dashboard` ainda está em andamento; esta proposta complementa sua implementação com a camada de experiência interativa descrita na nova SPEC, sem alterar seus contratos funcionais.

## Impact

- Frontend Angular em `frontend/src/app`, incluindo shell, templates, estilos, rotas, componentes compartilhados e testes.
- Serviços e modelos existentes serão reutilizados; qualquer dado adicional deverá vir de payload já suportado ou de contrato explicitamente confirmado, sem inventar OEE, lucro, faturamento, parada ou eficiência.
- Backend, autenticação, autorização, endpoints, importação homologada, relatórios reconciliados e códigos operacionais permanecem fora do escopo de alteração.
- Poderão ser ajustados testes E2E, documentação visual e tokens CSS. Não será adicionada biblioteca pesada de gráficos sem necessidade comprovada.
