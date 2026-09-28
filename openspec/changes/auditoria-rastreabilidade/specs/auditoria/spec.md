# Spec Delta

## Purpose

Esta capability define uma trilha persistente e segura para explicar mudanças de negócio, identificando empresa, ator, recurso, ação, origem e valores controlados antes e depois da alteração.

## ADDED Requirements

### Requirement: Eventos de auditoria persistentes
O sistema SHALL persistir um evento append-only para cada operação de negócio auditável efetivada, contendo `empresaId` quando aplicável, `usuarioId` quando houver ator humano, snapshot mínimo do nome do usuário, `tipoRecurso`, `recursoId`, `acao`, `origem`, `dataHora`, `dadosAnteriores`, `dadosNovos`, `metadados` e `correlationId` opcional. Os identificadores de recurso, ação e origem SHALL ser contratos estáveis e independentes de nomes de classes Java.

#### Scenario: Alteração de produção registra ator e valores
- **WHEN** um usuário autenticado altera a quantidade do registro de produção 100 de 500 para 550 na Empresa A
- **THEN** o sistema persiste um evento da Empresa A com recurso `REGISTRO_PRODUCAO`, id 100, ação `ALTERACAO`, ator autenticado e snapshots contendo os valores anterior e novo

#### Scenario: Operação automática não inventa usuário
- **WHEN** uma operação interna efetiva uma mudança sem usuário humano autenticado
- **THEN** o evento registra origem `SISTEMA` e mantém `usuarioId` nulo, sem atribuir a autoria a outro usuário

### Requirement: Snapshots seguros e controlados
O sistema SHALL serializar somente DTOs/snapshots controlados, com tamanho limitado e campos relevantes para o recurso. Eventos SHALL excluir senha, hash de senha, tokens, secrets, API keys, credenciais, Authorization, cookies de autenticação e equivalentes, inclusive quando a operação for alteração de senha.

#### Scenario: Alteração de senha não armazena segredo
- **WHEN** um usuário altera sua senha ou um administrador redefine a senha de outro usuário
- **THEN** o sistema registra apenas ator, alvo, ação `ALTERACAO_SENHA`, origem, empresa e data, sem senha ou hash anterior/novo no evento ou na resposta

#### Scenario: Valor financeiro é protegido no snapshot
- **WHEN** um valor industrial é criado, encerrado ou alterado
- **THEN** o sistema registra o snapshot financeiro somente no evento persistido e permite sua consulta apenas conforme a permissão financeira aplicável

### Requirement: Consistência transacional da auditoria
O sistema SHALL persistir o evento de sucesso na mesma transação da alteração de negócio auditada. Se a alteração ou a gravação do evento falhar antes do commit, nenhuma das duas mudanças SHALL permanecer como concluída.

#### Scenario: Falha antes do commit não cria falso sucesso
- **WHEN** uma alteração de negócio é iniciada, o evento é preparado e ocorre erro antes do commit
- **THEN** a alteração e o evento de sucesso são revertidos

#### Scenario: Operação rejeitada não gera alteração concluída
- **WHEN** validação, autorização ou regra de negócio rejeita uma operação
- **THEN** o sistema não persiste evento que indique que a mudança foi efetivada

### Requirement: Cobertura das operações auditáveis
O sistema SHALL auditar criação, alteração, ativação e desativação conforme aplicável para produção, paradas, funcionários, peças, máquinas, turnos, metas, tempos padrão, valores industriais, usuários e perfis/permissões. Alterações de permissões e valores financeiros SHALL ser identificáveis como eventos sensíveis.

#### Scenario: Mudança de meta registra vigência e valores
- **WHEN** uma meta de produção de 20.000 peças por dia passa para 22.000 peças por dia
- **THEN** o evento registra recurso `META_PRODUCAO`, ação de alteração e os campos de valor e vigência que mudaram

#### Scenario: Permissão concedida é rastreável
- **WHEN** um administrador concede `FINANCEIRO_INDUSTRIAL_VISUALIZAR` a um usuário
- **THEN** o sistema registra ator, usuário-alvo, ação `ALTERACAO_PERMISSAO`, recurso `PERMISSAO` ou `PERFIL`, data e mudança aplicada
