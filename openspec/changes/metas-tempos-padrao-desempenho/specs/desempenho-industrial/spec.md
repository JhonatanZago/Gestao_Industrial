# Spec Delta

## Purpose

Transformar produção registrada e tempo produtivo em medidas explicáveis de esperado, desempenho, atingimento de meta e cobertura, sem mascarar registros que não possuem padrão aplicável.

## ADDED Requirements

### Requirement: Cálculo de esperado e desempenho
O sistema MUST calcular a quantidade esperada a partir do tempo produtivo e do tempo padrão por unidade, e o desempenho como quantidade real dividida pela esperada vezes 100; valores acima de 100% MUST ser permitidos.

#### Scenario: Desempenho de 100 por cento
- **WHEN** o tempo padrão é 20 segundos, o tempo produtivo é 3600 segundos e a quantidade real é 180
- **THEN** o sistema MUST retornar quantidade esperada 180 e desempenho 100%

#### Scenario: Desempenho acima do esperado
- **WHEN** os mesmos parâmetros possuem quantidade real 198
- **THEN** o sistema MUST retornar desempenho de 110%

### Requirement: Base produtiva e histórico
O sistema MUST usar tempo produtivo, descontando paradas aplicáveis, e MUST resolver o padrão de cada registro pela data de início e pelo contexto do registro antes de agregar resultados.

#### Scenario: Parada reduz tempo produtivo
- **WHEN** um registro possui tempo bruto e uma parada ativa durante o período
- **THEN** o sistema MUST calcular o esperado usando o tempo produtivo resultante, não o tempo bruto

#### Scenario: Padrões diferentes por peça
- **WHEN** um funcionário produz duas peças com padrões distintos no mesmo período
- **THEN** o sistema MUST calcular o esperado por registro e depois agregar os resultados

### Requirement: Ausência e cobertura
Quando não houver padrão aplicável, o sistema MUST manter a produção real nos resultados base, MUST não tratar a ausência como zero e MUST informar totais, registros com padrão, registros sem padrão, quantidade coberta e percentual de cobertura.

#### Scenario: Cobertura parcial
- **WHEN** 8 de 10 registros possuem padrão aplicável
- **THEN** o sistema MUST informar cobertura de 80%, 8 registros com padrão e 2 sem padrão

#### Scenario: Denominador inexistente
- **WHEN** não existe quantidade esperada positiva ou não existe meta aplicável
- **THEN** o sistema MUST retornar o indicador dependente desse denominador como nulo ou não calculável

### Requirement: Atingimento de meta
O sistema MUST calcular atingimento como quantidade real dividida pela meta aplicável vezes 100, MUST permitir valores acima de 100% e MUST expor a situação de ausência de meta separadamente de zero atingimento.

#### Scenario: Meta superada
- **WHEN** a meta diária é 20000 e a produção real é 22000
- **THEN** o sistema MUST retornar atingimento de 110%

#### Scenario: Meta não encontrada
- **WHEN** não existe meta válida para o contexto consultado
- **THEN** o sistema MUST retornar atingimento não calculável e indicar que a meta está indisponível
