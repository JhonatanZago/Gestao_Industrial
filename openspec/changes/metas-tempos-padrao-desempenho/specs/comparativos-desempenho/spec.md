# Spec Delta

## Purpose

Permitir comparar desempenho industrial por dimensões relevantes com os mesmos filtros e períodos dos analytics, apresentando valores e cobertura sem transformar métricas contextuais em classificações subjetivas.

## ADDED Requirements

### Requirement: Comparativos por dimensão
O sistema MUST disponibilizar comparativos por funcionário, setor, máquina e peça, respeitando empresa, período, granularidade e filtros compatíveis com os analytics existentes.

#### Scenario: Comparativo por funcionário
- **WHEN** o consumidor solicita desempenho de funcionários para uma empresa e período
- **THEN** o sistema MUST retornar uma linha por funcionário com quantidade real, esperada, desempenho e cobertura disponíveis

#### Scenario: Comparativo por máquina
- **WHEN** o consumidor solicita desempenho de máquinas
- **THEN** o sistema MUST agrupar os registros pela máquina mantendo os filtros informados

### Requirement: Comparativos explicáveis
Cada resultado comparativo MUST expor quantidade real, quantidade esperada quando calculável, desempenho, meta e atingimento quando aplicáveis, além da cobertura dos dados usados.

#### Scenario: Cobertura no comparativo
- **WHEN** uma dimensão possui registros com e sem tempo padrão
- **THEN** o sistema MUST expor a cobertura daquela dimensão e MUST preservar a quantidade real total

#### Scenario: Divisão por zero
- **WHEN** a quantidade esperada de uma dimensão é zero
- **THEN** o sistema MUST retornar desempenho nulo ou não calculável para aquela dimensão

### Requirement: Ausência de ranking subjetivo
Os comparativos MUST apresentar métricas e identificadores das dimensões, mas não MUST declarar automaticamente uma dimensão como melhor ou pior.

#### Scenario: Ordenação solicitada
- **WHEN** o consumidor solicita os resultados ordenados por desempenho
- **THEN** o sistema MUST ordenar pelos valores calculáveis sem adicionar rótulo subjetivo de vencedor ou perdedor
