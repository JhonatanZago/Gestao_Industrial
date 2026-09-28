# Spec Delta

## Purpose

Disponibilizar análises financeiras industriais explicáveis por período e dimensão, relacionando quantidade, valor, moeda, cobertura e valor por hora sem afirmar custo, lucro ou faturamento contábil.

## ADDED Requirements

### Requirement: Resumo e agregações financeiras
O sistema MUST disponibilizar resumo e agregações de valor por setor, funcionário, peça, máquina e turno, respeitando empresa, período e filtros dos analytics existentes.

#### Scenario: Valor por funcionário
- **WHEN** o consumidor solicita o período de um funcionário
- **THEN** o sistema MUST retornar somente o valor industrial das produções registradas sob seu código e a moeda correspondente

#### Scenario: Valor por setor
- **WHEN** o consumidor solicita agregação por setor
- **THEN** o sistema MUST agrupar quantidade e valor dos registros valorizados daquele setor

### Requirement: Moeda e semântica analítica
Cada resultado financeiro MUST informar moeda e semântica do valor; resultados em moedas diferentes MUST ser separados e o sistema MUST exibir receita estimada somente quando existir valor explicitamente cadastrado com esse tipo.

#### Scenario: Produção não é faturamento
- **WHEN** o resultado agrega valores do tipo `VALOR_PRODUCAO`
- **THEN** o sistema MUST nomear o indicador como valor industrial produzido e MUST não chamá-lo de faturamento

#### Scenario: Receita estimada explícita
- **WHEN** o resultado contém valor `RECEITA_ESTIMADA`
- **THEN** o sistema MUST disponibilizar receita estimada separadamente do valor industrial

### Requirement: Valor por hora
O sistema MUST calcular separadamente valor por hora bruta e valor por hora produtiva, usando valor produzido dividido pelo respectivo tempo em horas, e MUST retornar nulo quando o denominador for zero.

#### Scenario: Hora produtiva
- **WHEN** o valor produzido é BRL 500 e o tempo produtivo é 120 minutos
- **THEN** o sistema MUST retornar BRL 250 por hora produtiva

#### Scenario: Tempo zero
- **WHEN** o tempo bruto ou produtivo é zero
- **THEN** o sistema MUST retornar nulo somente para a taxa correspondente

### Requirement: Série temporal e comparativo
O sistema MUST fornecer série temporal diária, semanal e mensal e comparativo entre períodos com valor atual, anterior, variação absoluta e percentual; base anterior zero MUST produzir percentual nulo.

#### Scenario: Comparativo de valor
- **WHEN** o valor atual é BRL 104 e o anterior BRL 100
- **THEN** o sistema MUST retornar variação absoluta BRL 4 e percentual 4%

#### Scenario: Base anterior zero
- **WHEN** não existe valor financeiro no período anterior
- **THEN** o sistema MUST retornar variação percentual nula sem divisão por zero

### Requirement: Limites financeiros
O sistema MUST usar precisão decimal para dinheiro, MUST evitar arredondamento intermediário e MUST não calcular automaticamente custo, margem, lucro ou dinheiro perdido por parada.

#### Scenario: Valor unitário com várias casas
- **WHEN** o valor unitário é 0.0085 e a quantidade é 1000
- **THEN** o sistema MUST preservar o cálculo decimal e retornar BRL 8.50 conforme a escala de apresentação

#### Scenario: Parada produtiva
- **WHEN** uma produção possui parada registrada
- **THEN** o sistema MUST manter o valor produzido pela quantidade e MUST não criar automaticamente uma perda financeira
