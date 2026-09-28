# Spec Delta

## Purpose

Permitir analisar a produção agrupada por dimensões operacionais e ao longo do tempo, retornando listas limitadas e dados suficientes para relatórios sem materializar uma tabela paralela de KPIs.

## ADDED Requirements

### Requirement: Agregar por dimensões operacionais

O sistema MUST oferecer agregações por Setor, Funcionário, Peça, Máquina, Turno e Motivo de Parada. Cada item MUST conter sua identificação e descrição disponível, quantidade produzida, quantidade de registros ou ocorrências, durações aplicáveis e as mesmas taxas derivadas do resumo. As listas MUST possuir paginação ou limite controlado e ordenação determinística.

#### Scenario: Agrupar duas dimensões

- **WHEN** o período possui produções em dois Setores
- **THEN** cada Setor recebe somente suas métricas e a soma das quantidades agrupadas corresponde ao resumo filtrado, respeitando arredondamento apenas na apresentação

#### Scenario: Agregar motivos de parada

- **WHEN** paradas ativas possuem motivos diferentes
- **THEN** a agregação por Motivo retorna código, nome, categoria, ocorrências, minutos parados e percentual sobre o tempo parado do conjunto

### Requirement: Tratar dimensões opcionais sem inventar cadastro

Produções sem Máquina ou Turno MUST permanecer na análise geral. Nas agregações correspondentes, o sistema MUST representá-las como grupo técnico sem identificador persistido, com rótulo explícito como `SEM_MAQUINA` ou `SEM_TURNO`, sem criar entidades ou registros artificiais.

#### Scenario: Produção sem máquina

- **WHEN** uma produção ativa não possui máquina
- **THEN** ela compõe o resumo e aparece no grupo explícito sem máquina na agregação por Máquina

### Requirement: Detalhar indicadores de Funcionário

O sistema MUST oferecer lista analítica por Funcionário e detalhe de um Funcionário no escopo da Empresa. O detalhe MUST resumir quantidade, tempos, taxas, peças processadas e Setores envolvidos, sem incluir milhares de registros brutos em uma única resposta.

#### Scenario: Consultar detalhe individual

- **WHEN** o cliente solicita um Funcionário pertencente à Empresa e informa período
- **THEN** recebe o resumo do Funcionário, suas peças e setores agregados, com paginação ou limite controlado para listas

### Requirement: Série temporal

O sistema MUST oferecer série temporal com granularidades `DIA`, `SEMANA` e `MES`, período inclusivo e filtros combináveis. Cada ponto MUST conter o intervalo/balde, quantidade produzida, quantidade de registros e tempos agregados. Períodos sem produção MUST ser omitidos, sem criar intervalos ilimitados ou preencher lacunas silenciosamente.

#### Scenario: Série diária

- **WHEN** existem produções em dois dias do período
- **THEN** a resposta contém um ponto por dia com dados, ordenado cronologicamente, sem depender da ordem de inserção

#### Scenario: Granularidade inválida

- **WHEN** o cliente informa uma granularidade diferente de `DIA`, `SEMANA` ou `MES`
- **THEN** a API rejeita a consulta com erro seguro de granularidade inválida

### Requirement: Consultas analíticas sem mistura ou explosão de dados

As respostas analíticas MUST respeitar o contexto de Empresa e filtros antes de agrupar. Listas grandes MUST ser pagináveis ou limitadas, e o comportamento observável não pode exigir que o cliente receba registros operacionais brutos para obter agregados.

#### Scenario: Empresa sem dados

- **WHEN** o período e filtros válidos não encontram produções
- **THEN** o resumo retorna métricas zero e taxas nulas, e as listas agregadas retornam conteúdo vazio
