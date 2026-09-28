# Spec Delta

## Purpose

Definir metas de produção contextualizadas por organização, recurso e período, com regras explícitas de aplicabilidade, conflito e histórico para apoiar o acompanhamento industrial.

## ADDED Requirements

### Requirement: Cadastro e ciclo de vida da meta
O sistema MUST permitir criar, consultar e atualizar metas com nome, quantidade positiva, tipo de contexto, periodicidade, vigência e contexto compatível com o tipo; ativação e desativação MUST ser operações lógicas.

#### Scenario: Meta diária de setor
- **WHEN** o usuário informa setor, quantidade 20000, periodicidade `DIA` e vigência válida
- **THEN** o sistema MUST persistir uma meta aplicável ao setor

#### Scenario: Quantidade inválida
- **WHEN** o usuário informa quantidade zero ou negativa
- **THEN** o sistema MUST rejeitar a meta

### Requirement: Validação de contexto
O sistema MUST rejeitar combinações ambíguas ou incompatíveis entre tipo de meta e referências de empresa, setor, funcionário, peça, máquina e turno.

#### Scenario: Tipo sem contexto obrigatório
- **WHEN** o usuário cria uma meta do tipo `MAQUINA` sem informar máquina
- **THEN** o sistema MUST rejeitar a operação

#### Scenario: Contexto extra
- **WHEN** o usuário cria uma meta do tipo `SETOR` informando simultaneamente funcionário e máquina sem uma composição suportada
- **THEN** o sistema MUST rejeitar a operação

### Requirement: Periodicidade e vigência
O sistema MUST suportar periodicidades de turno, dia, semana e mês, com início de vigência e fim opcional, e MUST rejeitar sobreposição de metas ativas conflitantes para o mesmo contexto e periodicidade.

#### Scenario: Conflito de metas
- **WHEN** uma nova meta ativa se sobrepõe a outra meta ativa do mesmo contexto e periodicidade
- **THEN** o sistema MUST rejeitar a nova meta

#### Scenario: Periodicidades distintas
- **WHEN** existem metas válidas para o mesmo setor, uma diária e outra mensal
- **THEN** o sistema MUST permitir ambas e identificá-las pela periodicidade

### Requirement: Aplicação e histórico da meta
O sistema MUST selecionar a meta aplicável ao contexto e período consultado, preservar metas desativadas para histórico e MUST calcular uma meta mensal sem ratear automaticamente o valor por dias parciais.

#### Scenario: Período parcial
- **WHEN** uma consulta cobre somente parte do mês de uma meta mensal
- **THEN** o sistema MUST manter o valor mensal da meta e MUST informar o período consultado sem rateio automático

#### Scenario: Meta desativada
- **WHEN** uma meta é desativada
- **THEN** o sistema MUST impedir sua aplicação em novos cálculos e MUST preservar sua consulta histórica
