# Proposal

## Why

O backend já concentra importação Excel, relatórios, analytics, Dashboard e IA/RAG, mas ainda não oferece sinais operacionais consistentes para distinguir indisponibilidade, degradação, erro interno e lentidão. A TASK 16 prepara a API para operação real com saúde verificável, métricas, correlação de requisições e tratamento seguro de falhas, sem tornar integrações opcionais uma dependência do ERP principal.

## What Changes

- Adicionar Spring Boot Actuator e Micrometer com health, info, métricas HTTP, liveness/readiness e exposição Prometheus protegida e configurável.
- Representar banco como dependência essencial de readiness e IA/RAG como componente opcional com estados `DISABLED`, `AVAILABLE` ou `UNAVAILABLE`, sem derrubar as APIs principais.
- Padronizar logs estruturados e implementar `X-Correlation-Id` validado, propagado pelo MDC, retornado na resposta e associado tecnicamente a importações e requisições de IA.
- Unificar respostas de erro com códigos estáveis, path e correlation ID; falhas 500 retornarão mensagem genérica sem stack trace, SQL, paths locais ou detalhes internos.
- Instrumentar importações, relatórios/exportações, Dashboard e IA com contadores e duração, evitando tags de alta cardinalidade e conteúdo industrial, prompts ou nomes de arquivo.
- Garantir que falhas técnicas de importação persistam estado terminal sanitizado e não deixem lotes em processamento indefinidamente.
- Definir timeouts e retry seletivo para integrações remotas idempotentes; aplicar circuit breaker apenas a adapters remotos reais quando configurados, com fallback explícito que não inventa dados.
- Configurar graceful shutdown, limites de payload/upload, pools limitados, HikariCP e perfis `dev`, `test` e `prod`, mantendo SQL detalhado e DEBUG global desabilitados em produção.
- Adicionar healthcheck da aplicação ao Docker, sem tornar Prometheus, Grafana ou qualquer provedor externo requisito para executar o ERP.
- Criar testes determinísticos de saúde, segurança do Actuator, métricas, correlação, sanitização, falhas de importação, timeout/retry/circuit breaker e regressão, além do runbook `docs/OPERACAO_BACKEND.md`.

## Capabilities

### New Capabilities

- `health-monitoring`: health mínimo público, detalhes protegidos, liveness/readiness, banco, IA/RAG opcional, info seguro e healthcheck de container.
- `metrics`: métricas HTTP e operacionais de importação, relatórios, Dashboard e IA, com convenções de nomes, tags de baixa cardinalidade e endpoint Prometheus protegido.
- `structured-logging`: eventos estruturados e sanitizados, política por ambiente e proibição de secrets, payloads industriais completos e bodies HTTP por padrão.
- `request-correlation`: geração, validação, MDC, retorno e propagação de `X-Correlation-Id`, inclusive em erros e processamentos relacionados.
- `resilience`: timeouts, retry seletivo, backoff, circuit breaker condicionado a adapters remotos, fallback seguro, graceful shutdown e limites de recursos.
- `integration-monitoring`: observabilidade e estado terminal confiável para importações, relatórios/exportações, Dashboard, IA/RAG e demais integrações opcionais.

### Modified Capabilities

Nenhuma capability principal está registrada em `openspec/specs/`. A mudança adicionará contratos operacionais transversais sem alterar as regras de negócio existentes das APIs industriais.

## Impact

- Dependências Gradle para Actuator, Micrometer/Prometheus e, somente se houver adapter remoto real que justifique, Resilience4j.
- Configurações de management endpoints, probes, métricas, logging, shutdown, servidor, multipart, HikariCP e perfis da aplicação.
- Novos filtros/componentes de correlação, sanitização, health indicators, métricas e resiliência; revisão de `SecurityConfig`, `GlobalExceptionHandler` e `ErrorResponse`.
- Instrumentação de `ImportacaoProducaoService`, relatórios/exportações, analytics/Dashboard e `AssistenteService`/`AiTelemetry` sem usar IDs de empresa, usuário ou entidade como tags.
- Ajustes no fluxo transacional da importação para preservar falha terminal e correlation ID mesmo quando o processamento técnico falhar.
- Atualização de Dockerfile/Compose, testes de integração e documentação operacional; nenhum frontend, stack ELK, tracing distribuído completo, Kubernetes ou alteração de regra de negócio faz parte da mudança.
