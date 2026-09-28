# Spec Delta

## Purpose

Estabelecer autenticação stateless para identificar cada requisição, proteger credenciais e fornecer login, identidade atual e alteração de senha com expiração previsível.

## ADDED Requirements

### Requirement: Login com token expirável
O sistema MUST disponibilizar `POST /api/v1/auth/login`, validar senha com hash seguro e retornar Bearer token assinado com expiração configurável, sem retornar senha ou hash.

#### Scenario: Credenciais válidas
- **WHEN** usuário ativo informa e-mail e senha corretos
- **THEN** o sistema MUST retornar 200 com token, tipo Bearer, expiração e dados públicos do usuário

#### Scenario: Credenciais inválidas
- **WHEN** e-mail inexistente ou senha incorreta é informado
- **THEN** o sistema MUST retornar 401 com mensagem genérica de credenciais inválidas

### Requirement: Validação da identidade
Requisições protegidas MUST exigir Bearer token válido, não expirado e assinado pela configuração vigente; ausência, token inválido ou expirado MUST retornar 401.

#### Scenario: Sem token
- **WHEN** cliente chama endpoint protegido sem Authorization
- **THEN** o sistema MUST retornar 401

#### Scenario: Token expirado
- **WHEN** cliente envia token expirado
- **THEN** o sistema MUST retornar 401 sem expor detalhes internos

### Requirement: Identidade atual e alteração de senha
O sistema MUST disponibilizar `GET /api/v1/auth/me` sem senha/hash e `PATCH /api/v1/auth/senha` exigindo senha atual e nova senha conforme política mínima; alteração MUST invalidar ou tornar inelegível a sessão/token conforme estratégia documentada.

#### Scenario: Consulta do usuário autenticado
- **WHEN** usuário autenticado chama `/auth/me`
- **THEN** o sistema MUST retornar id, nome, e-mail, empresa, perfil, permissões e setores autorizados

#### Scenario: Senha atual incorreta
- **WHEN** usuário informa senha atual incorreta
- **THEN** o sistema MUST rejeitar a alteração sem modificar o hash
