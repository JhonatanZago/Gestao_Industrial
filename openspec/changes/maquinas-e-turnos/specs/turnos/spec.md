# Spec Delta

## Purpose

Permitir que cada empresa defina turnos operacionais dinâmicos com janelas nominais, inclusive noturnas, sem acoplar o domínio a uma quantidade fixa de turnos.

## ADDED Requirements

### Requirement: Empresa can manage dynamic shifts
O sistema MUST permitir criar, consultar, listar, atualizar, ativar e desativar turnos no escopo de uma Empresa, com código, nome, hora inicial e hora final.

#### Scenario: Create daytime shift
- **WHEN** uma Empresa cadastra código, nome, horaInicio 06:00 e horaFim 14:00
- **THEN** o sistema cria o turno ativo e retorna seus dados

#### Scenario: Create overnight shift
- **WHEN** uma Empresa cadastra horaInicio 22:00 e horaFim 06:00
- **THEN** o sistema aceita o turno como janela que atravessa meia-noite

#### Scenario: Reject equal shift boundaries
- **WHEN** horaInicio e horaFim são iguais
- **THEN** o sistema rejeita o turno por intervalo ambíguo entre zero e vinte e quatro horas

#### Scenario: Protect shift code uniqueness
- **WHEN** a Empresa já possui um turno com o mesmo código normalizado
- **THEN** o sistema rejeita o novo cadastro ou atualização conflitante, mas permite o mesmo código em outra Empresa

### Requirement: Shift duration is derived
O sistema MUST disponibilizar a duração nominal do turno sem persistir duração redundante; para uma hora final anterior à inicial, o cálculo MUST considerar a passagem pela meia-noite.

#### Scenario: Calculate regular duration
- **WHEN** o turno é 06:00 até 14:00
- **THEN** a duração nominal retornada é 480 minutos

#### Scenario: Calculate overnight duration
- **WHEN** o turno é 22:00 até 06:00
- **THEN** a duração nominal retornada é 480 minutos

### Requirement: Inactive shifts are historical only
Turnos inativos MUST continuar consultáveis em históricos, mas MUST ser rejeitados quando informados em uma nova produção.

#### Scenario: Deactivate shift
- **WHEN** um turno é desativado
- **THEN** ele permanece na consulta com ativo=false e não pode ser associado a nova produção

#### Scenario: Scope shift access by company
- **WHEN** um cliente usa o identificador de turno de outra Empresa
- **THEN** a consulta ou alteração é rejeitada como recurso fora do escopo

### Requirement: Shift listing supports operational filters
A listagem de turnos MUST ser paginada e MUST aceitar filtros por código, nome e status ativo dentro da Empresa.

#### Scenario: Filter active shifts
- **WHEN** a listagem informa ativo=true e nome
- **THEN** somente turnos ativos cujo nome corresponda ao filtro são retornados
