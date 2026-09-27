# Spec Delta

## Purpose

Permitir que cada empresa cadastre e mantenha recursos produtivos identificáveis, vinculados ao setor atual, para uso seguro em registros industriais e consultas históricas.

## ADDED Requirements

### Requirement: Empresa can manage productive resources
O sistema MUST permitir criar, consultar, listar, atualizar, ativar e desativar máquinas/recursos produtivos no escopo de uma Empresa, com código, nome, descrição opcional e tipo classificatório opcional.

#### Scenario: Create resource in company sector
- **WHEN** a valid request informs a setor ativo da mesma Empresa, código e nome não vazios
- **THEN** o sistema cria o recurso ativo e retorna seus dados, incluindo empresa, setor, código, nome e tipo

#### Scenario: Reject invalid resource ownership
- **WHEN** a request informa empresa inexistente, setor inexistente ou setor pertencente a outra Empresa
- **THEN** o sistema rejeita a operação e não cria o recurso

#### Scenario: Normalize and protect resource code
- **WHEN** o código possui espaços externos e letras minúsculas
- **THEN** o sistema normaliza o código conforme o padrão dos cadastros existentes e impede outro recurso com o mesmo código na Empresa

#### Scenario: Allow code reuse across companies
- **WHEN** duas Empresas cadastram recursos com o mesmo código
- **THEN** ambas operações são permitidas e os recursos permanecem isolados por Empresa

### Requirement: Resource lifecycle is logical
Máquinas/recursos MUST use ativação e desativação lógica, sem remoção física, e uma atualização MUST preservar a possibilidade de movimentação futura para outro setor sem criar histórico de movimentação nesta capability.

#### Scenario: Deactivate resource
- **WHEN** um recurso ativo é desativado
- **THEN** ele permanece consultável como inativo e não pode ser utilizado em nova produção ou nova parada

#### Scenario: Access is company-scoped
- **WHEN** um cliente tenta consultar, alterar ou alterar o status de um recurso por outra Empresa
- **THEN** o sistema responde como recurso não encontrado naquele escopo

### Requirement: Resource listing supports operational filters
A listagem MUST ser paginada e MUST aceitar filtros por setor, código, nome, tipo e status ativo, combinados dentro da Empresa informada.

#### Scenario: Filter resources by sector and status
- **WHEN** a listagem informa setorId e ativo
- **THEN** somente recursos daquele setor e status são retornados na página solicitada
