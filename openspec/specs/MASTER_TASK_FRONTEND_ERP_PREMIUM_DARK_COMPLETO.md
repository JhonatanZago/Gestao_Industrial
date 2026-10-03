# MASTER TASK FRONTEND --- ERP INDUSTRIAL PREMIUM DARK COMPLETO

## MISSÃO

Analise o projeto completo do **Sistema de Gestão e Inteligência de
Produção da Metalúrgica** e implemente o frontend completo, integrado ao
Backend V1 homologado, em uma única change/plano mestre OpenSpec.

O objetivo é entregar um ERP industrial moderno, profissional,
responsivo e consistente, no estilo **Premium Dark**, utilizando
exclusivamente os contratos e regras reais já existentes no backend.

> Esta é uma MASTER TASK. Trabalhe por fases internas e checkpoints. Não
> tente gerar arquivos aleatórios de todas as telas simultaneamente.
> Primeiro analise contratos e arquitetura; depois construa fundação;
> depois features; finalmente integração, testes e homologação.

------------------------------------------------------------------------

# 1. REGRAS ABSOLUTAS

1.  Leia TODO o backend antes de criar o frontend.
2.  Leia README, OpenSpec, OpenAPI/Swagger e documentação das TASKS
    01--18.
3.  O Backend V1 é a fonte de verdade.
4.  Não recrie regras de negócio no Angular.
5.  Não invente endpoints.
6.  Não invente campos de DTO.
7.  Não altere backend apenas para facilitar o frontend.
8.  Não hardcode dados de produção.
9.  Não deixe mocks permanentes quando a API real existir.
10. Não invente permissões.
11. Não exponha funcionalidades não autorizadas.
12. Não armazene senha.
13. Não registre JWT/secrets no console.
14. Não use `any` indiscriminadamente.
15. Não faça uma aplicação monolítica em um único componente.
16. Não copie o mesmo CSS em todas as telas.
17. Não conclua enquanto `test` e `build` estiverem quebrados.
18. Preserve tudo que já funciona no backend.
19. Se contrato real divergir deste documento, siga o contrato
    homologado e registre a divergência.
20. Ao encontrar bloqueio real, documente-o em vez de inventar solução
    incompatível.

------------------------------------------------------------------------

# 2. OPENSPEC

Criar uma change mestre:

``` text
frontend-premium-dark-completo
```

Organizar `tasks.md` em fases:

``` text
Fase A — análise do Backend V1
Fase B — arquitetura Angular
Fase C — Design System Premium Dark
Fase D — autenticação e shell
Fase E — Dashboard
Fase F — produção e setores
Fase G — funcionários
Fase H — peças e máquinas
Fase I — importações
Fase J — paradas, metas e desempenho
Fase K — qualidade
Fase L — relatórios
Fase M — auditoria
Fase N — assistente IA/RAG
Fase O — configurações
Fase P — integração e UX
Fase Q — testes e homologação
```

Marcar cada item somente após implementação e validação real.

------------------------------------------------------------------------

# 3. ANÁLISE OBRIGATÓRIA ANTES DE CODIFICAR

Produzir um mapa:

``` text
Endpoint
→ método
→ request
→ response
→ autenticação
→ permissão
→ feature Angular consumidora
```

Mapear também:

``` text
roles
permissões
empresa
setores autorizados
paginação
ordenação
filtros
erros
correlationId
upload
download
IA/RAG
```

Não começar a criar telas complexas antes desse levantamento.

------------------------------------------------------------------------

# 4. ARQUITETURA ANGULAR

Utilizar Angular compatível com o ambiente existente, preferencialmente
standalone.

Estrutura de referência:

``` text
src/app/
├── core/
│   ├── auth/
│   ├── guards/
│   ├── interceptors/
│   ├── config/
│   ├── models/
│   ├── services/
│   └── http/
│
├── shared/
│   ├── ui/
│   ├── components/
│   ├── directives/
│   ├── pipes/
│   ├── utils/
│   └── models/
│
├── layout/
│   ├── shell/
│   ├── sidebar/
│   ├── topbar/
│   ├── page-header/
│   └── breadcrumbs/
│
└── features/
    ├── auth/
    ├── dashboard/
    ├── producao/
    ├── setores/
    ├── funcionarios/
    ├── pecas/
    ├── maquinas/
    ├── importacoes/
    ├── paradas/
    ├── metas/
    ├── desempenho/
    ├── qualidade/
    ├── relatorios/
    ├── auditoria/
    ├── assistente/
    └── configuracoes/
```

Aplicar lazy loading por feature.

------------------------------------------------------------------------

# 5. VISUAL PREMIUM DARK

O sistema deve transmitir:

``` text
industrial
executivo
moderno
limpo
tecnológico
profissional
confiável
```

Evitar aparência:

``` text
template Bootstrap genérico
dashboard colorido demais
efeitos neon excessivos
gradientes exagerados
cards gigantes
bordas muito fortes
interface gamer
```

## Direção visual

``` text
background geral → grafite/preto profundo
surfaces → tons escuros levemente elevados
accent → verde/esmeralda
texto principal → quase branco
texto secundário → cinza frio
success → verde
warning → âmbar
danger → vermelho
info → azul discreto
```

Usar tokens, não espalhar valores hardcoded.

------------------------------------------------------------------------

# 6. DESIGN TOKENS

Centralizar:

``` text
--bg-app
--bg-sidebar
--bg-topbar
--bg-surface
--bg-surface-raised
--bg-hover

--border-subtle
--border-strong

--text-primary
--text-secondary
--text-muted

--accent-primary
--accent-hover

--success
--warning
--danger
--info

--radius-sm
--radius-md
--radius-lg
--radius-xl

--shadow-sm
--shadow-card

--spacing-*
```

------------------------------------------------------------------------

# 7. LAYOUT PRINCIPAL

Estrutura visual:

``` text
┌─────────────────────────────────────────────────────────────────────────┐
│ SIDEBAR                 TOPBAR                                          │
│                         Empresa • Período          🔔  Perfil           │
│                                                                         │
│ ◈ METALÚRGICA ERP       TÍTULO DA PÁGINA                               │
│                         descrição/contexto                              │
│ ▣ Visão Geral                                                           │
│ ◫ Produção              CONTEÚDO                                        │
│ ◇ Setores                                                               │
│ ♙ Funcionários                                                          │
│ ◈ Peças                                                                 │
│ ⚙ Máquinas                                                              │
│ ⇧ Importações                                                           │
│ ⏱ Paradas                                                               │
│ ◎ Metas                                                                 │
│ ◉ Desempenho                                                            │
│ ✓ Qualidade                                                             │
│ ▤ Relatórios                                                            │
│ ◌ Auditoria                                                             │
│ ✦ Assistente IA                                                         │
│ ⚙ Configurações                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

Sidebar: - fixa no desktop; - recolhível; - tooltip quando recolhida; -
drawer no mobile; - estado ativo elegante; - menu baseado nas permissões
reais.

Topbar: - empresa atual; - contexto; - usuário; - perfil; - logout; -
área preparada para notificações somente se houver suporte real.

------------------------------------------------------------------------

# 8. COMPONENTES REUTILIZÁVEIS

Criar Design System interno:

``` text
Button
IconButton
Card
StatCard
Badge
Input
PasswordInput
Select
MultiSelect se necessário
DatePicker/DateRange
SearchInput
Table
Pagination
Tabs
Tooltip
Dropdown
Modal/Dialog
ConfirmDialog
Drawer
Toast
LoadingSkeleton
Spinner
EmptyState
ErrorState
ForbiddenState
PageHeader
FilterBar
MetricDelta
StatusIndicator
ChartCard
```

Todos devem funcionar no Premium Dark.

------------------------------------------------------------------------

# 9. LOGIN

Criar login premium.

Layout sugerido:

``` text
┌──────────────────────────┬──────────────────────────┐
│                          │                          │
│   METALÚRGICA ERP        │      Bem-vindo          │
│                          │                          │
│   Gestão e Inteligência  │  Usuário                │
│   de Produção            │  [________________]      │
│                          │                          │
│   Dados • Produção • IA  │  Senha                  │
│                          │  [________________]      │
│                          │                          │
│                          │  [      ENTRAR       ]   │
│                          │                          │
└──────────────────────────┴──────────────────────────┘
```

Implementar:

``` text
loading
erro de credenciais
backend indisponível
validação
Enter para enviar
mostrar/ocultar senha
```

Seguir contrato real de autenticação.

------------------------------------------------------------------------

# 10. AUTENTICAÇÃO E SEGURANÇA

Criar:

``` text
AuthService
authGuard
permissionGuard
authInterceptor
errorInterceptor ou estratégia equivalente
```

Tratar:

``` text
401
403
429
500
503
```

Quando backend retornar `correlationId`:

``` text
Código de suporte: XXXXX
```

Não expor stack trace.

------------------------------------------------------------------------

# 11. DASHBOARD EXECUTIVO

Esta é a tela principal.

Estrutura:

``` text
Visão Geral
├── filtros
├── KPIs
├── evolução da produção
├── comparação
├── desempenho dos setores
├── paradas
├── metas
├── qualidade dos dados
└── resumo operacional
```

## Cards principais

Quando suportados pela API:

``` text
Produção total
Eficiência/desempenho
Tempo produtivo
Tempo de parada
Meta
Valor industrial (somente autorizado)
```

Cada card:

``` text
valor
unidade
comparação
seta/tendência
contexto do período
```

Nunca chamar valor industrial de lucro se backend não fizer isso.

------------------------------------------------------------------------

# 12. GRÁFICO DE PRODUÇÃO

Gráfico principal:

``` text
linha/área
produção ao longo do tempo
comparação opcional com período anterior
tooltip
zoom/filtro quando fizer sentido
```

Sem gráfico 3D.

------------------------------------------------------------------------

# 13. RESUMO POR SETOR

Cards/lista:

``` text
Pintura
Solda
KTL
Estamparia
Zincagem
Dobradeira
Laser
outros cadastrados
```

Mostrar somente setores reais/autorizados.

Informações possíveis:

``` text
produção
meta
desempenho
paradas
tendência
```

Clique abre drill-down do setor.

------------------------------------------------------------------------

# 14. PRODUÇÃO

Tela com:

``` text
KPIs
filtros
série temporal
produção por setor
produção por peça
produção por funcionário
tabela detalhada
```

Filtros reais:

``` text
período
setor
funcionário
peça
máquina
turno
```

Somente se suportados pela API.

------------------------------------------------------------------------

# 15. SETORES

Tela:

``` text
cards/resumo
ranking
produção
desempenho
paradas
meta
tendência
```

Ao clicar em setor:

``` text
Setor — Pintura
├── produção
├── funcionários
├── peças
├── máquinas
├── paradas
├── metas
├── desempenho
├── qualidade
└── evolução
```

------------------------------------------------------------------------

# 16. FUNCIONÁRIOS

Tela geral:

``` text
busca por código
setor
produção
tempo produtivo
tempo parado
desempenho contextualizado
```

Não criar ranking humilhante ou julgamento subjetivo.

## Drill-down

Ao clicar no funcionário:

``` text
Funcionário 00147

Produção total
Tempo produtivo
Tempo parado
Desempenho
Cobertura dos dados

Peças produzidas
Evolução
Paradas relacionadas
Comparativos
```

Usar códigos/identificação conforme política do backend.

------------------------------------------------------------------------

# 17. PEÇAS

Tela:

``` text
busca por código
produção total
setores
quantidade
tempo
valor industrial autorizado
tendência
```

Drill-down:

``` text
evolução
setores
funcionários relacionados quando permitido
máquinas
tempo
valor industrial
qualidade
```

------------------------------------------------------------------------

# 18. MÁQUINAS

Tela:

``` text
máquinas
setor
produção associada
tempo
paradas
status/dados disponíveis
```

Não inventar telemetria ou status em tempo real se backend não possuir.

------------------------------------------------------------------------

# 19. IMPORTAÇÃO EXCEL

Tela muito importante.

Criar área drag-and-drop premium:

``` text
┌─────────────────────────────────────┐
│                                     │
│      Arraste a planilha aqui        │
│              ou                     │
│        [Selecionar arquivo]         │
│                                     │
│ XLSX • limite conforme backend      │
└─────────────────────────────────────┘
```

Fluxo:

``` text
seleção
→ validação
→ upload
→ processamento
→ resultado
```

Mostrar:

``` text
arquivo
setor/contexto quando necessário
status
linhas processadas
sucessos
erros
data
usuário
```

Detalhe de erro sem stack trace.

------------------------------------------------------------------------

# 20. PARADAS

Dashboard próprio:

``` text
tempo total parado
quantidade de ocorrências
principais motivos
setores afetados
evolução
```

Gráficos:

``` text
barras por motivo
linha temporal
distribuição por setor
```

------------------------------------------------------------------------

# 21. METAS

Tela:

``` text
metas vigentes
atingimento
período
setor
histórico
```

Estados:

``` text
atingida
em andamento
abaixo
sem meta
```

Não tratar ausência como zero.

------------------------------------------------------------------------

# 22. DESEMPENHO

Tela:

``` text
desempenho geral
por setor
por funcionário autorizado
por peça quando aplicável
cobertura
tempo padrão
```

Sempre mostrar contexto de qualidade/cobertura quando necessário.

------------------------------------------------------------------------

# 23. QUALIDADE DOS DADOS

Criar tela dedicada:

``` text
Índice de qualidade
Cobertura
Registros válidos
Alertas
Críticos
Duplicidades
Inconsistências
```

Visual:

``` text
cards
indicadores
tabela de problemas
filtros
status
```

Esta tela mede qualidade do DADO, não qualidade física da peça, salvo se
backend tiver esse domínio.

------------------------------------------------------------------------

# 24. RELATÓRIOS

Tela Premium:

``` text
tipo
período
setor
filtros
formato
gerar
baixar
```

Mostrar histórico se API suportar.

Formatos somente se backend realmente oferecer:

``` text
CSV
XLSX
PDF
HTML
```

Não inventar PDF se não existir.

------------------------------------------------------------------------

# 25. AUDITORIA

Acesso apenas autorizado.

Tela:

``` text
data/hora
usuário
ação
recurso
empresa
detalhes
```

Filtros:

``` text
período
usuário
ação
recurso
```

Paginação server-side.

------------------------------------------------------------------------

# 26. ASSISTENTE INDUSTRIAL IA

Criar uma experiência premium de chat integrada à TASK 15.

Layout:

``` text
┌─────────────────────────────────────────────────────────┐
│ ✦ Assistente Industrial                                │
│ Pergunte sobre produção, paradas e indicadores.        │
│                                                         │
│ Você: Quanto a Pintura produziu este mês?              │
│                                                         │
│ IA: A Pintura produziu ...                              │
│     Período: ...                                        │
│     Qualidade dos dados: ...                            │
│     Fontes internas: ...                                │
│                                                         │
│ [ Digite sua pergunta...                         ] [➤] │
└─────────────────────────────────────────────────────────┘
```

Mostrar quando fornecido:

``` text
status
período utilizado
avisos
qualidade
fontes internas
```

Tratar:

``` text
RESPONDIDO
SEM_DADOS
SEM_PERMISSAO
PRECISA_CLARIFICACAO
INDISPONIVEL
```

Nunca exibir chain-of-thought.

Sugestões iniciais:

``` text
Quanto a Pintura produziu hoje?
Qual setor teve mais paradas?
Compare este mês com o anterior.
Como está o desempenho da Solda?
```

------------------------------------------------------------------------

# 27. CONFIGURAÇÕES

Mostrar somente configurações suportadas:

``` text
perfil
preferências visuais locais
empresa/contexto quando aplicável
informações da aplicação
```

Configurações administrativas dependem de permissão e API real.

------------------------------------------------------------------------

# 28. FILTROS

Criar FilterBar reutilizável.

Comportamento:

``` text
período
setor
turno
funcionário
peça
máquina
```

Filtros devem:

``` text
sincronizar estado da tela
atualizar API
permitir limpar
mostrar filtros ativos
```

Evitar duplicar lógica em cada página.

------------------------------------------------------------------------

# 29. TABELAS

Padrão:

``` text
paginação server-side
sort seguro
loading skeleton
empty state
erro
hover
ações
responsividade
```

Nunca carregar todos os registros para paginar no browser.

------------------------------------------------------------------------

# 30. GRÁFICOS

Escolher UMA biblioteca mantida e compatível com Angular.

Criar wrapper/componentes comuns.

Tipos:

``` text
line
area
bar
horizontal bar
donut somente quando adequado
```

Todos:

``` text
Premium Dark
responsivos
tooltip
legenda
empty state
loading
```

------------------------------------------------------------------------

# 31. LOADING

Evitar spinner gigante em todas as páginas.

Usar skeletons que preservam o layout:

``` text
cards
gráficos
tabelas
```

------------------------------------------------------------------------

# 32. EMPTY STATES

Exemplos:

``` text
Nenhuma produção encontrada neste período.
Nenhuma parada registrada.
Nenhum relatório disponível.
```

Sempre oferecer ação útil quando aplicável.

------------------------------------------------------------------------

# 33. ERROS

Componente padronizado:

``` text
Não foi possível carregar os dados.

[Tentar novamente]

Código de suporte: ...
```

Não mostrar erro Java bruto.

------------------------------------------------------------------------

# 34. TOASTS

Utilizar para ações:

``` text
sucesso
erro
aviso
informação
```

Evitar excesso.

------------------------------------------------------------------------

# 35. RESPONSIVIDADE

Prioridade:

``` text
1920 desktop
1366 notebook
tablet
mobile
```

Desktop/notebook são prioridade industrial.

No mobile:

``` text
sidebar → drawer
cards → grid reduzido
tabelas → estratégia responsiva/scroll controlado
gráficos → altura adaptada
```

------------------------------------------------------------------------

# 36. ACESSIBILIDADE

Implementar:

``` text
labels
focus
teclado
contraste
aria-label
aria-expanded
aria-current
sem depender apenas de cor
```

------------------------------------------------------------------------

# 37. PERFORMANCE

Aplicar:

``` text
lazy loading
code splitting
trackBy/track
OnPush/signals quando adequado
debounce de busca
cancelamento de requests obsoletos
paginação server-side
```

Evitar múltiplas chamadas idênticas.

------------------------------------------------------------------------

# 38. HTTP E CONFIGURAÇÃO

Centralizar:

``` text
apiBaseUrl
timeouts quando aplicável
```

Não colocar URLs em componentes.

Criar proxy dev se útil:

``` text
/api → backend
```

------------------------------------------------------------------------

# 39. MODELOS

Gerar interfaces/types a partir dos contratos reais quando possível.

Separar:

``` text
API DTO
view model
UI state
```

Não reaproveitar cegamente uma interface para tudo.

------------------------------------------------------------------------

# 40. ESTADO

Não adicionar NgRx apenas por padrão.

Usar solução proporcional:

``` text
signals/services
```

Se complexidade real justificar store global, documentar decisão antes.

------------------------------------------------------------------------

# 41. PERMISSÕES NO MENU

Menu deve adaptar-se ao usuário.

Exemplo:

``` text
sem AUDITORIA_VISUALIZAR
→ Auditoria não aparece
```

Mas tentativa direta pela URL também deve ser tratada e backend continua
protegendo.

------------------------------------------------------------------------

# 42. FINANCEIRO

Dados de valor industrial só aparecem com permissão real.

Não renderizar brevemente antes da verificação.

Não inferir valores escondidos por cálculos client-side.

------------------------------------------------------------------------

# 43. CORRELATION ID

Quando erro do backend contiver correlation ID:

``` text
mostrar ao usuário em contexto técnico
```

Facilitar copiar código de suporte.

------------------------------------------------------------------------

# 44. DATAS E NÚMEROS

Padronizar locale conforme requisitos do projeto.

Para pt-BR:

``` text
1.234,56
29/09/2026
R$ quando moeda for BRL
```

Não converter moeda automaticamente sem regra do backend.

------------------------------------------------------------------------

# 45. STATUS

Criar badges consistentes:

``` text
Ativo
Inativo
Processando
Concluído
Falha
Parcial
Disponível
Indisponível
```

------------------------------------------------------------------------

# 46. ÍCONES

Usar uma biblioteca consistente.

Não misturar vários packs.

Ícones nunca substituem labels importantes sem tooltip/aria-label.

------------------------------------------------------------------------

# 47. ANIMAÇÕES

Usar transições sutis:

``` text
hover
sidebar
modal
toast
```

Sem animações chamativas em ambiente industrial.

------------------------------------------------------------------------

# 48. TESTES UNITÁRIOS

Cobrir:

``` text
AuthService
guards
interceptors
formatters
componentes UI críticos
filtros
paginação
tratamento de erros
permissões
```

------------------------------------------------------------------------

# 49. TESTES DE INTEGRAÇÃO

Cobrir:

``` text
login → dashboard
dashboard → setor
setor → funcionário
importação → resultado
relatório → geração
assistente → resposta
logout
```

Usar mocks contratuais quando necessário.

------------------------------------------------------------------------

# 50. E2E DO FRONTEND

Criar cenário E2E com ferramenta adequada já existente ou uma única
escolha justificada.

Fluxo principal:

``` text
login
→ dashboard
→ filtrar período
→ abrir setor
→ abrir funcionário
→ importar planilha de homologação
→ visualizar resultado
→ gerar relatório
→ abrir assistente
→ logout
```

Não depender de API externa de IA; utilizar ambiente controlado.

------------------------------------------------------------------------

# 51. TESTES DE PERMISSÃO

Testar:

``` text
Dono
Administrador
Gestor de setor
Usuário geral
```

Adaptar aos perfis reais.

Validar:

``` text
menu
rota
dados
ações
financeiro
auditoria
```

------------------------------------------------------------------------

# 52. TESTES VISUAIS MANUAIS

Validar:

``` text
1920x1080
1366x768
tablet
mobile
```

Verificar:

``` text
overflow
cards
tabelas
gráficos
sidebar
modais
```

------------------------------------------------------------------------

# 53. BUILD

Executar comandos reais:

``` powershell
npm install
npm test
npm run build
```

Executar lint se configurado.

Nenhum erro de TypeScript.

------------------------------------------------------------------------

# 54. DOCUMENTAÇÃO

Criar:

``` text
frontend/README.md
docs/FRONTEND_ARQUITETURA.md
docs/FRONTEND_DESIGN_SYSTEM.md
docs/FRONTEND_INTEGRACAO_API.md
docs/FRONTEND_HOMOLOGACAO.md
```

------------------------------------------------------------------------

# 55. README FRONTEND

Documentar:

``` text
pré-requisitos
instalação
execução
build
testes
environments
proxy
estrutura
integração backend
```

------------------------------------------------------------------------

# 56. DOCUMENTO DE DESIGN SYSTEM

Registrar:

``` text
cores
tipografia
spacing
radius
shadows
componentes
estados
gráficos
tabelas
responsividade
```

------------------------------------------------------------------------

# 57. DOCUMENTO DE INTEGRAÇÃO

Criar matriz:

``` text
Feature | Endpoint | Método | Permissão | Status
```

------------------------------------------------------------------------

# 58. HOMOLOGAÇÃO

Criar checklist:

``` text
[ ] Login
[ ] Logout
[ ] Dashboard
[ ] Produção
[ ] Setores
[ ] Funcionários
[ ] Peças
[ ] Máquinas
[ ] Importações
[ ] Paradas
[ ] Metas
[ ] Desempenho
[ ] Qualidade
[ ] Relatórios
[ ] Auditoria
[ ] Assistente
[ ] Configurações
[ ] Permissões
[ ] Responsividade
[ ] Acessibilidade
[ ] Erros
[ ] Build
```

------------------------------------------------------------------------

# 59. CRITÉRIOS DE ACEITE FINAL

O frontend só pode ser considerado concluído quando:

1.  Backend V1 tiver sido analisado.
2.  Nenhum endpoint importante tiver sido inventado.
3.  Arquitetura estiver organizada por features.
4.  Premium Dark estiver consistente.
5.  Design System estiver centralizado.
6.  Login estiver integrado.
7.  Logout funcionar.
8.  Guards funcionarem.
9.  Interceptors funcionarem.
10. Permissões forem respeitadas.
11. Dashboard usar dados reais.
12. Produção funcionar.
13. Setores funcionarem.
14. Drill-down de setor funcionar.
15. Funcionários funcionarem.
16. Drill-down de funcionário funcionar.
17. Peças funcionarem.
18. Máquinas funcionarem.
19. Importação Excel funcionar.
20. Paradas funcionarem.
21. Metas funcionarem.
22. Desempenho funcionar.
23. Qualidade funcionar.
24. Relatórios funcionarem.
25. Auditoria funcionar para autorizados.
26. Assistente IA/RAG estiver integrado.
27. Estados IA forem tratados.
28. Configurações suportadas funcionarem.
29. Loading states existirem.
30. Empty states existirem.
31. Error states existirem.
32. 401/403/429/500/503 forem tratados.
33. Correlation ID for apresentado quando disponível.
34. Paginação server-side for respeitada.
35. Dados financeiros forem protegidos.
36. Responsividade passar.
37. Acessibilidade básica passar.
38. Não houver secrets.
39. Não houver dados fake permanentes.
40. Não houver erros TypeScript.
41. Testes passarem.
42. E2E principal passar.
43. Build de produção passar.
44. Documentação existir.
45. Backend continuar passando seus testes essenciais.

------------------------------------------------------------------------

# 60. ENTREGA ESPERADA DO CODEX

Ao terminar, apresentar um relatório:

## Arquitetura

``` text
árvore de pastas
features
core
shared
layout
```

## OpenSpec

``` text
change
proposal
design
tasks
specs
validação
```

## Backend analisado

Listar contratos utilizados e divergências encontradas.

## Telas implementadas

Listar cada tela e seu status.

## Integração

Para cada feature:

``` text
endpoint
método
permissão
```

## Design System

Informar componentes e tokens criados.

## Segurança

Informar:

``` text
auth
guards
permissões
financeiro
auditoria
token
erros
```

## Testes

Informar:

``` text
unitários
integração
E2E
permissões
```

## Build

Apresentar resultado real:

``` text
npm test
npm run build
```

## Homologação

Resultado:

``` text
FRONTEND: GO
```

ou:

``` text
FRONTEND: NO-GO
```

Não declarar GO se funcionalidades obrigatórias estiverem quebradas.

## Pendências

Listar somente pendências reais.

------------------------------------------------------------------------

# 61. ORDEM INTERNA DE EXECUÇÃO

Mesmo sendo uma única MASTER TASK, implementar nesta ordem:

``` text
1. Analisar Backend V1
2. OpenSpec
3. Estrutura Angular
4. Design System
5. Login/Auth
6. Shell/Sidebar/Topbar
7. Dashboard
8. Produção
9. Setores
10. Funcionários
11. Peças
12. Máquinas
13. Importações
14. Paradas
15. Metas
16. Desempenho
17. Qualidade
18. Relatórios
19. Auditoria
20. Assistente IA/RAG
21. Configurações
22. Responsividade
23. Acessibilidade
24. Testes
25. E2E
26. Build
27. Documentação
28. Homologação
```

Após cada bloco importante:

``` text
compilar
testar
corrigir
só então continuar
```

------------------------------------------------------------------------

# 62. RESULTADO VISUAL ESPERADO

O produto final deve parecer um ERP industrial premium, com foco em
leitura rápida pelo dono/gestor:

``` text
                 METALÚRGICA ERP
────────────────────────────────────────────────────────────

Visão Geral                           Setembro / 2026

┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ PRODUÇÃO    │ │ DESEMPENHO  │ │ PARADAS     │ │ META        │
│  dados API  │ │  dados API  │ │  dados API  │ │  dados API  │
│ tendência   │ │ tendência   │ │ tendência   │ │ atingimento │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘

┌──────────────────────────────────────┐ ┌─────────────────────┐
│ EVOLUÇÃO DA PRODUÇÃO                 │ │ SETORES             │
│                                      │ │                     │
│           ╭───────╮                  │ │ Pintura  ███████    │
│      ╭────╯       ╰────╮             │ │ Laser    ██████     │
│ ─────╯                 ╰────         │ │ Solda    █████      │
│                                      │ │ KTL      ████       │
└──────────────────────────────────────┘ └─────────────────────┘

┌─────────────────────────┐ ┌─────────────────────────────────┐
│ PRINCIPAIS PARADAS      │ │ QUALIDADE DOS DADOS            │
│ dados API               │ │ cobertura / alertas            │
└─────────────────────────┘ └─────────────────────────────────┘

✦ Assistente Industrial
“Pergunte sobre produção, setores, paradas ou desempenho...”
```

**Nenhum valor dessa ilustração deve ser hardcoded.**

------------------------------------------------------------------------

# 63. PRINCÍPIO FINAL

O frontend não deve ser apenas bonito.

Ele deve ser:

``` text
BONITO
+
INTEGRADO
+
SEGURO
+
RÁPIDO
+
CONSISTENTE
+
FÁCIL DE ENTENDER
+
PREPARADO PARA CRESCER
```

A prioridade é entregar ao dono da metalúrgica uma visão executiva clara
e, ao mesmo tempo, permitir que usuários autorizados explorem os
detalhes de produção sem transformar o sistema em uma coleção confusa de
tabelas.
