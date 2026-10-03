# Proposal

## Why

Após a estabilização das TASKS 01–16, o backend possui autenticação, isolamento empresarial/setorial, Actuator, métricas e observabilidade, mas ainda precisa de uma revisão sistemática antes da homologação. A TASK 17 fecha lacunas de segurança e performance com evidência: inventário de endpoints, testes de IDOR e mass assignment, limites defensivos, revisão de queries/índices/N+1, baseline reprodutível e carga sintética sem inventar SLA de produção.

## What Changes

- Inventariar endpoints por método, autenticação, permissão, empresa, setor, financeiro, auditoria, paginação e limites, validando default deny.
- Reforçar autorização por objeto e isolamento de empresa/setor em recursos, uploads, relatórios, auditoria e documentos RAG.
- Revisar DTOs, validações, JWT, senhas, secrets, CORS, CSRF, HTTPS/forwarded headers e headers de segurança.
- Proteger paginação, ordenação, filtros, datas, uploads Excel/RAG, path traversal, formula injection e payloads abusivos.
- Revisar rate limiting de login/IA e proteção de endpoints pesados sem degradar o uso normal.
- Auditar SQL/JPQL/Specifications, queries críticas, índices, N+1, transações longas, concorrência e pool Hikari; aplicar somente otimizações justificadas por medição.
- Criar dataset sintético controlado e baseline de Dashboard, drill-downs, produção, qualidade, auditoria, relatórios, importação e IA mockada.
- Adotar uma ferramenta de carga, com cenários mínimos de login, Dashboard, drill-down e consulta de produção, observando latência, 5xx, JVM, GC, pool e queries.
- Validar migrations do zero e banco alvo quando disponível, sem editar migrations já aplicadas.
- Criar `docs/SECURITY_HARDENING.md`, `docs/PERFORMANCE_BACKEND.md` e checklist pré-homologação.

## Capabilities

### New Capabilities

- `security-hardening`: modelo de ameaças, default deny, autorização por objeto, isolamento empresarial/setorial, JWT, secrets, CORS/CSRF, headers, uploads, IA/RAG e Actuator.
- `api-hardening`: validação de DTOs, limites de paginação e filtros, ordenação allowlist, rate limiting, erros seguros, formula injection e proteção contra abuso.
- `database-performance`: revisão de SQL/JPQL, Specifications, índices, N+1, agregações, paginação, transações, concorrência, migrations e validação no banco alvo.
- `application-performance`: baseline e otimização medida de Dashboard, produção, qualidade, auditoria, relatórios, importação, IA/RAG, pools e cache condicionado por escopo.
- `load-validation`: dataset sintético, ferramenta e cenários de carga, métricas observadas, critérios técnicos baseados no baseline e limites documentados.
- `hardening-documentation`: inventário de endpoints, checklist pré-homologação, decisões de segurança, evidências de performance, vulnerabilidades e procedimentos reproduzíveis.

### Modified Capabilities

Nenhuma capability principal está registrada em `openspec/specs/`; as capacidades acima serão adicionadas como contratos transversais de segurança e performance, preservando as regras de negócio existentes.

## Impact

- `SecurityConfig`, filtros de autorização/correlation, recursos REST, DTOs, validações, tratamento de erros e configurações de CORS/headers.
- Repositórios JPA, Specifications, queries analíticas, índices/migrations, paginação, projeções/EntityGraphs e limites de transação.
- Upload Excel, documentos RAG, exportações CSV/XLSX, Dashboard, qualidade, auditoria, importação e Assistente IA.
- Testes de integração de segurança/IDOR, concorrência, performance, Testcontainers ou banco alvo quando disponível, análise de dependências/CVEs e ferramenta de carga.
- Documentação operacional e de homologação; nenhum frontend, redesign de domínio, cluster, Kubernetes, banco novo, modelo de IA ou pentest externo fazem parte desta mudança.
