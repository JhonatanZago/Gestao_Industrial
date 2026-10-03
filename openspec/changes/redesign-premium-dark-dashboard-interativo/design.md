# Design

## Context

Conforme `proposal.md`, esta mudança é uma camada de experiência sobre o frontend Angular 22 já existente. Hoje o shell ainda está concentrado em `App`, usa os serviços reais de autenticação, dashboard e recursos, possui rotas core para dashboard/setores/importação/relatórios e já tem tokens básicos em `frontend/src/styles.scss`; a composição visual e os estados interativos ainda são incompletos. O change anterior `frontend-premium-dark-excel-dashboard` permanece a referência funcional e não deve ser reescrito.

## Goals / Non-Goals

**Goals:**

- Entregar uma composição Premium Dark responsiva e consistente para todas as telas core.
- Expor no dashboard apenas agregações suportadas pelos payloads reais.
- Compartilhar período, estado de consulta, permissões e navegação contextual entre os painéis.
- Tornar loading, empty, error, retry, foco, tooltip e drill-down observáveis e testáveis.

**Non-Goals:**

- Alterar endpoints, DTOs, regras de importação, autenticação ou autorização do backend.
- Criar métricas de negócio não presentes ou não deriváveis com segurança.
- Reintroduzir módulos secundários na sidebar principal ou ampliar o escopo para uma troca de banco.

## Decisions

1. **Preservar os contratos e serviços existentes.** `AuthService`, guards, interceptor, `DashboardService` e `ResourceService` continuam sendo a fonte de sessão, permissões e dados. Isso evita que o redesign visual reabra os problemas de login/importação e mantém a homologação do Excel.

   Alternativa considerada: criar um BFF ou endpoints de dashboard específicos para a nova tela. Foi descartada porque a SPEC exige dados reais e o backend V1 já possui contratos homologados; divergências serão registradas como bloqueio, não mascaradas no frontend.

2. **Extrair a composição em componentes de apresentação pequenos, mantendo estado de consulta no nível da tela.** O shell, header/período, KPI, gráfico, ranking, estado de painel e drawer devem ser reutilizáveis; a tela coordenadora mantém empresa, permissões, período, carregamento e rotas.

   Alternativa considerada: continuar expandindo `App` monolítico. Foi descartada por aumentar duplicação de CSS, dificultar testes regionais e tornar impossível manter o mesmo período entre painéis sem efeitos acidentais.

3. **Usar os tokens CSS existentes como fonte única e completar seus valores.** Background, surfaces, bordas, texto, esmeralda, estados, raios e sombras serão centralizados em `styles.scss`/tokens de componente, com tipografia de UI legível e sem fonte monoespaçada global.

   Alternativa considerada: estilizar cada tela isoladamente. Foi descartada porque a SPEC exige consistência entre dashboard, setores, importação e relatórios.

4. **Manter o gráfico SVG existente como base de visualização.** Tooltip, pontos, área, grid e texto acessível serão adicionados à abstração atual quando o contrato permitir; uma biblioteca externa só será avaliada se uma necessidade de interação não puder ser atendida pelo SVG.

   Alternativa considerada: instalar uma biblioteca completa de charts. Foi descartada inicialmente por peso, risco de bundle e por não haver requisito que justifique nova dependência.

5. **Modelar estados por painel e não por uma tela global única.** Cada região poderá estar em loading, success, empty ou error sem apagar dados válidos de outras regiões; retry será explícito e operações mutáveis não serão repetidas automaticamente.

6. **Compartilhar filtros e cancelar consultas obsoletas.** O período será uma fonte de estado única, refletida na URL quando aplicável, e mudanças rápidas deverão evitar respostas antigas sobrescrevendo a seleção atual. Requisições serão deduplicadas e o botão Atualizar terá estado ocupado.

7. **Tratar a primeira dobra como restrição de layout.** Em 1366x768 e 1920x1080, a barra compacta, quatro KPIs e a primeira linha de gráfico/setor devem caber sem esconder os dados principais; em larguras menores, os grids colapsam progressivamente e o drawer substitui a sidebar.

## Risks / Trade-offs

- [Payloads atuais podem não conter Top funcionários, Top peças ou série diária] → validar cada campo antes da implementação; exibir indisponibilidade ou solicitar contrato real, nunca fabricar valores.
- [Extração de componentes pode quebrar testes ou bindings do shell existente] → migrar por fatias, manter rotas/serviços e executar testes após cada grupo de componentes.
- [SVG com tooltip e acessibilidade pode exigir mais código] → reutilizar `UiChartComponent`, fornecer tabela/texto equivalente e limitar animações a 150–250 ms.
- [Filtros compartilhados podem gerar consultas repetidas] → centralizar estado, debounce/cancelamento e teste explícito de concorrência.
- [A referência visual pode divergir em tamanhos intermediários] → validar em 1366x768, 1920x1080, tablet e mobile, com critérios de primeira dobra e sem rolagem horizontal.

## Migration Plan

1. Registrar baseline de testes e build do frontend antes da troca visual.
2. Introduzir tokens, componentes de estado e shell sem alterar os contratos de serviço.
3. Migrar dashboard geral, depois setores/drill-downs, importação e relatórios, mantendo as rotas core compatíveis.
4. Executar testes unitários/componentes após cada fatia e E2E após a integração.
5. Validar responsividade, acessibilidade e massa homologada; publicar apenas se testes e build estiverem verdes.
6. Em caso de regressão, reverter a composição visual para o commit anterior mantendo serviços e contratos intactos.

## Open Questions

Não há questão pendente que altere o contrato desta mudança. Se um painel solicitado não for derivável dos payloads reais durante a implementação, ele deverá permanecer em estado de indisponibilidade e gerar um bloqueio explícito para eventual mudança de API.
