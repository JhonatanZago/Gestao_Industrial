# Tasks

## 1. Dependências, configuração e persistência operacional

- [x] 1.1 Executar a suíte atual e registrar o baseline de `clean test` e `build` antes das alterações; verificar que qualquer falha preexistente esteja identificada sem desabilitar testes.
  - Baseline em 2026-09-28: `clean test` e `build` concluídos com sucesso; 113 testes executados, 0 falhas, 0 erros e 0 ignorados.
- [x] 1.2 Adicionar Actuator e Micrometer Prometheus ao Gradle, mantendo Resilience4j ausente enquanto não houver adapter remoto real; verificar resolução de dependências e inicialização do contexto sem Prometheus externo.
- [x] 1.3 Criar propriedades validadas para management, payload, shutdown, HikariCP e resiliência remota com defaults finitos; verificar por testes de binding que valores inválidos impedem startup e defaults não são excessivos.
- [x] 1.4 Separar configurações `dev`, `test` e `prod`, desabilitando DEBUG global e `spring.jpa.show-sql` em produção; verificar com testes de contexto que cada profile aplica a política esperada sem revelar secrets.
- [x] 1.5 Criar migration posterior à V21 para a permissão operacional e os campos opcionais de correlation ID/código/mensagem de erro da importação, sem editar migrations existentes; verificar Flyway em PostgreSQL compatível e H2 de teste.

## 2. Correlation ID, erros e logging estruturado

- [x] 2.1 Implementar o contexto e filtro inicial de `X-Correlation-Id` com regex `[A-Za-z0-9._-]{1,100}`, geração segura, MDC, response header e limpeza em `finally`; verificar valores válido, ausente, inválido e reutilização de thread.
- [x] 2.2 Posicionar o filtro antes da autenticação e permitir/expor o header na configuração CORS sem ampliar origens; verificar correlation ID em respostas autenticadas, 401, 403 e preflight autorizado.
- [x] 2.3 Evoluir o contrato de erro para timestamp, status, code, message, path, correlationId e fields opcionais; verificar serialização estável e preservação dos erros de campo permitidos.
- [x] 2.4 Revisar o advice global para mapear `VALIDATION_ERROR`, `RESOURCE_NOT_FOUND`, `ACCESS_DENIED`, `BUSINESS_RULE_VIOLATION`, `IMPORT_ERROR`, `EXTERNAL_SERVICE_UNAVAILABLE`, `AI_PROVIDER_UNAVAILABLE` e `INTERNAL_ERROR`; verificar 400/403/404/409/5xx e mensagem genérica no 500.
- [x] 2.5 Unificar authentication entry point e access denied handler com o contrato global; verificar que respostas da cadeia Spring Security incluem path e correlation ID sem formatos legados paralelos.
- [x] 2.6 Implementar evento estruturado de conclusão HTTP com método, rota normalizada, status e duração, usando configuração de logging apropriada por ambiente; verificar que URI com IDs não cria campo de alta cardinalidade e que bodies não são logados.
- [x] 2.7 Implementar sanitização e limites para valores externos em logs; verificar por captura de eventos que Authorization, JWT, cookie, API key, senha, prompts/payloads completos e caracteres de controle não aparecem.
- [x] 2.8 Registrar stack trace somente no log interno para exceção inesperada e um resumo seguro de startup; verificar que o log contém correlation ID e que response/startup não expõem SQL, paths locais, secrets ou configuração completa.

## 3. Health, probes, info e segurança Actuator

- [x] 3.1 Configurar a lista de exposição somente com health, info, metrics e prometheus, mantendo env/beans/heapdump/configprops indisponíveis; verificar por testes HTTP a superfície exata.
- [x] 3.2 Liberar health agregado e probes com detalhe mínimo e proteger info, metrics, prometheus e detalhes operacionais com a nova permissão; verificar acesso anônimo, usuário comum e administrador.
- [x] 3.3 Configurar liveness independente e readiness com indicador leve do banco; verificar processo vivo com IA indisponível e readiness fora de serviço quando o banco falha.
- [x] 3.4 Implementar estado operacional de IA/RAG como `DISABLED`, `AVAILABLE` ou `UNAVAILABLE` sem probe remoto caro nem secrets; verificar gateways disabled, fake/disponível e falha conhecida sem alterar liveness/readiness.
- [x] 3.5 Publicar nome, versão e build/commit seguro no info; verificar que credenciais, URLs sensíveis e propriedades completas não são retornadas.

## 4. Métricas operacionais de baixa cardinalidade

- [x] 4.1 Criar facade de métricas com nomes estáveis e tags enumeradas para resultado, formato, operação e tipo de falha; verificar no registry em memória que IDs, nomes livres, paths brutos e payloads não aparecem em meters ou tags.
- [x] 4.2 Validar métricas HTTP nativas de quantidade, status e duração por método/URI template; verificar incremento de 2xx, 4xx e 5xx sem IDs na tag de rota.
- [x] 4.3 Instrumentar importações com início, conclusão, rejeição/falha técnica, duração e linhas processadas; verificar cada caminho e garantir incremento terminal único sem nome de arquivo ou empresa como tag.
- [x] 4.4 Instrumentar relatórios JSON/CSV/XLSX e exportações com total, falha e duração; verificar sucesso/erro por formato sem registrar conteúdo ou dados financeiros.
- [x] 4.5 Instrumentar a consulta consolidada usada pelo Dashboard com resultado e duração; verificar sucesso e falha sem filtros ou entidades nas tags.
- [x] 4.6 Substituir a telemetria exclusivamente textual da IA por métricas de requests, falhas, timeout, duração, tool calls e tokens disponíveis, mantendo log correlacionado minimizado; verificar que pergunta, prompt, resposta, usuário, empresa e documento não viram tags.
- [x] 4.7 Configurar endpoint Prometheus opcional e protegido e validar métricas JVM/Hikari disponíveis; verificar scrape autorizado, negação anônima e inicialização sem servidor Prometheus/Grafana.

## 5. Importação com falha terminal confiável

- [x] 5.1 Separar criação do lote, processamento de dados e marcação de falha em fronteiras transacionais adequadas; verificar que rollback de registros de produção não remove o lote de controle.
- [x] 5.2 Persistir `FALHOU`, finalização, código/mensagem sanitizados e correlation ID em transação independente, preservando `REJEITADA`, `VALIDADA` e `CONCLUIDA`; verificar mapeamento e consulta do histórico sem quebra do enum público.
- [x] 5.3 Associar logs, auditoria e métricas da importação ao correlation ID e `importacaoId` como identificadores distintos; verificar rastreabilidade sem transformar nenhum deles em tag de métrica.
- [x] 5.4 Cobrir falha de leitura, validação funcional, falha durante gravação de linhas e sucesso; verificar estado terminal, rollback dos dados parciais, métrica única e ausência de lote preso em `VALIDANDO`/`IMPORTANDO`.

## 6. Resiliência, limites e encerramento

- [x] 6.1 Implementar classificação compartilhada para timeout e falhas HTTP transitórias versus 4xx/não idempotentes, com tentativas e backoff limitados e testáveis sem sleeps longos; verificar retry somente para timeout/502/503/504 de leituras idempotentes.
- [x] 6.2 Aplicar connect/read timeout e propagação de correlation ID na borda de qualquer adapter HTTP remoto existente; se nenhum adapter remoto existir, verificar e documentar que gateways fake/disabled não recebem circuito ou retry artificiais.
- [x] 6.3 Condicionar circuit breaker a adapter remoto real e expor seu estado operacional quando aplicável; verificar `CLOSED → OPEN → HALF_OPEN → CLOSED` com fake clock, ou, na ausência de adapter remoto, verificar explicitamente a não aplicabilidade prevista no design.
- [x] 6.4 Fortalecer o fallback da IA para indisponibilidade explícita, sem dados inventados nem impacto em produção, Dashboard e relatórios; verificar provider ausente, timeout e falha transitória esgotada.
- [x] 6.5 Implementar limite geral de payload e harmonizá-lo com multipart Excel e documentos RAG; verificar rejeição controlada antes de processamento parcial para Content-Length conhecido e leitura acima do limite.
- [x] 6.6 Configurar HikariCP e quaisquer executores com limites finitos e observáveis; verificar binding, métricas do pool e ausência de pool ilimitado ou retry de transação de banco.
- [x] 6.7 Configurar graceful shutdown e timeout de fase; verificar propriedades efetivas e documentar/testar o comportamento de requisições e importações em andamento sem prometer retomada automática.

## 7. Docker e documentação operacional

- [x] 7.1 Adicionar healthcheck da aplicação ao Dockerfile/Compose usando readiness e preservando a dependência do PostgreSQL saudável; verificar a configuração renderizada do Compose e o comando de probe disponível na imagem runtime.
- [x] 7.2 Criar `docs/OPERACAO_BACKEND.md` com endpoints Actuator, autorização, health, métricas, correlation ID, profiles, limites, shutdown e diagnóstico de IA/importação; verificar que exemplos não contêm secrets e correspondem à configuração implementada.
- [x] 7.3 Incluir no runbook os fluxos API 500, banco indisponível, IA indisponível, importação falha, latência elevada e circuito aberto/não aplicável; verificar que cada fluxo parte de um sinal observável e termina em ação segura ou rollback documentado.

## 8. Verificação integrada e regressão

- [x] 8.1 Executar a matriz integrada de health público/detalhado, liveness, readiness, banco, IA disabled/unavailable e segurança Actuator; verificar status, detalhes permitidos e ausência de secrets.
- [x] 8.2 Executar a matriz de correlation ID, logs e erros 400/401/403/404/409/500; verificar mesmo ID em response/MDC/log, mensagem genérica no 500 e stack trace somente interno.
- [x] 8.3 Executar Dashboard, importação, relatórios e IA contra registry de teste; verificar incrementos esperados, durações e inexistência de tags de alta cardinalidade.
- [x] 8.4 Executar cenários de timeout, retry seletivo, fallback e circuito quando aplicável, além de upload/payload excedido e falha transacional da importação; verificar encerramento controlado, ausência de dados inventados e nenhum lote preso.
- [x] 8.5 Executar `./gradlew clean test`, `./gradlew build` e `openspec validate observabilidade-resiliencia-backend --type change --strict`; verificar regressão verde das TASKS 01–15 e todos os artefatos OpenSpec válidos.
