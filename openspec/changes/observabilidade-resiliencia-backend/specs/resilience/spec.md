# Spec Delta

## Purpose

Define limites e respostas previsíveis para falhas transitórias, indisponibilidade externa, encerramento e consumo de recursos sem ocultar erros ou inventar dados.

## ADDED Requirements

### Requirement: Timeouts obrigatórios em integrações externas
Toda integração HTTP externa SHALL possuir connect timeout e response/read timeout finitos e configuráveis. Um timeout SHALL encerrar a espera e produzir erro classificado sem manter thread presa indefinidamente.

#### Scenario: Serviço externo não responde
- **WHEN** uma integração ultrapassa o timeout configurado
- **THEN** a chamada é encerrada, uma falha de timeout é medida e o cliente recebe erro controlado apropriado

### Requirement: Retry seletivo e limitado
Retry SHALL ocorrer somente para operações idempotentes e falhas transitórias elegíveis, como timeout, 502, 503 ou 504, com tentativas e backoff configuráveis e limitados. O sistema MUST NOT repetir automaticamente 400, 401, 403, 422, escrita não idempotente ou transação de banco.

#### Scenario: GET remoto retorna 503 e depois sucesso
- **WHEN** uma leitura remota idempotente retorna 503 dentro do limite configurado
- **THEN** o sistema realiza retry com backoff e retorna o resultado quando uma tentativa permitida tem sucesso

#### Scenario: Integração retorna 401
- **WHEN** uma integração externa retorna 401
- **THEN** o sistema não repete a chamada e classifica a falha de autenticação

### Requirement: Circuit breaker condicionado a dependência remota
Circuit breaker SHALL ser aplicado somente a adapters remotos reais cuja falha repetida possa degradar o ERP e SHALL possuir transições verificáveis entre `CLOSED`, `OPEN` e `HALF_OPEN`. Operações locais e banco MUST NOT receber circuit breaker por conveniência.

#### Scenario: Falhas remotas repetidas
- **WHEN** uma integração protegida excede o limiar configurado de falhas
- **THEN** o circuito abre, rejeita chamadas dentro da janela e permite sondagem controlada em half-open

#### Scenario: Nenhum adapter remoto ativo
- **WHEN** a aplicação opera apenas com adapters locais ou desabilitados
- **THEN** nenhum circuit breaker artificial é exigido para esses componentes

### Requirement: Fallback sem fabricação de dados
Fallback MUST preservar a verdade operacional e MUST NOT inventar dados. Dependências opcionais SHALL degradar somente sua funcionalidade; dependências essenciais SHALL produzir indisponibilidade explícita.

#### Scenario: IA indisponível
- **WHEN** o provedor de IA falha ou o circuito está aberto
- **THEN** o assistente retorna estado indisponível e produção, Dashboard e relatórios continuam operacionais

### Requirement: Encerramento gracioso
O sistema SHALL parar de aceitar novas requisições durante shutdown e aguardar requisições em andamento por um limite configurável antes de encerrar. O comportamento de importações em andamento MUST ser documentado sem prometer retomada automática inexistente.

#### Scenario: Shutdown durante requisição
- **WHEN** o encerramento é iniciado com uma requisição em andamento
- **THEN** a aplicação aguarda até a conclusão ou até o limite configurado e não aceita novo tráfego

### Requirement: Recursos e limites finitos
Pools de banco e execução SHALL possuir limites explícitos e configuráveis. O sistema SHALL impor limites a payload HTTP, upload Excel e documentos RAG e SHALL retornar erro controlado quando excedidos. Produção MUST operar com SQL detalhado desabilitado.

#### Scenario: Upload excede o limite
- **WHEN** um upload Excel ou documento RAG excede seu limite configurado
- **THEN** a requisição é rejeitada com erro estável, sem processar parcialmente o conteúdo

#### Scenario: Profile de produção
- **WHEN** a aplicação executa em produção
- **THEN** `show-sql` permanece desabilitado e os pools usam limites finitos configurados

