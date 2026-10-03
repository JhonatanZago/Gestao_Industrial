# CODEX MASTER — PREMIUM DARK PIXEL-ACCURATE

## Objetivo
Reconstruir VISUALMENTE o frontend Angular existente, preservando integralmente backend, APIs, regras, autenticação, Excel → Dashboard, relatórios e dados reais. Não fazer apenas ajustes cosméticos.

## OpenSpec
Criar change: `redesign-dashboard-premium-dark-pixel-accurate`.

## Antes de editar
1. Ler AGENTS.md, openspec, specs do core Excel→Dashboard e frontend atual.
2. Inspecionar package.json, router, services, DTOs, componentes, estilos e biblioteca de gráficos.
3. Rodar baseline de `npm test` e `npm run build`.
4. Mapear endpoints reais usados.
5. Implementar; não parar na análise.
6. Ao final abrir a aplicação e validar visualmente.

## Proibições
- Não trocar Angular.
- Não criar outro frontend.
- Não alterar backend por conveniência visual.
- Não inventar endpoint/KPI/dado.
- Não hardcodar números da referência.
- Não criar OEE, lucro, faturamento, eficiência ou parada sem dados reais.
- Não perder zeros como `00147`.
- Não usar P/S/F/R como ícones.
- Não declarar sucesso apenas porque compilou.

## Layout desktop obrigatório

```text
┌──────────────────┬──────────────────────────────────────────────────────────────┐
│ METALÚRGICA      │ Visão Geral                    [Setembro/2026] [↻ Atualizar]│
│ Produção         │ Acompanhe a produção da empresa                            │
│                  │                                                            │
│ ● Visão Geral    │ [Produção] [Setores] [Funcionários] [Peças]              │
│   Setores        │                                                            │
│   Importar Excel │ ┌───────────────────────────────┬────────────────────────┐ │
│   Relatórios     │ │ EVOLUÇÃO DA PRODUÇÃO         │ PRODUÇÃO POR SETOR     │ │
│                  │ │                               │ Pintura █████ 31,9%   │ │
│                  │ │          gráfico              │ Solda   ████  23,9%   │ │
│                  │ │                               │ KTL     ███   18,8%   │ │
│                  │ └───────────────────────────────┴────────────────────────┘ │
│                  │                                                            │
│                  │ [Top funcionários] [Principais peças] [Resumo período]    │
│ usuário / sair   │                                                            │
└──────────────────┴──────────────────────────────────────────────────────────────┘
```

Em 1366×768 a primeira dobra deve mostrar header + KPIs + pelo menos 65% do grid gráfico/setores.

## Medidas
Desktop >=1440:
- sidebar: 232px;
- padding horizontal: 28px;
- padding topo: 24px;
- gap geral: 20px;
- KPI grid: `repeat(4,minmax(0,1fr))`, gap 16px;
- principal: `minmax(0,1.9fr) minmax(300px,.8fr)`, gap 18px;
- inferior: `repeat(3,minmax(0,1fr))`, gap 18px.
- KPI altura alvo 120–135px;
- gráfico principal 330–370px.

1024–1439: sidebar 216px; padding 20–24px; adaptar KPIs sem esmagar.
Tablet: drawer, KPIs 2×2, painéis empilhados.
Mobile: drawer, 1×4 ou 2×2, gráficos 100%, sem overflow da página.

## Tokens visuais
Centralizar em arquivo global/design tokens:

```css
:root {
 --bg-app:#07100f;
 --bg-sidebar:#091312;
 --bg-elevated:#0d1917;
 --bg-card:#0f1d1a;
 --bg-card-hover:#12231f;
 --border:rgba(148,181,171,.13);
 --border-hover:rgba(73,224,174,.30);
 --text-primary:#edf7f3;
 --text-secondary:#a4b7b1;
 --text-muted:#70857f;
 --accent:#49e0ae;
 --accent-strong:#2fd19a;
 --accent-soft:rgba(73,224,174,.10);
 --danger:#f26d78;
 --warning:#e8b75f;
 --info:#67a9ff;
 --radius-sm:8px;
 --radius-md:12px;
 --radius-lg:16px;
 --radius-xl:20px;
 --shadow-card:0 10px 35px rgba(0,0,0,.16);
}
```

Ajuste mínimo permitido por contraste.

## Tipografia
Preferir Inter se já disponível; senão stack de sistema. Não usar fonte monoespaçada global.

```text
Page title 30px/700
Section title 17px/650
KPI 32–36px/700
Body 14px/400
Label 12px/550
Caption 12px/400
```

## Sidebar
Implementar de verdade:
```text
[Factory] METALÚRGICA
          Produção

[Home]   Visão Geral
[Factory] Setores
[Upload] Importar Excel
[File]   Relatórios

----------------
Usuário
Sair
```
Item 42–44px, radius 10px, ícone SVG 18–20px, ativo com accent-soft, hover suave. Rodapé fixo visualmente. Drawer em mobile. Usar uma única biblioteca leve de ícones já existente, se possível.

## Header
Remover o container gigante de período. Header 70–86px:
```text
Visão Geral
Acompanhe a produção da empresa            [Setembro/2026 ▼] [↻ Atualizar]
```
Atualizar deve ter estados `normal → atualizando → atualizado`, desabilitando clique duplicado.

## KPIs
4 cards compactos:
- Produção total — ícone Package/Boxes.
- Setores com dados — Factory/Building.
- Funcionários — Users.
- Peças — Box/Component.

Padding 18–20px, ícone 36×36 em accent-soft, radius 14–16px, border sutil. Hover `translateY(-2px)` + border accent + sombra, 180ms. Comparação percentual SOMENTE se API fornecer.

## Evolução da produção
Card no lado esquerdo do grid. Linha esmeralda 2–3px, área com gradiente transparente, grid sutil, eixos legíveis, tooltip customizado, ponto no hover, animação 350–500ms. Não distorcer dados com suavização excessiva.

Tooltip conceitual:
```text
18/09/2026
3.420 peças
```
Valores reais apenas.

## Produção por setor
Obrigatoriamente à direita do gráfico:
```text
Produção por setor                 Ver todos →

Pintura a pó                 8.450
████████████████████          31,9%

Solda                        6.320
███████████████               23,9%
```
Ordenar desc; percentual calculado do total real; 5–7 itens; clique abre setor; `Ver todos` abre Setores; nomes dinâmicos.

## Linha inferior
Três painéis:

### Produção por funcionário
Top 5 por quantidade real. Código + quantidade + microbarra. Clique apenas se rota/contexto puder ser determinado corretamente. Não chamar de “melhor funcionário”.

### Principais peças
Top 5 por quantidade real. Código + quantidade + microbarra.

### Resumo do período
Somente métricas deriváveis corretamente: dias com produção, média diária, maior e menor produção diária. Omitir o que não puder ser calculado.

## Microinterações
- cards 180ms;
- nav 160ms;
- buttons 160ms;
- tooltip 120–180ms;
- gráfico <=500ms;
- skeleton discreto;
- respeitar `prefers-reduced-motion`.

## Estados
Cada widget: loading, success, empty, error. Skeleton mantém dimensões finais. Empty deve orientar o usuário. Error deve ter “Tentar novamente” e correlationId quando disponível.

## Página Setores
Mesmo Design System. Busca + cards compactos clicáveis, com nome, produção e métricas reais disponíveis, microvisual e seta. Nada hardcoded.

## Dashboard do Setor
```text
← Setores
Pintura a pó                              [Período]
[Produção] [Funcionários] [Peças] [Duração*]
[Evolução]
[Por funcionário] [Por peça]
```
`Duração` só se disponível. Drill-downs preservados.

## Importar Excel
Redesenhar em etapas:
```text
1 Setor ─── 2 Arquivo ─── 3 Validação ─── 4 Concluído
```
Dropzone premium, XLSX, modelo oficial, arquivo selecionado, processamento, sucesso, parcial, erro e duplicidade. Resultado:
```text
✓ Importação concluída
381 importados | 4 erros
[Ver erros] [Abrir setor]
```
Dados reais; preservar validações.

## Relatórios
```text
Relatórios
Gere um resumo da produção
[Data inicial] [Data final] [Setor ▼] [Gerar]
[Resumo] [Por setor] [Por funcionário] [Por peça]
```
Exportação somente se suportada.

## Componentização
Criar/reutilizar equivalentes a:
`AppShell`, `Sidebar`, `PageHeader`, `PeriodSelector`, `RefreshButton`, `StatCard`, `DashboardPanel`, `ProductionTrendChart`, `SectorProductionPanel`, `RankingPanel`, `SummaryPanel`, `Skeleton`, `EmptyState`, `ErrorState`.
Adaptar nomes ao padrão do projeto. Não criar componente monolítico.

## Dados
A referência define layout, não valores. Todos os números, setores, funcionários, peças, datas e gráficos devem vir dos services/DTOs/APIs reais. Não criar mock permanente.

## Validação visual obrigatória
Após implementar:
1. executar frontend;
2. abrir Visão Geral em 1366×768;
3. capturar screenshot se ambiente permitir;
4. comparar com esta composição;
5. corrigir até: sidebar presente; header compacto; 4 KPIs; gráfico/setores lado a lado; 3 cards inferiores; ícones reais; densidade boa; sem vazios gigantes;
6. repetir em 1920×1080;
7. NÃO encerrar só porque build passou.

## Testes
Preservar testes válidos. Cobrir sidebar, rota ativa, KPIs, período, atualizar, setor, Ver todos, rankings, loading/empty/error e `00147`.

Executar:
```bash
npm test
npm run build
```
Executar E2E existente se configurado.

## Critérios finais
```text
[ ] Sidebar premium
[ ] Header compacto
[ ] Período integrado
[ ] Atualizar interativo
[ ] 4 KPIs premium
[ ] Ícones SVG reais
[ ] Evolução refinada
[ ] Produção por setor ao lado
[ ] Top funcionários
[ ] Top peças
[ ] Resumo do período
[ ] Setores redesenhado
[ ] Dashboard Setor redesenhado
[ ] Importação redesenhada
[ ] Relatórios redesenhado
[ ] Loading/empty/error
[ ] 1366×768 validado
[ ] 1920×1080 validado
[ ] Tablet/mobile
[ ] Acessibilidade básica
[ ] Dados reais
[ ] APIs preservadas
[ ] Testes verdes
[ ] Build verde
[ ] Validação visual feita
```

## Resposta final do Codex
```text
## REDESIGN PREMIUM DARK
Status: GO | NO-GO

### Arquivos alterados
...
### Componentes
...
### Endpoints preservados
...
### Bibliotecas
...
### Testes
npm test: ...
npm run build: ...
E2E: ...
### Validação visual
1366x768: PASS/FAIL
1920x1080: PASS/FAIL
### Diferenças restantes
...
### Screenshots
...
```

Só declarar `REDESIGN PREMIUM DARK: GO` quando estiver visualmente validado, integrado, testado e compilando.

## Princípio final
Isto é uma reconstrução VISUAL sobre o mesmo sistema funcional. O usuário deve entender imediatamente produção, setores, funcionários, peças, evolução e destaques do período. O resultado deve parecer um produto industrial SaaS profissional, não um protótipo acadêmico.
