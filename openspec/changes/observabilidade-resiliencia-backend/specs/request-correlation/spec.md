# Spec Delta

## Purpose

Define um identificador técnico seguro que permita acompanhar cada requisição entre resposta, erros, logs e integrações sem substituir identificadores de negócio.

## ADDED Requirements

### Requirement: Aceitação e geração de correlation ID
O sistema SHALL aceitar `X-Correlation-Id` somente quando possuir de 1 a 100 caracteres no conjunto `[A-Za-z0-9._-]`; quando ausente ou inválido, SHALL gerar um identificador seguro novo.

#### Scenario: Header válido recebido
- **WHEN** a requisição contém `X-Correlation-Id: abc-123`
- **THEN** o sistema usa `abc-123` como correlation ID da requisição

#### Scenario: Header inválido recebido
- **WHEN** o header contém tamanho excessivo, espaços, quebra de linha ou caracteres fora da política
- **THEN** o sistema substitui o valor por um identificador seguro gerado

### Requirement: Propagação no ciclo HTTP
O sistema SHALL inserir o correlation ID no contexto de logging durante toda a requisição, retorná-lo no header da resposta e remover o valor do contexto ao final do processamento.

#### Scenario: Resposta HTTP concluída
- **WHEN** uma requisição termina com qualquer status
- **THEN** a resposta contém `X-Correlation-Id` e os logs relacionados usam o mesmo valor

#### Scenario: Thread reutilizada
- **WHEN** o servidor reutiliza uma thread para uma nova requisição
- **THEN** o correlation ID anterior não aparece no contexto da nova requisição

### Requirement: Correlation ID em erros
Todas as respostas de erro SHALL conter `timestamp`, `status`, `code`, `message`, `path` e `correlationId`, preservando campos de validação quando aplicáveis.

#### Scenario: Erro de validação
- **WHEN** a entrada viola uma validação de contrato
- **THEN** a resposta usa `VALIDATION_ERROR`, inclui correlation ID e mantém os erros de campo permitidos

#### Scenario: Erro interno
- **WHEN** ocorre uma exceção inesperada
- **THEN** a resposta usa `INTERNAL_ERROR`, inclui correlation ID e apresenta mensagem genérica

### Requirement: Propagação para operações relacionadas
O sistema SHALL propagar o correlation ID em chamadas HTTP externas quando existirem e SHALL associá-lo tecnicamente a importações, auditorias e requisições de IA, sem substituir `importacaoId`, `aiRequestId` ou outros IDs de negócio.

#### Scenario: Chamada externa originada por request
- **WHEN** uma requisição provoca uma chamada HTTP a uma integração configurada
- **THEN** a chamada de saída contém o correlation ID válido da requisição

#### Scenario: Importação registrada
- **WHEN** uma importação é criada durante uma requisição
- **THEN** seus eventos operacionais podem ser localizados pelo correlation ID e pelo identificador próprio da importação

### Requirement: Compatibilidade CORS
O sistema SHALL permitir e expor `X-Correlation-Id` na política CORS configurada, sem ampliar origens permitidas.

#### Scenario: Cliente web autorizado
- **WHEN** uma origem CORS permitida envia uma requisição com correlation ID
- **THEN** o header é aceito e pode ser lido na resposta pelo cliente

