# Spec Delta

## Purpose

Disponibilizar indicadores operacionais calculados a partir dos registros reais de produção e das paradas persistidas, com fórmulas estáveis, isolamento por Empresa e período explícito para consumo de relatórios e integrações futuras.

## ADDED Requirements

### Requirement: Resumo analítico por Empresa e período

O sistema MUST expor um resumo analítico no contexto de uma Empresa, com `inicio` e `fim` obrigatórios e inclusivos. O resumo MUST retornar quantidade produzida, quantidade de registros, duração bruta em minutos, tempo parado em minutos, tempo produtivo em minutos, taxa bruta em peças/hora, taxa produtiva em peças/hora e percentual de tempo parado. Por padrão, somente registros de produção ativos e paradas ativas de registros incluídos devem participar.

#### Scenario: Calcular resumo determinístico

- **WHEN** o período contém dois registros ativos com 300 e 200 peças, 240 minutos brutos e 30 minutos de paradas ativas
- **THEN** o resumo retorna 500 peças, 2 registros, 240 minutos brutos, 30 minutos parados, 210 minutos produtivos, taxa bruta de 125,00, taxa produtiva de 142,86 e percentual parado de 12,50

#### Scenario: Restringir consulta à Empresa

- **WHEN** uma consulta é feita no contexto de uma Empresa
- **THEN** nenhum registro, parada ou dimensão pertencente a outra Empresa pode alterar o resultado

#### Scenario: Rejeitar período inválido

- **WHEN** `fim` é anterior a `inicio` ou um dos limites obrigatórios está ausente
- **THEN** a API rejeita a consulta com erro seguro de período inválido e não executa uma consulta ilimitada

### Requirement: Fórmulas e divisão por zero

O sistema MUST calcular duração bruta como a soma de `fim - inicio`, tempo parado como a soma das paradas ativas, e tempo produtivo como `max(0, duração bruta - tempo parado)`. As taxas e percentuais MUST usar `BigDecimal`, escala de apresentação 2 e arredondamento HALF_UP, sem arredondar valores intermediários. Quando o denominador for zero, o valor derivado MUST ser `null`.

#### Scenario: Registro sem duração calculável

- **WHEN** a duração bruta ou produtiva do conjunto é zero
- **THEN** as taxas e o percentual cujo denominador seja zero são retornados como `null`, sem erro aritmético

#### Scenario: Parada não ativa

- **WHEN** uma produção possui uma parada inativa e uma parada ativa
- **THEN** somente a duração da parada ativa compõe o tempo parado e o cálculo produtivo

### Requirement: Filtros analíticos combináveis

O resumo MUST aceitar filtros opcionais de `setorId`, `funcionarioId`, `pecaId`, `maquinaId` e `turnoId`. Cada filtro MUST ser validado no escopo da Empresa; um identificador de outra Empresa não pode produzir dados nem ser silenciosamente ignorado.

#### Scenario: Aplicar filtros simultâneos

- **WHEN** a consulta informa Setor, Funcionário e Peça
- **THEN** somente registros que atendem simultaneamente aos três filtros entram nas métricas

#### Scenario: Filtro fora da Empresa

- **WHEN** um filtro referencia uma dimensão de outra Empresa
- **THEN** a API rejeita a consulta com erro seguro de contexto inválido

### Requirement: Histórico independente do status atual das dimensões

Registros ativos devem continuar compondo análises históricas mesmo que Funcionário, Peça, Setor ou Máquina relacionados tenham sido desativados depois da produção. Um registro de produção inativo MUST ser excluído do resultado padrão.

#### Scenario: Dimensão desativada após produção

- **WHEN** uma produção permanece ativa, mas seu Funcionário ou Setor relacionado é desativado posteriormente
- **THEN** a produção ainda aparece na análise histórica com os dados vinculados persistidos

#### Scenario: Produção desativada

- **WHEN** o próprio Registro de Produção é desativado
- **THEN** ele não entra no resumo padrão
