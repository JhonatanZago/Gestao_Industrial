# MASTER TASK FRONTEND — Premium Dark Excel → Dashboard

## Objetivo
Refatorar o frontend do Sistema de Gestão e Inteligência de Produção da Metalúrgica para o core homologado:

```text
Excel por setor → Importação → Produção → Dashboard Geral
→ Dashboard do Setor → Funcionário/Peça → Relatório
```

O Dashboard é o produto principal. O Backend V1 homologado é a fonte de verdade.

## 1. Antes de alterar
1. Ler `simplificar-sistema-dashboard-excel`.
2. Ler OpenAPI/Swagger e contratos reais.
3. Ler `docs/ESCOPO_SIMPLIFICADO.md`, `PADRAO_PLANILHA_PRODUCAO.md` e `FLUXO_EXCEL_DASHBOARD.md`.
4. Inspecionar frontend atual.
5. Rodar os 15 testes frontend atuais e build.
6. Preservar os 154 testes backend homologados.
7. Não inventar endpoints, DTOs, KPIs ou dados.
8. Não reintroduzir módulos secundários no menu.

## 2. OpenSpec
Criar:
```text
frontend-premium-dark-excel-dashboard
```

Fases internas:
```text
análise → design system → shell → dashboard → setores
→ funcionário/peça → importação → relatórios → UX → testes → homologação
```

## 3. Navegação principal
A sidebar deve conter somente:
```text
▣ Visão Geral
▦ Setores
⇧ Importar Excel
▤ Relatórios
```

Funcionários e peças são drill-downs contextuais. Máquinas, metas, desempenho, qualidade avançada, auditoria e IA/RAG não devem aparecer no fluxo principal.

## 4. Premium Dark
Criar visual industrial, executivo, minimalista e moderno:
- fundo grafite/preto profundo;
- sidebar mais escura;
- cards elevados discretamente;
- bordas sutis;
- texto quase branco/cinza frio;
- verde/esmeralda como destaque;
- sucesso/atenção/erro com cores sem exagero;
- sem neon, 3D ou aparência gamer.

Centralizar design tokens:
```text
--bg-app --bg-sidebar --bg-topbar --bg-surface --bg-surface-raised
--border-subtle --text-primary --text-secondary --text-muted
--accent --accent-hover --success --warning --danger --info
--radius-sm --radius-md --radius-lg --radius-xl --shadow-card
```

## 5. Componentes compartilhados
Criar/reutilizar:
```text
Button, IconButton, Card, StatCard, Badge, Input, Select,
DateRange, SearchInput, Table, Pagination, ChartCard, Tooltip,
Modal, Toast, Skeleton, EmptyState, ErrorState, PageHeader, FilterBar
```

Não duplicar CSS por página.

## 6. Shell
Desktop:
```text
┌──────────────┬─────────────────────────────────────────────┐
│ METALÚRGICA  │ TOPBAR / contexto / usuário                │
│              ├─────────────────────────────────────────────┤
│ Visão Geral  │                                             │
│ Setores      │                CONTEÚDO                     │
│ Importar     │                                             │
│ Relatórios   │                                             │
└──────────────┴─────────────────────────────────────────────┘
```

Sidebar responsiva/drawer no mobile. Preservar autenticação, perfil e logout.

## 7. Visão Geral
Home:
```text
VISÃO GERAL                         [Período]

[ Produção Total ] [ Setores ] [ Funcionários ] [ Peças ]

┌─────────────────────────────────┐ ┌────────────────────────┐
│ EVOLUÇÃO DA PRODUÇÃO            │ │ PRODUÇÃO POR SETOR     │
│ gráfico temporal                │ │ barras/lista           │
└─────────────────────────────────┘ └────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│ RESUMO DOS SETORES — cards clicáveis                      │
└────────────────────────────────────────────────────────────┘
```

Somente indicadores homologados:
```text
produção total
setores com dados
funcionários envolvidos
códigos de peças produzidos
```
Duração/comparação somente se API fornecer. Proibido inventar OEE, lucro, faturamento, parada ou eficiência.

## 8. Setores
Buscar dinamicamente da API; nunca hardcodar nomes.

Cards compactos podem mostrar produção, funcionários e peças quando disponíveis. Clique abre `/app/setores/:id`.

## 9. Dashboard do Setor
Mostrar:
```text
nome
período
produção total
funcionários envolvidos
peças
evolução
produção por funcionário
produção por peça
duração registrada, se disponível
```

Funcionários e peças são clicáveis.

## 10. Funcionário
Fluxo:
```text
Setores → Pintura → 00147
```
Mostrar:
```text
código, setor, período, produção total, peças produzidas,
duração registrada, evolução e registros resumidos
```
Nunca remover zeros à esquerda nem criar avaliação subjetiva.

## 11. Peça
Fluxo:
```text
Setores → Pintura → PEC-001
```
Mostrar:
```text
código, setor, período, quantidade total, evolução,
funcionários relacionados quando autorizado
```

## 12. Importar Excel
Tela prioritária:
```text
IMPORTAR PRODUÇÃO

Setor
[Pintura a pó ▼]

┌─────────────────────────────────────┐
│      Arraste o Excel aqui           │
│                ou                   │
│       [Selecionar arquivo]          │
│              XLSX                   │
└─────────────────────────────────────┘

              [ IMPORTAR ]
```

Setores vêm da API.

Mostrar padrão oficial:
```text
Data
Código Funcionário
Código Peça
Quantidade
Hora Início
Hora Fim
```

Disponibilizar `PRODUCAO_PADRAO.xlsx` se tecnicamente apropriado, sem inventar endpoint.

Estados:
```text
selecionado, validando, enviando, processando,
sucesso, parcial, erro, duplicidade
```

Evitar clique duplicado/reimportação.

## 13. Resultado da importação
Exibir resposta real:
```text
arquivo
setor
registros lidos
importados
com erro
status
```

Ações:
```text
[Ver erros] [Ir para o setor]
```

Erros:
```text
Linha | Campo | Valor | Motivo
```
Nunca stack trace.

## 14. Relatórios
Tela simples:
```text
Período [início] → [fim]
Setor [Todos ▼]
[GERAR RESUMO]
```

Resultado:
```text
produção total
produção por setor
produção por funcionário
produção por peça
evolução
```
Exportação somente para formatos realmente suportados.

## 15. Filtros
Componente reutilizável. Priorizar data inicial/final; setor no relatório. Persistir filtros relevantes na query string quando adequado.

## 16. Gráficos
Uma única biblioteca Angular mantida. Dark mode, responsivo, tooltip, loading e empty state. Sem 3D.

## 17. Estados de UI
Toda tela deve ter:
```text
loading
success
empty
error
forbidden quando aplicável
```
Usar skeletons em cards/gráficos/tabelas.

Erro padrão:
```text
Não foi possível carregar os dados.
[Tentar novamente]
Código de suporte: <correlationId>
```

## 18. Segurança
Preservar/reutilizar:
```text
AuthService
auth interceptor
auth guard
permission guard quando aplicável
logout
401/403
```
Não guardar senha, logar JWT ou hardcodar secret.

## 19. Rotas
Conceitualmente:
```text
/login
/app/dashboard
/app/setores
/app/setores/:setorId
/app/setores/:setorId/funcionarios/:codigo
/app/setores/:setorId/pecas/:codigo
/app/importacoes
/app/relatorios
```
Adaptar ao router existente.

## 20. Responsividade e acessibilidade
Prioridade: 1920x1080, 1366x768, tablet e mobile. Sidebar vira drawer. Garantir contraste, foco, labels, teclado, aria-label e aria-current.

## 21. Performance
Aplicar conforme arquitetura:
```text
lazy loading
code splitting
signals/services
debounce
cancelamento de requests obsoletos
track/trackBy
paginação server-side
```
Não adicionar NgRx sem necessidade real.

## 22. pt-BR
Padronizar:
```text
29/09/2026
1.234
1.234,56
duração legível
```
`00147` deve permanecer `00147`.

## 23. Testes
Preservar testes existentes ainda válidos e adicionar cobertura para:
```text
sidebar simplificada
Dashboard Geral
setores dinâmicos
Dashboard Setor
funcionário
peça
upload
erro/duplicidade
relatório
loading/empty/error
401/403
correlationId
00147 preservado
```

Não excluir testes só para deixar pipeline verde.

## 24. E2E obrigatório
Usar massa homologada:
```text
Total = 350
00147 = 200
00152 = 150
PEC-001 = 270
PEC-002 = 80
Duração = 185 min
```

Fluxo:
```text
Login
→ importar PRODUCAO_PADRAO.xlsx para Pintura a pó
→ confirmar resultado
→ Visão Geral: 350
→ abrir Pintura
→ confirmar funcionários
→ 00147: 200
→ PEC-001: 270
→ gerar resumo
→ confirmar reconciliação
→ logout
```

Usar ambiente E2E limpo/determinístico; não desabilitar proteção de duplicidade.

## 25. Regressão
Referência atual:
```text
Backend: 154 testes aprovados
Frontend: 15 testes aprovados
```
Contagens podem mudar legitimamente, mas explicar. Backend não pode ser quebrado pelo frontend.

## 26. Documentação
Criar/revisar:
```text
frontend/README.md
docs/FRONTEND_PREMIUM_DARK.md
docs/FRONTEND_INTEGRACAO_CORE.md
docs/FRONTEND_HOMOLOGACAO.md
```

## 27. Ordem obrigatória
```text
1 análise/contratos
2 baseline
3 OpenSpec
4 navegação
5 design system
6 shell
7 Visão Geral
8 Setores
9 Dashboard Setor
10 Funcionário
11 Peça
12 Importação
13 Relatórios
14 estados UI
15 responsividade/acessibilidade
16 testes
17 E2E
18 build
19 documentação
20 homologação
```

Após cada bloco: compilar, testar, corrigir e só então continuar.

## 28. Critérios de aceite
Somente concluir se:
1. OpenSpec validar.
2. Sidebar tiver apenas quatro áreas principais.
3. Premium Dark estiver consistente.
4. Dashboard consumir dados reais.
5. Setores forem dinâmicos.
6. Dashboard Setor funcionar.
7. Funcionário/Peça forem drill-downs.
8. Importação exigir setor e XLSX.
9. Resultado/erros forem claros.
10. Duplicidade continuar protegida.
11. Relatório reconciliar com Dashboard.
12. Loading/empty/error existirem.
13. 401/403/correlationId forem tratados.
14. `00147` continuar intacto.
15. Nenhum KPI fictício existir.
16. Responsividade/acessibilidade básica passarem.
17. Testes passarem.
18. E2E passar.
19. Build frontend passar.
20. Regressão backend relevante passar.
21. Documentação estiver atualizada.

## 29. Entrega final do Codex
Apresentar:
- OpenSpec e validação;
- árvore frontend;
- telas implementadas;
- tabela `Tela | Endpoint real | Método | Status`;
- tokens/componentes do Design System;
- testes e contagens;
- E2E com os totais homologados;
- resultado real de `npm test` e `npm run build`;
- regressão backend;
- pendências reais.

Resultado obrigatório:
```text
FRONTEND EXCEL → DASHBOARD: GO
```
ou:
```text
FRONTEND EXCEL → DASHBOARD: NO-GO
```

Não declarar GO com integração, E2E ou build quebrados.

## 30. Princípio final

O usuário da metalúrgica deve conseguir:

```text
Escolher setor
→ importar Excel
→ abrir Dashboard
→ entender a produção
→ clicar no setor
→ consultar funcionário/peça
→ gerar resumo
```

sem navegar por dezenas de módulos. O frontend deve ser bonito, mas principalmente simples, rápido, consistente e fiel aos dados reais.
