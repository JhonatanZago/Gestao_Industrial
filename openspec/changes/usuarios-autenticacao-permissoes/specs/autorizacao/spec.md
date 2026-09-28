# Spec Delta

## Purpose

Aplicar autorização no backend com menor privilégio, default-deny e isolamento empresarial, impedindo que parâmetros de empresa, setor, perfil ou permissão enviados pelo cliente substituam a identidade autenticada.

## ADDED Requirements

### Requirement: Default-deny e permissões
Todo endpoint MUST exigir autenticação e permissão explícita, exceto login e endpoints técnicos públicos configurados; usuário autenticado sem permissão MUST receber 403.

#### Scenario: Sem permissão financeira
- **WHEN** usuário possui `ANALYTICS_VISUALIZAR` mas não possui `FINANCEIRO_INDUSTRIAL_VISUALIZAR` e acessa analytics financeiro
- **THEN** o sistema MUST retornar 403

#### Scenario: Acesso operacional permitido
- **WHEN** usuário possui `PRODUCAO_VISUALIZAR` e acessa produção autorizada
- **THEN** o sistema MUST permitir a operação

### Requirement: Isolamento por empresa
A empresa efetiva MUST vir da identidade autenticada; chamar URL de outra empresa MUST resultar em 403 ou resposta equivalente consistente, sem retornar dados da empresa alvo.

#### Scenario: Empresa divergente
- **WHEN** usuário da empresa A chama endpoint com `empresaId` da empresa B
- **THEN** o sistema MUST retornar 403 e MUST não consultar ou expor dados de B

### Requirement: Classificação das capacidades
Permissões de visualizar e gerenciar MUST ser separadas quando aplicável, incluindo funcionários, produção, paradas, metas, tempos padrão, valores financeiros, importação e analytics.

#### Scenario: Gerenciamento separado
- **WHEN** usuário possui `FUNCIONARIO_VISUALIZAR` mas não `FUNCIONARIO_GERENCIAR`
- **THEN** o sistema MUST permitir consulta autorizada e MUST negar criação/alteração

### Requirement: Respostas e segredos
O sistema MUST retornar 401 para falhas de autenticação e 403 para falhas de autorização, sem expor stack trace, secret, senha, hash ou token completo.

#### Scenario: Acesso proibido
- **WHEN** usuário autenticado não possui a permissão necessária
- **THEN** o sistema MUST retornar 403 sem transformar a falha em 400
