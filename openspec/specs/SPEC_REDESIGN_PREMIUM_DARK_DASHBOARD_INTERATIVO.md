# SPEC — Redesign Premium Dark Interativo

## Objetivo
Melhorar o frontend existente sem alterar o core homologado. A referência visual anexada a esta SPEC deve guiar composição, hierarquia e interatividade, mas todos os dados devem continuar vindo das APIs reais.

## OpenSpec
Criar change `redesign-premium-dark-dashboard-interativo`.

## Não quebrar
- Excel → Dashboard homologado.
- Setores dinâmicos.
- Drill-down funcionário/peça.
- Relatórios reconciliados.
- Autenticação/autorização.
- Preservação de códigos como `00147`.
- Testes/build.
- Não inventar OEE, lucro, faturamento, parada ou eficiência.

## Problemas atuais a corrigir
- cabeçalho alto demais;
- container de período excessivamente grande;
- cards KPI vazios;
- letras P/S/F/R no lugar de ícones;
- gráfico isolado ocupando largura inteira;
- pouca informação acima da dobra;
- falta painel por setor;
- falta Top funcionários/peças;
- pouca resposta de hover/click;
- hierarquia tipográfica fraca;
- botão Atualizar desconectado do filtro.

## Sidebar
Desktop premium de 220–250px:
```text
METALÚRGICA
Sistema de Produção

▣ Visão Geral
▦ Setores
⇧ Importar Excel
▤ Relatórios

Perfil
Sair
```
Estado ativo esmeralda discreto, ícones consistentes, hover suave e drawer mobile.

## Header
Compactar para:
```text
[ícone] Visão Geral
        Acompanhe a produção da empresa

                     Período [Setembro / 2026 ▼] [↻ Atualizar]
```
Estados: Atualizar → Atualizando → Atualizado.

## KPIs
Quatro cards compactos:
```text
Produção Total
Setores com dados
Funcionários envolvidos
Peças produzidas
```
Cada card: ícone semântico real, valor, descrição, microvisual discreto, hover. Comparação percentual apenas se API real fornecer.

## Grid principal
```text
┌────────────────────────────────────┬──────────────────────┐
│ Evolução da produção 65–70%        │ Produção por setor  │
│ gráfico temporal                   │ ranking/barras       │
└────────────────────────────────────┴──────────────────────┘
```

### Gráfico
Linha esmeralda + área transparente, pontos discretos, tooltip premium, grid sutil, animação curta, skeleton, empty/error state. Dia/Semana/Mês somente se suportado corretamente.

### Produção por setor
```text
Pintura a pó █████████  8.450 31,9%
Solda        ███████    6.320 23,9%
KTL          █████      4.980 18,8%
```
Ordenar por produção real. Máximo 5–7 na home. `Ver todos` → Setores. Linha clicável → Dashboard do Setor. Setores sempre dinâmicos.

## Segunda linha
```text
┌────────────────────────┬────────────────────────┬──────────────────────┐
│ Produção funcionário   │ Principais peças      │ Resumo do período    │
│ Top 5                  │ Top 5                 │                      │
└────────────────────────┴────────────────────────┴──────────────────────┘
```
Funcionário: código + quantidade, sem ranking subjetivo.
Peça: código + quantidade.
Resumo: dias com produção, média diária, maior/menor produção somente se corretamente deriváveis.

## Interatividade
Hover, foco, tooltip, cursor adequado, transições 150–250ms, navegação em setor/funcionário/peça, `Ver todos`, filtro compartilhado e loading regional. Respeitar `prefers-reduced-motion`.

## Página Setores
Cards compactos e clicáveis, busca e dados reais:
```text
Pintura a pó
8.450 peças
Funcionários: ...
Peças: ...
Ver detalhes →
```

## Dashboard do Setor
Mesmo design:
```text
← Setores
Pintura a pó                         [Período]

[Produção] [Funcionários] [Peças] [Duração, se disponível]

[Evolução da produção]
[Produção por funcionário] [Produção por peça]
```

## Importar Excel
Criar experiência em 4 passos:
```text
1 Selecionar setor → 2 Enviar arquivo → 3 Validar → 4 Concluir
```
Área drag-and-drop, padrão XLSX, modelo oficial, estados de progresso e resultado:
```text
✓ Importação concluída
381 importados | 4 erros
[Ver erros] [Ir para o setor]
```

## Relatórios
```text
Relatórios
Período [ ] → [ ]
Setor [Todos ▼]
[Gerar relatório]
```
Resultado premium em resumo/tabelas. Exportações somente se backend suportar.

## Tipografia
Usar fonte de UI legível. Evitar monoespaçada global.
```text
H1 30–36
H2 18–22
KPI 30–38
Body 14–16
Caption 12–13
```

## Design tokens
Centralizar background, surfaces, border, text, accent esmeralda, success/warning/danger/info, radius e shadow. Radius 12–16px; borda 1px sutil; sombras leves.

## Responsividade
- >=1440: 4 KPIs, grid principal 2 colunas, 3 painéis inferiores.
- 1024–1439: adaptar grid mantendo informação acima da dobra.
- 768–1023: 2 KPIs por linha e painéis empilhados.
- mobile: drawer, cards compactos e gráficos adaptados.

## Acessibilidade
Contraste, foco, teclado, aria-label/current, informação textual junto a gráficos e não depender só de cor.

## Performance
Não duplicar chamadas por widget se payload consolidado atende. Compartilhar estado do período, lazy loading, skeletons e evitar biblioteca pesada desnecessária.

## Estados
Cada painel: `loading | success | empty | error`.
Erro padronizado com tentar novamente e correlationId quando disponível.

## Critérios de aceite
1. Sidebar premium.
2. Header compacto.
3. Período + Atualizar integrados.
4. Ícones reais substituindo letras.
5. KPIs compactos.
6. Gráfico + produção por setor lado a lado.
7. Top funcionários.
8. Top peças.
9. Resumo do período.
10. Hover/tooltip/click.
11. Dados 100% reais.
12. Melhor hierarquia tipográfica.
13. Boa primeira dobra em 1366x768 e 1920x1080.
14. Premium Dark consistente.
15. Sem KPIs fictícios.
16. Setores/importação/relatórios também redesenhados.
17. Testes e build verdes.

## Ordem
```text
baseline → OpenSpec → tokens/tipografia → sidebar/shell → header
→ KPIs → gráfico → setor → painéis inferiores → Setores
→ Dashboard Setor → Importar → Relatórios → responsividade
→ acessibilidade → testes → build → homologação visual
```

## Testes
Cobrir KPIs, período, atualizar, setor clicável, Ver todos, Top funcionários/peças, loading/empty/error, sidebar e preservação de dados reais.
Executar `npm test` e `npm run build`.

## Resultado final
O Dashboard deve ter composição semelhante à referência visual entregue junto desta SPEC, mas sem copiar dados fictícios.

Declarar:
```text
REDESIGN PREMIUM DARK: GO
```
ou `NO-GO`, explicando bloqueios reais.
