# Spec Delta

## Purpose

Define a reconstrução visual Premium Dark do dashboard industrial com fidelidade à referência fornecida, mantendo dados reais, responsividade, acessibilidade e contratos funcionais existentes.

## ADDED Requirements

### Requirement: O dashboard SHALL reproduzir a composição visual Premium Dark da referência

Em desktop, o sistema SHALL apresentar sidebar de aproximadamente 232px, header compacto, quatro KPIs, gráfico de evolução à esquerda, produção por setor à direita e três painéis inferiores, respeitando espaçamentos, proporções, tokens, tipografia, bordas, sombras e estados da referência.

#### Scenario: Visão Geral em 1366x768

- **WHEN** uma sessão autorizada abre a Visão Geral em 1366x768
- **THEN** header, quatro KPIs e pelo menos 65% do grid gráfico/setores SHALL estar visíveis na primeira dobra
- **AND** não SHALL existir overflow horizontal ou container gigante de período

#### Scenario: Visão Geral em 1920x1080

- **WHEN** a Visão Geral é aberta em 1920x1080
- **THEN** a sidebar, grid principal e três cards inferiores SHALL manter as proporções da referência
- **AND** números, setores, funcionários, peças e datas SHALL vir das APIs reais

### Requirement: Indicadores e gráficos SHALL usar somente dados reais

KPIs, série temporal, percentuais de setor, rankings, resumo e qualquer tabela SHALL ser derivados de services/DTOs existentes; valores ilustrativos da imagem SHALL nunca ser copiados.

#### Scenario: Payload possui dados parciais

- **WHEN** um campo ou widget não é fornecido pelo backend
- **THEN** o painel SHALL exibir empty/indisponível mantendo suas dimensões
- **AND** SHALL não inventar OEE, lucro, faturamento, eficiência, parada ou números da referência

### Requirement: A validação visual SHALL ser repetível e bloquear conclusão falsa

Após a implementação, o frontend SHALL ser executado, screenshots SHALL ser capturados em 1366x768 e 1920x1080, e as diferenças de posição, tamanho, espaçamento, tipografia, cor, gráfico, cards e sidebar SHALL ser verificadas antes do status final.

#### Scenario: Build passa mas a composição diverge

- **WHEN** `npm test` e `npm run build` passam, mas a comparação visual identifica diferença material
- **THEN** o status SHALL permanecer FAIL até a correção e nova captura

### Requirement: Funcionalidades existentes SHALL permanecer preservadas

Autenticação, autorização, endpoints, importação Excel, relatórios, códigos operacionais e regras de negócio SHALL continuar funcionando sem alteração de backend.

#### Scenario: Usuário usa o fluxo operacional

- **WHEN** o usuário navega, importa Excel ou gera relatório após o redesign
- **THEN** as mesmas APIs e permissões SHALL ser usadas
- **AND** códigos como `00147` SHALL conservar zeros à esquerda
