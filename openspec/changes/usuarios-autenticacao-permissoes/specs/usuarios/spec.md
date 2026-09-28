# Spec Delta

## Purpose

Definir usuários empresariais com identidade, status, senha protegida, perfil principal e ciclo de vida administrativo, sem expor credenciais ou permitir cadastro público.

## ADDED Requirements

### Requirement: Cadastro administrativo de usuário
O sistema MUST permitir que somente um usuário autorizado crie usuários dentro da própria empresa, informando nome, e-mail, senha inicial, perfil principal e setores autorizados quando aplicável.

#### Scenario: Cadastro válido
- **WHEN** um administrador autenticado cria usuário com dados válidos da própria empresa
- **THEN** o sistema MUST criar o usuário com senha armazenada somente como hash

#### Scenario: Cadastro não autorizado
- **WHEN** um usuário sem `USUARIO_GERENCIAR` tenta criar usuário
- **THEN** o sistema MUST retornar 403 e MUST não criar o usuário

### Requirement: E-mail e status
O sistema MUST normalizar e-mail de modo consistente, garantir unicidade global e suportar status `ATIVO`, `INATIVO` e `BLOQUEADO`; usuários inativos ou bloqueados MUST não autenticar.

#### Scenario: E-mail duplicado
- **WHEN** o usuário tenta cadastrar e-mail já existente
- **THEN** o sistema MUST rejeitar a operação sem criar duplicata

#### Scenario: Usuário bloqueado
- **WHEN** um usuário bloqueado tenta fazer login com senha correta
- **THEN** o sistema MUST rejeitar as credenciais

### Requirement: Ciclo de vida e senha administrativa
O sistema MUST disponibilizar consulta paginada, atualização, ativação/desativação e reset administrativo de senha sem revelar a senha atual, hash ou token.

#### Scenario: Desativação
- **WHEN** um administrador desativa um usuário
- **THEN** o usuário MUST deixar de autenticar e permanecer consultável sem exclusão física

#### Scenario: Reset de senha
- **WHEN** um administrador autorizado solicita reset
- **THEN** o sistema MUST substituir o hash sem retornar a senha anterior
