# Spec Delta

## Purpose

Organizar autorizações reutilizáveis em perfis principais e permissões técnicas estáveis, permitindo evoluir a matriz de acesso sem espalhar regras dependentes apenas do nome do perfil.

## ADDED Requirements

### Requirement: Perfis base
O sistema MUST disponibilizar os perfis `ADMINISTRADOR`, `GESTOR` e `OPERACIONAL`, com um perfil principal por usuário nesta versão e possibilidade de evolução futura para múltiplos perfis.

#### Scenario: Administrador da empresa
- **WHEN** administrador possui permissões administrativas
- **THEN** o sistema MUST permitir gestão autorizada dentro da própria empresa, sem conceder alcance global entre empresas

#### Scenario: Operacional
- **WHEN** usuário operacional autentica
- **THEN** o sistema MUST conceder somente o conjunto operacional explicitamente configurado e MUST negar financeiro, usuários, configurações e gestão sem permissão

### Requirement: Permissões granulares
O sistema MUST manter códigos estáveis para visualizar/gerenciar usuários, setores, funcionários, peças, máquinas, produção, paradas, metas, tempos padrão, analytics, desempenho, financeiro, valores, importação e relatórios.

#### Scenario: Perfil com permissões agrupadas
- **WHEN** uma permissão é associada a um perfil
- **THEN** usuários desse perfil MUST receber a permissão sem duplicação manual por usuário

#### Scenario: Permissão ausente
- **WHEN** perfil não possui uma permissão
- **THEN** usuário MUST ser negado mesmo que o cliente envie o nome da permissão na requisição

### Requirement: Seeds técnicos idempotentes
Perfis e permissões base MUST ser criados por seed/migration idempotente sem senha real, token, secret ou e-mail pessoal versionado.

#### Scenario: Aplicação repetida
- **WHEN** migrations/seeds são executados mais de uma vez
- **THEN** o sistema MUST preservar uma única linha por código de perfil/permissão
