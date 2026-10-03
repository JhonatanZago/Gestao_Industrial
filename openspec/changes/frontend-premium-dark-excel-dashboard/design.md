# Design: Frontend Premium Dark Excel Dashboard

## Context

O frontend é Angular 22 standalone e atualmente concentra shell, login, dashboard e ações em `frontend/src/app/app.ts`. A maior parte das rotas em `app.routes.ts` ainda aponta para placeholders, o menu expõe módulos secundários e o dashboard usa o contrato legado `analytics/resumo`. Já existem `AuthService`, guards, interceptor, `ApiService`, serviços de feature e componentes visuais reutilizáveis que devem ser preservados quando compatíveis.

O Backend V1 fornece os contratos core de dashboard geral, dashboard por setor, importação, analytics de funcionário/peça e relatório simplificado. O baseline atual foi verificado antes do planejamento: 15 testes frontend e o build passam. Não há specs permanentes registradas no OpenSpec; portanto esta mudança cria a capability do frontend.

## Goals / Non-Goals

### Goals

- Transformar o fluxo core em rotas reais, lazy e testáveis.
- Reutilizar autenticação, autorização, interceptors, serviços e componentes existentes quando seus contratos forem compatíveis.
- Consumir apenas endpoints e campos confirmados no backend.
- Centralizar o Design System Premium Dark e estados de tela.
- Entregar testes unitários, de contrato, acessibilidade e E2E com massa determinística.

### Non-Goals

- Alterar endpoints, DTOs, banco, regras de negócio ou migrações do backend.
- Recolocar máquinas, paradas, metas, qualidade avançada, auditoria ou IA no fluxo principal.
- Criar KPIs de OEE, lucro, receita, eficiência ou paradas sem contrato backend.
- Migrar para NgRx sem necessidade comprovada.

## Decisions

### Rotas e composição

Manter Angular standalone e substituir o componente raiz monolítico por shell, páginas e componentes de feature com rotas lazy para `/app/dashboard`, `/app/setores`, `/app/setores/:setorId`, os dois drill-downs, `/app/importacoes` e `/app/relatorios`. O shell continuará responsável por sessão, navegação e layout; cada página será responsável por seu estado de dados.

### Contratos e serviços

Criar modelos de resposta e serviços de dashboard/relatório alinhados aos contratos reais. Reaproveitar `AuthService`, `authGuard`, `permissionGuard`, interceptor e serviço de importação. Os serviços SHALL transportar query params explicitamente, cancelar consultas obsoletas e não transformar códigos identificadores em números.

### Design System e gráficos

Consolidar tokens em uma camada global e extrair componentes compartilhados para cards, filtros, tabela, estados, modal, toast e cabeçalho. Reutilizar o componente de gráfico SVG existente como uma única abstração visual; não adicionar biblioteca de gráficos enquanto ele cobrir tooltip, legenda, responsividade e acessibilidade exigidos pelo contrato.

### Importação e relatório

O upload terá máquina de estados explícita e bloqueio idempotente no cliente, sem esconder a confirmação real do backend. O relatório será uma consulta separada, com filtros compartilháveis e download apenas quando a API fornecer conteúdo e formato suportados.

### Testes e homologação

Manter Vitest para unidades e componentes. Adicionar Playwright como runner E2E do fluxo real, com backend e massa oficial controlados pelo ambiente de homologação; mocks ficam restritos a testes unitários. O E2E SHALL verificar os números da massa, navegação, permissões, estados de erro e logout.

### Compatibilidade e codificação

Preservar compatibilidade com URLs e sessão existentes onde não conflitar com o fluxo core. Corrigir arquivos tocados para UTF-8, com textos pt-BR e códigos identificadores tratados como string.

## Risks / Trade-offs

- A API real pode revelar campos opcionais diferentes dos esperados; contratos e testes devem tratar ausência explicitamente, sem fallback fictício.
- A extração do `app.ts` pode quebrar testes que dependam de detalhes internos; os testes serão migrados para comportamento público antes da remoção do monólito.
- Playwright aumenta dependências e tempo de CI, mas é necessário para validar o fluxo real solicitado.
- A massa oficial pode não existir no ambiente local; a homologação deverá falhar claramente e documentar o pré-requisito, nunca substituir os totais por mocks no E2E final.
- A retirada da sidebar secundária é breaking para navegação visual, embora os endpoints permaneçam disponíveis.

## Migration Plan

1. Registrar contratos e baseline atuais; adicionar modelos, serviços e rotas sem remover imediatamente o shell antigo.
2. Criar tokens/componentes compartilhados e migrar o shell para as quatro áreas.
3. Implementar dashboard geral, setores e drill-downs contra os endpoints reais.
4. Implementar importação, relatório, estados de erro e tratamento de permissões.
5. Migrar testes, adicionar Playwright, executar a massa oficial e comparar os totais.
6. Atualizar documentação e remover caminhos visuais/placeholders não usados após os testes passarem.

## Open Questions

Não há decisão material pendente para iniciar a implementação. A seleção da massa/credenciais de homologação deve ser feita pelo ambiente de execução, mantendo os valores oficiais definidos na especificação.

