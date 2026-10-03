# Proposal

## Why

O arquivo `CODEX_MASTER_PREMIUM_DARK_PIXEL_ACCURATE.md` e a imagem de referência definem uma composição visual específica que ainda não está reproduzida com fidelidade no frontend: densidade, proporções, cards coloridos, gráfico/setores lado a lado e painéis inferiores precisam ser reconstruídos sobre os dados reais.

## What Changes

- Reproduzir a composição Premium Dark da referência em 1366x768 e 1920x1080.
- Ajustar sidebar, header, KPIs, gráfico, produção por setor, rankings e resumo do período.
- Adicionar tabela de últimas produções e distribuição somente quando os dados reais estiverem disponíveis.
- Manter autenticação, permissões, endpoints, importação Excel, relatórios e regras de negócio intactos.
- Validar visualmente com screenshots e executar testes/build finais.

## Capabilities

### New Capabilities

- `frontend-premium-dark-pixel-accurate`: composição visual pixel-accurate e validação responsiva do dashboard industrial.

### Modified Capabilities

Nenhuma capability permanente foi registrada; a mudança complementa o fluxo funcional já homologado.

## Impact

- Frontend Angular em `frontend/src/app`, estilos, componentes de visualização, testes E2E e documentação.
- Nenhuma alteração no backend ou nos contratos de API.
