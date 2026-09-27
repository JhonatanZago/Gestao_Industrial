# Spec Delta

## Purpose

Associar opcionalmente recursos produtivos e turnos aos registros de produção e recursos às paradas, preservando compatibilidade com históricos que não possuem essas informações.

## ADDED Requirements

### Requirement: Production accepts optional machine and shift
Um Registro de Produção MUST poder ser criado e atualizado sem máquina ou turno; quando informados, ambos MUST pertencer à mesma Empresa, estar ativos e ser retornados de forma resumida na resposta.

#### Scenario: Create production without optional allocation
- **WHEN** o payload não informa maquinaId nem turnoId
- **THEN** a produção é criada mantendo ambas associações nulas

#### Scenario: Create production with valid allocation
- **WHEN** o payload informa máquina e turno ativos da mesma Empresa
- **THEN** a produção é criada com os vínculos e a resposta informa id, código e nome de cada recurso associado

#### Scenario: Reject cross-company allocation
- **WHEN** maquinaId ou turnoId pertence a outra Empresa ou não existe no escopo
- **THEN** a produção é rejeitada sem alterar o registro

#### Scenario: Reject inactive allocation
- **WHEN** maquinaId ou turnoId referencia cadastro inativo
- **THEN** a nova produção ou atualização é rejeitada

### Requirement: Machine must match production sector
Quando uma produção informa máquina, a máquina MUST estar atualmente vinculada ao mesmo Setor da produção; o sistema MUST rejeitar recursos de setor incompatível.

#### Scenario: Accept machine from production sector
- **WHEN** a produção usa setor Pintura e a máquina ativa está alocada atualmente em Pintura
- **THEN** o vínculo é aceito

#### Scenario: Reject machine from another sector
- **WHEN** a produção usa setor Pintura e a máquina ativa está alocada atualmente em Laser
- **THEN** o vínculo é rejeitado com erro de incompatibilidade de setor

### Requirement: Production and stop responses preserve compatibility
As respostas de produção MUST manter os campos existentes e incluir somente os blocos opcionais de máquina e turno quando houver associação; as regras de duração, tempo parado e sobreposição MUST permanecer inalteradas.

#### Scenario: Production crosses shift boundary
- **WHEN** uma produção explicitamente associada a um turno começa antes e termina depois da troca de turno
- **THEN** o registro é aceito com um único turno e não é dividido automaticamente

#### Scenario: Preserve productive-time calculation
- **WHEN** uma produção com máquina e turno possui paradas ativas
- **THEN** tempo parado e tempo produtivo continuam sendo calculados pelas mesmas regras, independentemente das associações

### Requirement: Stops accept an optional consistent machine
Uma Parada de Produção MUST poder permanecer sem máquina; quando informar máquina, ela MUST pertencer à mesma Empresa, estar ativa e ser consistente com a máquina da produção quando esta existir.

#### Scenario: Create stop without machine
- **WHEN** uma parada válida não informa maquinaId
- **THEN** a parada é criada sem máquina e mantém as regras atuais de período e sobreposição

#### Scenario: Create stop with production machine
- **WHEN** a produção possui máquina e a parada informa a mesma máquina ativa
- **THEN** a parada é criada com o vínculo

#### Scenario: Reject inconsistent stop machine
- **WHEN** a produção possui uma máquina e a parada informa outra, ou a máquina pertence a outra Empresa ou está inativa
- **THEN** a parada é rejeitada sem modificar os tempos calculados

### Requirement: No implicit shift resolution or independent machine stops
O sistema MUST preservar o turno explicitamente informado, não MUST sobrescrever associações com inferência automática ambígua e não MUST criar paradas de máquina independentes de uma produção nesta capability.

#### Scenario: Do not infer ambiguous shift
- **WHEN** o horário da produção coincide com mais de uma janela possível ou não há turno explicitamente informado
- **THEN** o sistema não atribui turno automaticamente

#### Scenario: Keep machine stops production-scoped
- **WHEN** um cliente tenta registrar uma parada de máquina sem producaoId
- **THEN** a operação não é suportada pelos endpoints desta capability
