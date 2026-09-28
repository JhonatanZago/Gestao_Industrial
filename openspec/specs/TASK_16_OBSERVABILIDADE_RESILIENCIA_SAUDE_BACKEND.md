# TASK EXECUTÁVEL 16 --- Observabilidade, Resiliência e Saúde Operacional do Backend

## Instrução principal

Implemente esta task integralmente no **Sistema de Gestão e Inteligência
de Produção da Metalúrgica**, usando **OpenSpec + Codex**, somente após
estabilizar as TASKS 01--15.

Antes de programar: leia projeto e OpenSpec completos; execute testes;
confirme build; mapeie banco, Excel, relatórios, Dashboard, integrações
e IA/RAG; crie a change OpenSpec; preserve segurança/auditoria;
implemente observabilidade e resiliência; teste indisponibilidades;
execute regressão.

**Não implementar frontend. Não mascarar falhas com dados inventados.
Não registrar senhas, tokens, secrets ou payloads industriais completos.
Não iniciar a próxima task.**

## 1. Objetivo

Preparar o backend para operação real, permitindo responder:

``` text
A API está saudável?
O banco está disponível?
Qual endpoint está lento?
A importação falhou?
A IA está indisponível?
Há aumento de erros 5xx?
Qual requisição originou o erro?
```

## 2. OpenSpec

Criar change:

``` text
observabilidade-resiliencia-backend
```

Capabilities:

``` text
health-monitoring
metrics
structured-logging
request-correlation
resilience
integration-monitoring
```

Documentar health, readiness/liveness, métricas, logs, correlation ID,
erros, timeouts, retry, circuit breaker, integrações, segurança e
operação.

## 3. Actuator e saúde

Adicionar/configurar Spring Boot Actuator. Expor somente o necessário:

``` text
health
info
metrics
prometheus (se adotado)
```

Não expor publicamente endpoints sensíveis como `env`, `beans`,
`heapdump` e `configprops`.

Configurar: - **liveness**: processo está vivo; não deve cair porque IA
opcional está fora; - **readiness**: aplicação está pronta para
tráfego; - health leve do banco; - estado de IA/RAG como `DISABLED`,
`AVAILABLE`, `UNAVAILABLE` ou equivalente; - integrações opcionais
degradam somente sua funcionalidade.

Health público, se necessário, deve revelar apenas status mínimo.
Detalhes ficam protegidos.

## 4. Métricas

Usar Micrometer. Garantir métricas HTTP de requests, status, erros e
latência.

Criar métricas operacionais, por exemplo:

``` text
imports_started_total
imports_completed_total
imports_failed_total
import_duration_seconds

report_generation_total
report_generation_failures_total
report_generation_duration_seconds

ai_requests_total
ai_failures_total
ai_request_duration_seconds
ai_tool_calls_total
```

Não usar IDs de usuário, empresa, funcionário, peça ou nomes de arquivo
como tags de alta cardinalidade.

Se coerente, adicionar registry Prometheus e proteger
`/actuator/prometheus`.

## 5. Logs estruturados

Padronizar logs com:

``` text
timestamp
level
logger
message
correlationId
requestMethod
requestPath
status
durationMs
```

Nunca registrar:

``` text
senha
senhaHash
JWT
Authorization
refresh token
API key
cookie
secret
```

Não usar logs como banco de produção. Body HTTP fica desabilitado por
padrão ou rigorosamente sanitizado/limitado.

## 6. Correlation ID

Implementar filtro/interceptor para:

``` text
X-Correlation-Id
```

Gerar quando ausente. Validar tamanho/formato para impedir log
injection. Inserir no MDC e retornar no response.

Propagar quando aplicável para:

``` text
integrações HTTP
IA
processamentos internos
```

Relacionar tecnicamente com `importacaoId` e `aiRequestId`, sem
substituir IDs de negócio.

## 7. Erros

Revisar `@RestControllerAdvice`.

Response conceitual:

``` json
{
  "timestamp": "...",
  "status": 500,
  "code": "INTERNAL_ERROR",
  "message": "...",
  "path": "...",
  "correlationId": "..."
}
```

Códigos estáveis:

``` text
VALIDATION_ERROR
RESOURCE_NOT_FOUND
ACCESS_DENIED
BUSINESS_RULE_VIOLATION
IMPORT_ERROR
EXTERNAL_SERVICE_UNAVAILABLE
AI_PROVIDER_UNAVAILABLE
INTERNAL_ERROR
```

Em 500, cliente recebe mensagem genérica + correlation ID; stack trace
fica somente no log interno. Nunca expor SQL, paths locais ou detalhes
internos.

## 8. Resiliência

Avaliar Resilience4j somente para chamadas remotas reais.

Toda integração HTTP externa deve possuir:

``` text
connect timeout
response/read timeout
```

Retry apenas para falhas transitórias apropriadas, como
timeout/502/503/504. Não repetir automaticamente 400/401/403/422 ou
escrita não idempotente sem garantia. Usar backoff configurável.

Aplicar Circuit Breaker somente onde falhas repetidas possam degradar o
ERP:

``` text
CLOSED → OPEN → HALF_OPEN
```

Fallback nunca inventa dados. Exemplo: IA indisponível → assistente
indisponível, ERP continua funcionando.

Não aplicar retry cego a transações de banco.

## 9. Importação Excel

Se falha técnica ocorrer:

``` text
status = FALHA
erro sanitizado
correlationId disponível
```

Não deixar lote eternamente `PROCESSANDO`.

Medir duração, linhas processadas e sucesso/falha técnica sem usar nome
do arquivo como tag.

## 10. Relatórios e Dashboard

Medir geração JSON/CSV/XLSX sem logar conteúdo.

Medir latência da API consolidada do Dashboard. Essas métricas serão
usadas na TASK 17 para otimização.

## 11. IA/RAG

Medir, quando disponível:

``` text
latência
falhas
timeouts
tool calls
tokens
```

Nunca usar pergunta como tag de métrica e não registrar prompt completo
por padrão.

IA fora do ar não deve derrubar produção, Dashboard ou relatórios.

## 12. Ambientes

Definir estratégia de níveis:

``` text
dev
test
prod
```

Produção não deve operar com DEBUG global.

No startup, logar apenas dados seguros:

``` text
aplicação
versão
profile
Java
AI enabled/disabled
```

Configurar `/actuator/info` com nome/versão/build e, opcionalmente,
commit, sem secrets.

## 13. Graceful shutdown

Configurar encerramento gracioso:

``` text
parar novas requisições
→ aguardar requisições em andamento dentro do limite
→ encerrar
```

Documentar comportamento de importações em processamento. Não prometer
retomada automática se não existir.

## 14. Recursos

Revisar pools usados por async/IA/importação. Não criar pools
ilimitados.

Revisar HikariCP sem escolher números arbitrariamente enormes:

``` text
maximumPoolSize
minimumIdle
connectionTimeout
idleTimeout
maxLifetime
```

Expor métricas do pool quando disponíveis.

Em produção:

``` text
spring.jpa.show-sql=false
```

SQL detalhado somente para diagnóstico controlado.

## 15. Limites

Definir limites explícitos para:

``` text
upload Excel
documentos RAG
payload HTTP
```

Retornar erro controlado quando excedidos.

Reutilizar/fortalecer rate limiting da IA. Avaliar proteção de endpoints
pesados como exportação/relatórios sem bloquear uso normal.

## 16. Docker

Se Dockerfile/docker-compose existirem, adicionar healthcheck coerente.

Prometheus pode ser opcional no ambiente local. Grafana é opcional e não
deve virar requisito para executar o ERP.

## 17. Cenários obrigatórios

### Erro interno

Esperado:

``` text
HTTP 500
code = INTERNAL_ERROR
correlationId presente
sem stack trace no response
stack trace no log
```

### IA indisponível

Esperado:

``` text
Assistente degradado/indisponível
ERP principal operacional
métrica de falha incrementada
```

### Timeout externo

Esperado:

``` text
timeout controlado
sem thread presa
erro mapeado
métrica registrada
```

### Circuit breaker

Quando aplicável:

``` text
CLOSED → OPEN → HALF_OPEN → CLOSED
```

### Importação falha

Esperado:

``` text
lote = FALHA
correlationId
erro sanitizado
métrica incrementada
```

### Secrets

Garantir por testes/inspeção que `Authorization`, JWT, API key e senha
não aparecem em logs.

### Correlation

Request:

``` text
X-Correlation-Id: abc-123
```

Response, MDC e logs relacionados devem conter `abc-123`. Valor inválido
deve ser substituído/rejeitado conforme política.

### Métricas

Executar Dashboard, importação, relatório e IA e validar incremento das
métricas correspondentes.

## 18. Testes

Criar testes para: 1. health; 2. liveness; 3. readiness; 4. banco; 5. IA
disabled/unavailable; 6. métricas HTTP/customizadas; 7. correlation ID e
MDC; 8. propagação; 9. erros 400/403/404/500; 10. timeout; 11. retry;
12. circuit breaker; 13. falha de importação; 14. sanitização de logs;
15. segurança Actuator; 16. upload limit; 17. regressão.

Usar WireMock/MockWebServer/fakes/mocks ou equivalente. Não depender de
serviços externos reais nem usar sleeps longos.

## 19. Documentação operacional

Criar:

``` text
docs/OPERACAO_BACKEND.md
```

ou equivalente.

Explicar:

``` text
como verificar health
como localizar erro por correlation ID
como consultar métricas
como diagnosticar IA indisponível
como diagnosticar importação
quais endpoints Actuator existem
```

Adicionar runbook básico:

``` text
API retornando 500
Banco indisponível
IA indisponível
Importação falhou
Latência elevada
Circuit breaker aberto
```

## 20. Fora do escopo

NÃO implementar:

``` text
frontend
Kubernetes completo
ELK completo
Datadog/New Relic obrigatório
tracing distribuído completo
autoscaling
failover de banco
cluster
fila distribuída
nova IA
alterações de regras de negócio
```

## 21. Regressão e comandos

Todos os testes das TASKS 01--15 devem continuar passando.

Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar também validação oficial OpenSpec disponível.

## 22. Proibições

É proibido: - retornar UP para dependência essencial realmente
indisponível; - inventar fallback; - retry infinito; - logar
secrets/payload completo; - expor Actuator sensível; - usar tags de alta
cardinalidade; - `show-sql` em produção; - transformar 500 em 200; -
desabilitar testes; - concluir com build falhando.

## 23. Critérios de aceite

Concluir somente quando: 1. Actuator/health funcionarem; 2.
liveness/readiness funcionarem; 3. banco tiver health apropriado; 4.
IA/RAG puder degradar sem derrubar ERP; 5. métricas
HTTP/importação/relatório/IA funcionarem; 6. correlation ID + MDC
funcionarem; 7. correlation ID estiver nos erros; 8. tratamento global
estiver consistente; 9. 500 não expuser detalhes; 10. secrets não forem
logados; 11. timeouts existirem; 12. retry for seletivo; 13. circuit
breaker existir somente onde necessário; 14. fallback não inventar
dados; 15. importação não ficar presa após falha; 16. Actuator estiver
protegido; 17. limites de upload/payload existirem; 18. documentação
operacional existir; 19. regressão passar; 20. `clean test` passar; 21.
`build` passar; 22. OpenSpec refletir a implementação.

## 24. Entrega esperada do Codex

Ao concluir, apresentar: -
change/proposal/design/tasks/specs/validação; - Actuator, health,
liveness/readiness, métricas, Prometheus, logs e correlation ID; -
timeouts, retry, backoff, circuit breaker e fallbacks; - comportamento
de banco, Excel, relatórios, Dashboard, IA e RAG; -
segurança/sanitização; - testes; - localização do runbook; - resultados
de `clean test` e `build`; - somente pendências reais.

Não iniciar a próxima task.

## 25. Próxima etapa prevista

Após aprovação:

``` text
TASK 17 — Segurança, Performance e Hardening Final do Backend
```

Ela revisará:

``` text
autorização endpoint por endpoint
OWASP
CORS
headers
rate limits
validação
queries/índices
N+1
paginação
concorrência
testes de carga
Dashboard
importações
relatórios
IA
```

Depois:

``` text
TASK 18 — Homologação, Testes E2E, Documentação e Backend V1
```

Somente após a TASK 18 será iniciada a sequência do **frontend premium
dark**.
