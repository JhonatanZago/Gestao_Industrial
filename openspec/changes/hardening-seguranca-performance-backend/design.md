# Design

## Context

O backend já usa JWT stateless, filtro de autorização por empresa/setor, permissões por endpoint, Actuator protegido, limites operacionais, Micrometer e filtros de correlation ID. As listagens usam `Pageable` em vários recursos e o Dashboard atualmente carrega registros e agrega parte dos resultados na aplicação; uploads Excel usam Apache POI e RAG aplica limites e contexto de segurança. A revisão deve medir o comportamento atual antes de alterar queries ou configurações, preservando as decisões das TASKS 01–16.

## Goals / Non-Goals

**Goals:**

- Tornar a superfície de autorização, isolamento, validação e exposição operacional verificável endpoint por endpoint.
- Detectar e corrigir somente problemas de segurança, N+1, consultas, limites, transações e concorrência demonstrados por testes ou medições.
- Produzir baseline, dataset, perfil de carga e documentação reproduzíveis, sem transformar resultados locais em SLA.
- Manter o ERP read-only quando acessado pela IA, sem permitir que prompts alterem autorização ou dados.

**Non-Goals:**

- Frontend, redesign de domínio, Kubernetes, microserviços, cluster, troca de banco, novo modelo de IA ou pentest externo.
- Cache ou aumento de pool sem evidência de benefício.
- Alteração de regras industriais ou relaxamento de autenticação para facilitar benchmarks.

## Decisions

### Inventário e autorização

Será criado um inventário derivado dos `@RequestMapping`/controllers e confrontado com `SecurityConfig`, `RequestAuthorizationFilter`, `@PreAuthorize` e serviços de escopo. A matriz terá método, path template, autenticação, permissão, empresa, setor, financeiro, auditoria, paginação e limite. O teste de default deny usará uma rota deliberadamente não classificada e tentará IDs de outra empresa/setor em path, query e body.

Alternativas consideradas: confiar somente no filtro global, que pode não cobrir regras internas de objeto; ou testar apenas controllers isolados, que não evidencia a cadeia de segurança. A matriz e testes MockMvc preservam a defesa em profundidade.

### Validação e limites de API

Será usado um limite global já existente como base, complementado por limites específicos de página, datas, top-N, relatórios, Excel, documentos RAG e IA. A ordenação será allowlist por recurso, e o tratamento será uniforme para 400/413/429. DTOs continuarão explícitos; campos internos como empresa, auditoria, status e permissões não serão aceitos de clientes.

Alternativas consideradas: apenas truncar qualquer entrada ou aplicar um limite único muito baixo. A primeira mascara erros e a segunda quebra usos legítimos; limites por operação permitem defesa proporcional e compatibilidade.

### CORS, CSRF, HTTPS e headers

Será preservada a estratégia JWT stateless sem cookies de sessão, com CSRF documentado como desabilitado para essa fronteira e CORS vindo de configuração externa explícita. Serão testados `X-Content-Type-Options`, política de referrer, frame protection e CSP quando compatíveis com a API; HSTS ficará condicionado ao tráfego HTTPS efetivo e ao reverse proxy. Swagger/OpenAPI seguirá a política de não exposição administrativa não autorizada.

Alternativas consideradas: wildcard CORS ou habilitar credenciais por padrão. Ambas ampliam a superfície e não serão aceitas em produção.

### Uploads e documentos

O limite de tamanho/linhas continuará sendo aplicado antes da persistência. Para XLSX/ZIP será revisada a configuração de segurança do Apache POI/ZIP (entries, tamanho descompactado e taxa mínima de inflação), com rejeição controlada e sem usar o nome original como caminho. RAG terá a mesma política de tamanho, tipo, conteúdo, armazenamento e escopo; macros/scripts não serão executados.

Alternativas consideradas: confiar na extensão `.xlsx` ou fazer uma inspeção completa em memória. A extensão não é uma garantia e a segunda piora DoS; o plano combina MIME/estrutura, limites e leitura controlada.

### Queries, índices e transações

Primeiro será coletada uma matriz de endpoints críticos, query count, latência e plano no PostgreSQL quando disponível. Só depois serão escolhidos projections, fetch joins pontuais, `EntityGraph`, agregação SQL, batch fetching ou novos índices. Nenhuma associação será tornada globalmente `EAGER`. Migrations novas serão usadas para índices e nunca migrations existentes serão editadas.

Chamadas de IA/integração ficarão fora de transações abertas. Concorrência será testada nos fluxos de importação e nas configurações com risco real de lost update; `@Version` será aplicado somente se a evidência justificar.

Alternativas consideradas: carregar tudo e paginar em memória, criar índices em todas as colunas ou aumentar Hikari para esconder latência. Essas alternativas elevam custo e podem mascarar a causa, por isso ficam proibidas sem medição.

### Baseline e carga

Será criado um gerador programático de dataset sintético com volumes ajustáveis, sem arquivos gigantes versionados. O perfil padrão usará k6 para login, Dashboard, drill-down por setor/funcionário e consulta de produção; IA externa será substituída por fake. O relatório registrará p50/p95/p99, throughput, 4xx/5xx, resposta, CPU, memória, GC, Hikari e queries. Os objetivos serão relativos ao baseline e não serão apresentados como SLA empresarial.

Alternativas consideradas: benchmark manual ou ferramenta que exija um stack permanente. k6 permite scripts versionáveis e execução local/CI sem tornar Grafana obrigatório.

### Dependências e banco alvo

Será feita análise de dependências/CVEs com uma ferramenta compatível com Gradle e relatório de achados. H2 continuará rápida para a maior parte da suíte, enquanto um perfil PostgreSQL/Testcontainers será avaliado para migrations, constraints, queries nativas e planos que dependem do banco alvo. A validação do banco alvo será separada da suíte unitária para não aumentar o tempo de todo commit.

## Risks / Trade-offs

- [Risco] Limites mais estritos podem rejeitar clientes legítimos → Mitigação: medir o uso atual, documentar defaults e permitir configuração externa validada.
- [Risco] Alterar queries pode mudar ordenação, arredondamento ou escopo → Mitigação: testes de equivalência funcional e isolamento antes/depois para cada endpoint afetado.
- [Risco] Testes de carga locais não representam produção → Mitigação: registrar máquina, banco, dataset e limitações; usar apenas comparação relativa.
- [Risco] Proteções ZIP/POI podem rejeitar arquivos válidos incomuns → Mitigação: fixtures de compatibilidade, mensagem controlada e política configurável sem remover o limite.
- [Risco] Testcontainers/CVE scanners dependem de rede ou Docker → Mitigação: manter perfil opcional, registrar indisponibilidade como evidência e não substituir a regressão rápida.
- [Risco] CSP/HSTS podem afetar consumidores existentes ou TLS terminado fora da aplicação → Mitigação: validar headers em integração, condicionar HSTS ao proxy/HTTPS e documentar rollout.

## Migration Plan

1. Executar baseline e congelar o inventário/ameaças antes de qualquer otimização.
2. Adicionar testes de segurança e limites, mantendo os existentes verdes.
3. Executar dataset e medições; aplicar correções de autorização, validação, uploads e queries que tenham evidência.
4. Criar migrations novas somente para índices/constraints necessários e validar do zero em H2 e PostgreSQL quando disponível.
5. Executar o perfil k6 mockado, análise de dependências, regressão completa e atualizar os dois documentos obrigatórios.
6. Implantar com configuração gradual de limites/headers; rollback consiste em reverter a aplicação e migrations novas compatíveis, sem editar migrations já aplicadas.

## Open Questions

- O ambiente de homologação disponibilizará PostgreSQL e Docker para o perfil Testcontainers, ou será necessário registrar a validação do banco alvo como pendência operacional?
- O pipeline terá k6 instalado e poderá executar o perfil de carga, ou a execução ficará documentada como comando manual fora do CI?
