# Spec Delta

## Purpose

Comparar dois períodos analíticos explicitamente, usando as mesmas regras de inclusão e filtros, para mostrar diferenças operacionais sem esconder casos em que a base anterior é zero.

## ADDED Requirements

### Requirement: Comparar períodos explícitos

O sistema MUST aceitar `inicioAtual`, `fimAtual`, `inicioAnterior` e `fimAnterior`, além dos filtros analíticos aplicáveis. Os dois intervalos MUST usar limites inclusivos, ser válidos individualmente e permanecer no mesmo contexto de Empresa.

#### Scenario: Comparativo de produção

- **WHEN** o período atual possui 500 peças e o anterior possui 400 peças
- **THEN** o comparativo retorna valor atual 500, valor anterior 400, variação absoluta 100 e variação percentual 25,00

#### Scenario: Intervalo inválido

- **WHEN** qualquer `fim` é anterior ao respectivo `inicio` ou algum limite obrigatório está ausente
- **THEN** a API rejeita a consulta com erro seguro de período inválido

### Requirement: Calcular variações sem divisão inválida

Para cada indicador comparável, a variação absoluta MUST ser `atual - anterior` e a variação percentual MUST ser `((atual - anterior) / anterior) * 100`, com escala 2 e arredondamento HALF_UP. Quando o valor anterior for zero, a variação percentual MUST ser `null`, mantendo valores atual, anterior e diferença absoluta.

#### Scenario: Base anterior zero

- **WHEN** o período anterior possui zero registros ou zero quantidade e o atual possui valor positivo
- **THEN** o comparativo retorna a diferença absoluta e `variacaoPercentual: null`, sem erro de divisão por zero

### Requirement: Manter equivalência das regras analíticas

O comparativo MUST aplicar nos dois períodos as mesmas regras de registros ativos, paradas ativas, dimensões desativadas, filtros por Empresa, inclusão pelo início do registro e cálculo integral da duração do registro incluído. A API MUST informar os períodos efetivamente utilizados na resposta.

#### Scenario: Produção atravessando limite

- **WHEN** uma produção começa antes do início do período e termina dentro dele
- **THEN** ela não entra no período, pois a regra de inclusão é baseada no início; nenhuma quantidade ou duração é proporcionalmente inventada

#### Scenario: Filtro igual nos dois períodos

- **WHEN** o comparativo informa um Setor
- **THEN** o Setor é aplicado de forma idêntica aos períodos atual e anterior e não pode misturar dados de outra Empresa
