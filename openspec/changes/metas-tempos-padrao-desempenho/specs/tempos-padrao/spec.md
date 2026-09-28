# Spec Delta

## Purpose

Define tempos esperados de produção versionados e aplicáveis por contexto, preservando cálculos históricos e permitindo fallback previsível quando não houver um padrão específico de máquina.

## ADDED Requirements

### Requirement: Cadastro de tempo padrão
O sistema MUST permitir cadastrar, consultar e atualizar um tempo padrão associado a uma empresa, peça e setor, com máquina opcional, valor positivo em segundos, base por unidade ou por lote e quantidade de referência obrigatória quando a base for lote.

#### Scenario: Cadastro válido por unidade
- **WHEN** um usuário autorizado informa empresa, peça, setor, 20 segundos e base `POR_UNIDADE`
- **THEN** o sistema MUST persistir o tempo padrão com os dados informados

#### Scenario: Valor inválido
- **WHEN** o usuário informa tempo zero, negativo ou quantidade de lote não positiva
- **THEN** o sistema MUST rejeitar o cadastro com erro de validação

### Requirement: Vigência e conflitos
Cada tempo padrão MUST possuir início de vigência, fim opcional e ciclo de vida lógico; o sistema MUST rejeitar sobreposição de vigências ativas para o mesmo contexto de empresa, peça, setor e máquina, tratando a máquina nula de forma consistente.

#### Scenario: Vigências consecutivas
- **WHEN** existe um padrão vigente até 30 de junho e o usuário cadastra outro a partir de 1º de julho
- **THEN** o sistema MUST aceitar os dois padrões

#### Scenario: Sobreposição
- **WHEN** o usuário cadastra um padrão cuja vigência se sobrepõe a outro padrão ativo do mesmo contexto
- **THEN** o sistema MUST rejeitar a operação e informar conflito de vigência

### Requirement: Resolução do padrão aplicável
Para cada registro de produção, o sistema MUST resolver o padrão pela data de início do registro, priorizando o padrão específico da máquina e usando o padrão geral do setor como fallback; a ausência de ambos MUST ser representada como padrão indisponível.

#### Scenario: Fallback de máquina
- **WHEN** a máquina possui padrão de 17 segundos e o setor possui padrão geral de 20 segundos
- **THEN** o sistema MUST usar 17 segundos para registros daquela máquina

#### Scenario: Fallback geral
- **WHEN** não existe padrão vigente para a máquina, mas existe padrão geral vigente para o setor
- **THEN** o sistema MUST usar o padrão geral do setor

#### Scenario: Histórico por data do registro
- **WHEN** um registro de maio é consultado após a troca do padrão em julho
- **THEN** o sistema MUST calcular o registro usando o padrão vigente em maio

### Requirement: Histórico e isolamento
Desativar um tempo padrão MUST impedir sua aplicação futura sem apagar seu histórico; dados de uma empresa MUST permanecer isolados das consultas e validações de outra empresa.

#### Scenario: Desativação preserva histórico
- **WHEN** um padrão é desativado depois de ter sido usado em análises históricas
- **THEN** o sistema MUST mantê-lo consultável e MUST impedir sua seleção para novos cálculos

#### Scenario: Isolamento por empresa
- **WHEN** uma consulta é feita para uma empresa
- **THEN** o sistema MUST retornar e validar somente padrões pertencentes àquela empresa
