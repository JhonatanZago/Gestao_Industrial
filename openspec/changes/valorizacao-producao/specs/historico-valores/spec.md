# Spec Delta

## Purpose

Preservar a consistência histórica da valorização, resolvendo o valor aplicável pela data e pelo contexto de cada produção, com precedência de máquina, fallback geral e cobertura explícita.

## ADDED Requirements

### Requirement: Resolução histórica por registro
Para cada produção, o sistema MUST usar o valor vigente na data de início do registro e MUST resolver primeiro o contexto específico da máquina e depois o valor geral de peça e setor.

#### Scenario: Valor histórico
- **WHEN** uma produção de maio é consultada depois que o valor mudou em julho
- **THEN** o sistema MUST usar o valor vigente em maio

#### Scenario: Fallback de máquina
- **WHEN** Cabine 02 possui valor `0.90` e o setor possui valor geral `0.80`
- **THEN** Cabine 02 MUST usar `0.90` e uma máquina sem valor específico MUST usar `0.80`

### Requirement: Valor por produção
O valor industrial calculado MUST ser a quantidade do registro multiplicada pelo valor unitário aplicável, sem arredondar prematuramente cálculos intermediários.

#### Scenario: Cálculo básico
- **WHEN** uma produção possui quantidade 350 e valor unitário BRL 0.85
- **THEN** o sistema MUST calcular valor produzido BRL 297.50

#### Scenario: Múltiplos registros
- **WHEN** registros possuem 300 e 200 unidades com valor unitário 0.85
- **THEN** o sistema MUST retornar 500 unidades e valor total BRL 425.00

### Requirement: Cobertura financeira
Resultados financeiros MUST informar registros totais, registros com valor, registros sem valor, quantidade com valor, valor total calculado e percentual de cobertura; ausência de valor MUST ser distinta de valor monetário zero.

#### Scenario: Cobertura parcial
- **WHEN** 8 de 10 registros possuem valor aplicável
- **THEN** o sistema MUST informar 80% de cobertura e MUST identificar os 2 registros sem valor

#### Scenario: Sem valor aplicável
- **WHEN** nenhum valor vigente é encontrado
- **THEN** o sistema MUST manter a quantidade física e retornar o valor financeiro como indisponível, não como BRL 0.00

### Requirement: Produção manual e importada
Produções manuais e importadas MUST seguir a mesma resolução histórica de valores e MUST não exigir valor financeiro dentro da planilha de importação.

#### Scenario: Origem diferente
- **WHEN** uma produção manual e uma importada possuem o mesmo contexto e data
- **THEN** o sistema MUST aplicar a mesma regra de valor às duas
