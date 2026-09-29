# Design

## Context

O backend usa Spring Boot 4.1.1, Java 17, Spring MVC/Security, JPA/Flyway, PostgreSQL/H2 e execução síncrona. O `build.gradle` ainda não inclui Actuator nem registry de métricas; `application.properties` possui limites de multipart e IA, mas não define probes, graceful shutdown, política de logging, limites gerais de payload ou HikariCP. O Docker Compose verifica apenas o PostgreSQL.

`SecurityConfig` libera `/actuator/health`, embora Actuator ainda não esteja instalado, e protege o restante com JWT e permissões. O `GlobalExceptionHandler` não possui fallback para exceções inesperadas, e `ErrorResponse` não contém código estável nem correlation ID; respostas produzidas diretamente pela segurança também seguem outro formato.

A importação é executada em uma única transação. Falhas de leitura tratadas viram `REJEITADA`, mas uma exceção técnica após a criação do lote pode reverter a própria linha de controle e impedir a persistência de `FALHOU`. Relatórios, Dashboard e importações não possuem métricas próprias. `AiTelemetry` registra somente log e inclui IDs de empresa/usuário; o retry da IA é limitado, porém imediato e sem política compartilhada de timeout/backoff. Os gateways atuais são fake ou disabled, sem adapter HTTP remoto real.

## Goals / Non-Goals

**Goals:**

- Introduzir uma camada operacional coerente sem alterar cálculos ou regras de negócio.
- Tornar saúde, métricas, logs, erros e correlation ID consistentes em filtros, segurança, controllers e integrações.
- Separar falha de dependência essencial de degradação opcional.
- Preservar de forma transacional o resultado técnico da importação.
- Manter testes locais determinísticos e o ERP inicializável sem IA, Prometheus ou serviços externos.

**Non-Goals:**

- Implantar coletor Prometheus, Grafana, ELK, tracing distribuído, Kubernetes, fila, cluster ou failover de banco.
- Criar um novo provider de IA ou uma integração HTTP fictícia apenas para justificar circuit breaker.
- Medir regras de negócio com labels de empresa, usuário ou entidade.
- Prometer retomada de importações interrompidas ou processamento assíncrono nesta change.

## Decisions

### 1. Actuator com superfície permitida explícita

Serão adicionados Actuator e o registry Prometheus. A exposição web ficará limitada a `health`, `info`, `metrics` e `prometheus`; qualquer outro endpoint permanece fora da lista, mesmo para usuário autenticado. O health agregado e os probes de liveness/readiness serão públicos com detalhe mínimo para healthcheck. `info`, `metrics`, `prometheus` e detalhes operacionais exigirão uma permissão administrativa nova, concedida inicialmente ao perfil administrador por migration aditiva.

Liveness usará somente o estado interno da aplicação. Readiness incluirá a disponibilidade leve do banco. IA/RAG será publicada como componente opcional com estado próprio e não participará negativamente dos grupos de liveness/readiness. A checagem de IA não fará chamada cara ao provedor a cada consulta; usará configuração, tipo do adapter e último estado/circuito conhecido.

Alternativa rejeitada: liberar todos os endpoints Actuator atrás do JWT. Isso ainda ampliaria desnecessariamente a superfície e facilitaria exposição futura de `env`, `beans`, `heapdump` ou `configprops`.

### 2. Correlation ID no primeiro filtro da cadeia

Um filtro de requisição executado antes da autenticação aceitará somente `[A-Za-z0-9._-]{1,100}`; valores ausentes ou inválidos serão substituídos por UUID. O filtro colocará o valor no atributo da request e no MDC, adicionará o header da resposta e limpará o MDC em `finally`. Dessa forma, erros de autenticação e autorização também recebem correlação.

O mesmo componente emitirá um único evento de conclusão com método, rota normalizada, status e duração. A configuração CORS aceitará e exporá `X-Correlation-Id`. Adapters HTTP futuros copiarão o header; serviços internos acessarão uma pequena abstração de contexto em vez de ler o MDC diretamente. `importacaoId` e outros IDs continuam separados.

Alternativa rejeitada: confiar apenas no MDC preenchido no controller, pois isso perderia erros ocorridos antes do MVC e poderia vazar valor entre threads reutilizadas.

### 3. Um contrato de erro para MVC e Spring Security

`ErrorResponse` evoluirá para `timestamp`, `status`, `code`, `message`, `path`, `correlationId` e campos opcionais de validação. O advice mapeará validação, recurso ausente, acesso negado, regra de negócio, importação, dependência externa, IA e erro interno para códigos estáveis. O handler genérico registrará stack trace internamente e sempre devolverá mensagem neutra.

Authentication entry point e access denied handler usarão o mesmo serializador/contrato. Mensagens técnicas de banco, SQL, filesystem e providers nunca serão copiadas para o response.

Alternativa rejeitada: manter respostas especiais com `{ "erro": ... }` na segurança, pois clientes e operação não conseguiriam tratar falhas de forma uniforme.

### 4. Métricas centralizadas e labels fechados

Uma facade operacional centralizará contadores, timers e summaries para importações, relatórios, Dashboard e IA. Os nomes internos seguirão convenção Micrometer em pontos, exportados pelo Prometheus com underscores. Tags serão enums controlados, por exemplo `outcome`, `format`, `operation` e `failure_type`; empresa, usuário, arquivo, path bruto, IDs e texto livre serão proibidos.

Métricas HTTP nativas serão mantidas com URI template normalizada. HikariCP e JVM serão fornecidos pela instrumentação padrão. Testes consultarão o registry em memória para validar incremento e cardinalidade, sem depender de servidor Prometheus.

Alternativa rejeitada: instrumentar cada serviço diretamente com nomes e tags livres, pois isso aumenta duplicação e risco de explosão de séries.

### 5. Logging estruturado via suporte nativo e política de sanitização

Produção usará o formato estruturado suportado pelo Spring Boot/Logback, com campos do MDC; desenvolvimento poderá manter saída legível. Bodies HTTP permanecerão desabilitados. Uma função comum limitará e neutralizará texto externo usado em eventos específicos. Testes capturarão eventos para verificar ausência de padrões de JWT, Authorization, API key, senha e caracteres de controle.

Eventos de domínio poderão registrar IDs técnicos necessários para diagnóstico, mas nunca como tags de métrica nem com payload completo. O evento genérico HTTP usará rota normalizada, não URI com IDs.

Alternativa rejeitada: adicionar um stack de logging externo obrigatório, incompatível com a exigência de execução local autônoma.

### 6. Ciclo da importação dividido em transações

Será criada uma migration posterior à V21 com colunas opcionais `correlation_id`, `erro_codigo` e `erro_mensagem` na importação. A criação do lote e a marcação de falha serão executadas em transações independentes da transação que grava registros de produção. Se o processamento falhar, a transação de dados é revertida e uma transação curta marca o lote como `FALHOU`, define `finalizadoEm` e grava erro sanitizado/correlation ID.

Rejeições funcionais de arquivo continuam `REJEITADA`; validação sem persistência continua `VALIDADA`; sucesso continua `CONCLUIDA`. O enum público existente `FALHOU` será preservado para evitar quebra de contrato. A instrumentação termina uma vez por caminho e usa contagens, não nome do arquivo como label.

Alternativa rejeitada: capturar toda exceção dentro da transação atual e salvar o status nela, pois a transação pode estar marcada para rollback e remover o próprio registro de falha.

### 7. Resiliência pertence ao adapter remoto

Connect/read timeout, retry, backoff e circuit breaker serão configurados na borda HTTP, não em Services de domínio ou repositórios. Retry será permitido somente em leituras idempotentes e nas falhas transitórias enumeradas. Escritas sem idempotência, respostas 4xx e banco não terão retry automático.

Como o código atual não possui adapter remoto real de IA, a implementação não criará um circuito artificial para os gateways fake/disabled. Serão definidos contratos/configuração e testes da política de classificação; quando um adapter HTTP real existir, ele deverá aplicar a política e expor seu estado. Se um adapter remoto já for introduzido durante a execução desta change por dependência necessária, Resilience4j poderá ser usado nele e somente nele.

O fallback da IA retorna indisponibilidade explícita. Nunca será convertido em dado empresarial estimado nem em HTTP 200 enganoso para erros gerais.

### 8. Perfis, limites e encerramento

Configurações comuns conterão defaults conservadores parametrizados por ambiente. `prod` fixará DEBUG global e `spring.jpa.show-sql` como desabilitados; `dev` poderá elevar logs específicos; `test` manterá probes e métricas verificáveis com H2. Multipart Excel e documentos RAG conservarão limites próprios, enquanto um filtro de payload rejeitará requests gerais acima do limite antes do parsing sempre que o tamanho for conhecido e imporá leitura limitada quando necessário.

HikariCP terá tamanho máximo, mínimo ocioso, connection timeout, idle timeout e max lifetime configuráveis, sem defaults excessivos. Qualquer executor adicionado será limitado; o backend atual permanece síncrono. Graceful shutdown usará timeout de fase explícito. Como importações são síncronas, uma importação poderá concluir dentro da janela; ao excedê-la, não há promessa de retomada automática e o runbook explicará a recuperação.

### 9. Docker depende da readiness da aplicação

O container da aplicação ganhará healthcheck usando a readiness. O Compose manterá a dependência do PostgreSQL saudável e passará a refletir também a saúde da API. A imagem runtime deverá conter uma ferramenta de probe ou usar um mecanismo disponível no JRE, sem instalar uma plataforma de monitoramento.

### 10. Documentação e verificação operacional fazem parte da entrega

`docs/OPERACAO_BACKEND.md` documentará endpoints expostos, autorização, métricas, correlation ID, estados da IA, comportamento de importação e runbooks. Testes de integração cobrirão filtros, Security handlers, probes e registry; testes unitários cobrirão sanitização e classificação de resiliência. Fakes/mocks substituirão serviços externos e não haverá sleeps longos.

## Risks / Trade-offs

- [Health detalhado pode revelar topologia] → manter resposta pública agregada, permissão operacional e lista de endpoints fechada por configuração e testes.
- [Métrica com tag livre pode causar alta cardinalidade] → facade com enums, revisão automática dos meters registrados e proibição explícita de IDs/texto livre.
- [Nova separação transacional da importação pode alterar rollback] → testar falha antes e depois da gravação de linhas, garantindo rollback dos dados e persistência exclusiva do estado terminal.
- [Log estruturado aumenta volume] → um evento por request, sem body, níveis por ambiente e eventos de domínio somente em transições relevantes.
- [Probe de banco pode amplificar incidente] → usar indicador leve padrão e evitar queries industriais.
- [Shutdown pode interromper importação longa] → timeout configurável, status recuperável/correlacionável e documentação honesta sem retomada automática.
- [Política de circuito sem adapter remoto ativo não exercita transições em produção] → não criar dependência fictícia; testar a política e exigir o circuito no momento em que um adapter remoto real for habilitado.

## Migration Plan

1. Adicionar dependências e configurações com exposição restrita e IA desabilitada por padrão.
2. Aplicar migration aditiva de permissão operacional e metadados de falha/correlação da importação; nenhuma migration existente será editada.
3. Introduzir correlation ID e contrato de erros, atualizando conjuntamente Security handlers e testes de regressão.
4. Ativar métricas, health indicators e instrumentação dos fluxos críticos; validar ausência de tags proibidas.
5. Separar as transações da importação e executar cenários de rollback/falha terminal.
6. Habilitar perfis, graceful shutdown e healthcheck Docker; publicar o runbook antes de produção.
7. Em rollback de aplicação, manter as colunas e permissão aditivas no banco. Se necessário, desabilitar Prometheus e detalhes operacionais por configuração sem retirar o health mínimo.
