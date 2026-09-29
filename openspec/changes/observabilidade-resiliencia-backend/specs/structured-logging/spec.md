# Spec Delta

## Purpose

Define logs operacionais correlacionáveis, sanitizados e adequados a produção para diagnóstico sem transformar logging em armazenamento de dados do ERP.

## ADDED Requirements

### Requirement: Evento estruturado de requisição
O sistema SHALL emitir um evento estruturado ao término de cada requisição com timestamp, nível, logger, mensagem, correlation ID, método, rota normalizada, status e duração em milissegundos.

#### Scenario: Requisição concluída
- **WHEN** uma requisição HTTP termina
- **THEN** um evento contém os campos operacionais obrigatórios e não inclui o body da requisição ou da resposta

### Requirement: Sanitização de dados sensíveis
O sistema MUST NOT registrar senha, hash de senha, JWT, cabeçalho Authorization, refresh token, API key, cookie, secret, prompt completo, resposta completa de IA ou payload industrial completo. Valores controlados externamente SHALL ser limitados e neutralizados contra log injection.

#### Scenario: Requisição contém credenciais
- **WHEN** uma requisição contém Authorization, cookie ou campo de senha
- **THEN** nenhum valor dessas credenciais aparece nos logs produzidos pelo backend

#### Scenario: Valor contém quebra de linha
- **WHEN** um valor externo usado em log contém caracteres de controle
- **THEN** o valor é removido, substituído ou normalizado antes do registro

### Requirement: Diagnóstico interno de falhas inesperadas
O sistema SHALL registrar internamente a exceção inesperada com stack trace e correlation ID, enquanto MUST retornar ao cliente somente mensagem genérica e código estável.

#### Scenario: Exceção inesperada
- **WHEN** uma operação lança uma exceção não mapeada
- **THEN** o log interno contém stack trace e correlation ID e a resposta não contém stack trace, SQL, path local ou mensagem técnica

### Requirement: Política de níveis por ambiente
O sistema SHALL definir configuração explícita para `dev`, `test` e `prod`; produção MUST NOT operar com DEBUG global. No startup, somente aplicação, versão, profile, Java e estado habilitado/desabilitado da IA podem ser registrados como resumo operacional.

#### Scenario: Inicialização em produção
- **WHEN** a aplicação inicia com profile de produção
- **THEN** o nível global não é DEBUG e nenhum secret ou configuração completa é registrado

### Requirement: Body HTTP desabilitado por padrão
O sistema SHALL manter logging de bodies HTTP desabilitado por padrão. Qualquer diagnóstico excepcional que o habilite MUST ser limitado, sanitizado, temporário e documentado.

#### Scenario: Operação normal
- **WHEN** APIs de importação, relatórios, autenticação ou IA são chamadas
- **THEN** seus bodies não são gravados nos logs padrão

