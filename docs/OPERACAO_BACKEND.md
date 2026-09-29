# Operação do backend

Este documento descreve os sinais operacionais e as ações seguras para o backend do Sistema de Gestão Industrial. Os exemplos usam placeholders; não registre tokens, senhas, documentos, perguntas da IA nem payloads de produção em chamados ou logs.

## Endpoints operacionais

| Endpoint | Acesso | Finalidade |
| --- | --- | --- |
| `/actuator/health` | público | estado agregado, sem componentes |
| `/actuator/health/liveness` | público | processo Java vivo; não depende da IA |
| `/actuator/health/readiness` | público | aplicação pronta e banco acessível |
| `/actuator/info` | `OPERACAO_MONITORAR` | nome, versão, commit e Java, sem configuração completa |
| `/actuator/metrics` e `/actuator/metrics/{nome}` | `OPERACAO_MONITORAR` | catálogo e detalhe de métricas |
| `/actuator/prometheus` | `OPERACAO_MONITORAR` | scrape opcional no formato Prometheus |
| `/api/v1/operacao/status` | `OPERACAO_MONITORAR` | banco e estados seguros de IA/RAG |

`env`, `beans`, `heapdump` e `configprops` não são expostos. Use um JWT de operador no header `Authorization: Bearer <TOKEN>` para endpoints protegidos. O health público não apresenta detalhes internos.

Exemplo seguro:

```sh
curl -fsS http://localhost:8080/actuator/health/readiness
curl -fsS -H "Authorization: Bearer <TOKEN>" http://localhost:8080/actuator/metrics
```

Prometheus/Grafana são consumidores opcionais: a aplicação inicia e mantém métricas locais mesmo quando nenhum servidor de monitoramento está disponível. Para desabilitar o endpoint Prometheus, use `PROMETHEUS_ENABLED=false` e ajuste a exposição conforme a política do ambiente.

## Correlação, logs e erros

O cliente pode enviar `X-Correlation-Id` com 1 a 100 caracteres de `A-Z`, `a-z`, `0-9`, ponto, sublinhado ou hífen. Valores ausentes ou inválidos são substituídos. O mesmo ID volta no header e no contrato JSON de erro.

Os logs de produção são JSON e incluem timestamp, nível, logger, mensagem, `correlationId` e, no evento final HTTP, método, rota normalizada, status e duração. Procure primeiro pelo correlation ID. IDs de entidade, nomes de arquivo, usuários, empresas, filtros, prompts e documentos não são tags de métricas.

Erros inesperados retornam mensagem genérica e `INTERNAL_ERROR`; a stack trace fica somente no log interno. Falhas técnicas de importação usam `IMPORT_ERROR`. Nunca copie Authorization, JWT, cookie, API key, senha ou payload completo para diagnóstico.

## Métricas principais

- HTTP: `http.server.requests`, por método, template de URI, status e duração.
- Importação: `imports.started`, `imports.completed`, `imports.failed`, `import.duration` e `import.lines`.
- Relatórios: `report.generation`, `report.generation.failures` e `report.generation.duration`, por `json`, `csv` ou `xlsx`.
- Dashboard: `dashboard.requests` e `dashboard.request.duration`.
- IA: `ai.requests`, `ai.failures`, `ai.request.duration`, `ai.tool.calls` e `ai.tokens`.
- Runtime: métricas JVM e `hikaricp.*` para o pool de conexões.

As tags são enumeradas e de baixa cardinalidade. Não crie alertas que dependam de nomes de arquivo, IDs, paths brutos ou conteúdo de negócio.

## Profiles e limites

- `dev`: saída legível e nível da aplicação configurável.
- `test`: H2, Flyway e limites finitos para testes.
- `prod`: root/app em INFO, console JSON, SQL detalhado desabilitado e health sem detalhes.

Limites relevantes:

| Variável | Default | Observação |
| --- | --- | --- |
| `HTTP_MAX_PAYLOAD_BYTES` | 10 MiB | limite geral; rejeição HTTP 413 |
| `IMPORT_MAX_FILE_SIZE` / `IMPORT_MAX_REQUEST_SIZE` | 10 MB | multipart Excel, harmonizar com o limite geral |
| `IMPORT_MAX_LINHAS` | 10.000 | rejeita arquivo antes de gravar produção |
| `AI_MAX_DOCUMENT_BYTES` | 5 MiB | documento RAG |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | 10 / 2 | pool Hikari finito |
| `DB_CONNECTION_TIMEOUT_MS` | 30.000 ms | aquisição de conexão |
| `REMOTE_CONNECT_TIMEOUT` / `REMOTE_RESPONSE_TIMEOUT` | 2 s / 30 s | para futuro adapter HTTP remoto |
| `REMOTE_MAX_ATTEMPTS` / `REMOTE_BACKOFF` | 2 / 200 ms | somente leitura idempotente e falha transitória |
| `SHUTDOWN_TIMEOUT` | 30 s | espera por requisições em andamento |

O filtro geral rejeita `Content-Length` conhecido antes do controller. Para streams sem tamanho, a leitura não multipart é limitada; multipart também permanece sujeito aos limites do servlet. Mantenha os três limites de upload coerentes.

## Retry, timeout e circuito

Não existe adapter HTTP remoto real nesta versão. Os gateways de IA são `fake` (teste) ou `disabled`; por isso não há circuit breaker nem chamadas remotas às quais aplicar connect/read timeout. Também não foi adicionada dependência Resilience4j artificialmente.

A política compartilhada está pronta para um futuro adapter: somente leituras idempotentes repetem timeout, conexão e HTTP 502/503/504, com tentativas e backoff limitados. HTTP 4xx e escrita não idempotente não repetem. Quando um adapter remoto real for criado, ele deve:

1. aplicar os connect/read timeouts configurados;
2. propagar `X-Correlation-Id`;
3. usar a política de retry compartilhada;
4. adicionar e testar circuito `CLOSED → OPEN → HALF_OPEN → CLOSED` antes de anunciar o estado operacional.

Até lá, “circuito aberto” é **não aplicável**. IA indisponível retorna estado explícito, sem resposta inventada e sem derrubar liveness/readiness.

## Importação e shutdown

O lote de importação é criado em transação própria. A produção e sua auditoria são atômicas; se a gravação falhar, os dados parciais sofrem rollback e outra transação marca o lote `FALHOU`, com finalização, código seguro, mensagem genérica e correlation ID. `REJEITADA`, `VALIDADA` e `CONCLUIDA` continuam estados válidos.

O shutdown é `graceful`: novas requisições deixam de ser aceitas e o processo aguarda até `SHUTDOWN_TIMEOUT`. Uma importação que conclua dentro do prazo termina normalmente. Encerramento forçado depois do prazo pode interromper a requisição; não há retomada automática. Consulte o lote e reenvie o arquivo somente após confirmar que ele não está `CONCLUIDA`.

## Diagnóstico orientado por sinais

### API retorna 500

1. Capture somente o `X-Correlation-Id`, horário, rota e status.
2. Localize o evento `http_request` e a stack trace interna pelo correlation ID.
3. Verifique `http.server.requests` e dependências relacionadas.
4. Corrija a causa; não revele a exceção interna ao cliente.

### Banco indisponível

1. Observe readiness fora de serviço e métricas `hikaricp.*`.
2. Confirme saúde do PostgreSQL, conexões ocupadas e timeout do pool.
3. Restaure o banco; não aumente o pool sem medir capacidade do servidor.
4. Confirme readiness `UP` antes de recolocar a instância no balanceador.

### IA indisponível ou timeout

1. Consulte `/api/v1/operacao/status` e `ai.failures` por `unavailable`/`timeout`.
2. Confirme se IA está `DISABLED`, `AVAILABLE` ou `UNAVAILABLE`.
3. Em fake/disabled, circuito e adapter remoto são não aplicáveis.
4. Após esgotar retry transitório, preserve a resposta explícita de indisponibilidade; nunca fabrique dados.

### Importação falha

1. Pesquise `importacaoId` e correlation ID como identificadores distintos.
2. Confirme estado `REJEITADA` para conteúdo inválido ou `FALHOU` para falha técnica.
3. Verifique que `finalizadoEm`, `erroCodigo` e mensagem segura foram persistidos.
4. Confirme rollback de registros parciais antes de permitir novo envio.

### Latência elevada

1. Compare `http.server.requests`, `import.duration`, `report.generation.duration`, `dashboard.request.duration` e `ai.request.duration`.
2. Correlacione com uso JVM e saturação Hikari.
3. Identifique a operação pelo template de rota, não por path bruto.
4. Ajuste limites apenas depois de localizar o gargalo.

### Circuito aberto

No estado atual, valide primeiro que não houve implantação de um adapter remoto novo: o circuito é não aplicável. Se um adapter futuro expuser circuito aberto, interrompa retries manuais, aguarde a janela configurada, valide `HALF_OPEN` com tráfego controlado e só então confirme retorno a `CLOSED`.

## Containers

O Dockerfile instala `curl` e executa healthcheck na readiness. O Compose mantém a aplicação dependente do PostgreSQL saudável e também verifica `/actuator/health/readiness`. Valide a configuração com:

```sh
docker compose config
docker compose up --build
docker compose ps
```
