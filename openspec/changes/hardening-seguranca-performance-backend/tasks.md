# Tasks

## 1. Baseline e inventário

- [x] 1.1 Executar a suíte de testes atual, validar a configuração efetiva do build e registrar baseline de latência/erros para os endpoints críticos; verificar com o comando de testes e um relatório versionado em `docs/`.
- [x] 1.2 Gerar a matriz de endpoints a partir dos controllers e confrontá-la com `SecurityConfig`, `RequestAuthorizationFilter`, `@PreAuthorize` e serviços de escopo; verificar que todo endpoint possui autenticação, permissão, isolamento, limites e política de paginação documentados.
- [x] 1.3 Executar análise de dependências/CVEs compatível com Gradle e registrar ferramenta, versão, limitações e achados; verificar que o relatório é reproduzível ou documenta a indisponibilidade do scanner.

## 2. Autorização e isolamento de objetos

- [x] 2.1 Cobrir com testes MockMvc o default deny para rota não classificada e o acesso a IDs de outra empresa/setor em path, query e body; verificar respostas de negação sem vazamento de dados.
- [x] 2.2 Auditar e corrigir autorização por objeto nos recursos, uploads, relatórios, auditoria e RAG, preservando empresa/setor e permissões atuais; verificar com testes positivos e negativos por perfil.
- [x] 2.3 Auditar DTOs, binders e atualizações para rejeitar mass assignment de empresa, setor, auditoria, status e permissões; verificar que campos internos não são alterados por payload do cliente.
- [x] 2.4 Validar que os fluxos de IA/RAG mantêm escopo do usuário e acesso somente leitura, mesmo diante de prompt ou documento tentando alterar instruções; verificar com testes de isolamento e ausência de operações de escrita.

## 3. Autenticação, headers e fronteira HTTP

- [x] 3.1 Revisar JWT, expiração, algoritmo, armazenamento de senhas e carregamento de segredos sem valores sensíveis em código/configuração versionada; verificar com testes de autenticação e inspeção de configuração.
- [x] 3.2 Corrigir enumeração de login e aplicar rate limiting/backoff coerente para autenticação e rotas sensíveis; verificar respostas uniformes e comportamento sob tentativas repetidas.
- [x] 3.3 Validar CORS por allowlist configurável, CSRF conforme a fronteira JWT stateless e tratamento de HTTPS/forwarded headers; verificar cenários de origem permitida, negada e proxy.
- [x] 3.4 Aplicar e testar `X-Content-Type-Options`, Referrer-Policy, proteção de frame, CSP compatível com a API e HSTS somente em HTTPS efetivo; verificar headers em respostas protegidas e públicas.
- [x] 3.5 Revisar exposição do Actuator, Swagger/OpenAPI e endpoints operacionais; verificar que apenas probes necessárias são públicas e que documentação/diagnóstico administrativo exige autorização.

## 4. Validação, limites e entradas perigosas

- [x] 4.1 Uniformizar validação de DTOs, datas, filtros, paginação, tamanho de página e sort allowlist por recurso; verificar respostas 400 para entradas inválidas e ausência de ordenação arbitrária.
- [x] 4.2 Aplicar limites explícitos para top-N, relatórios, payloads, linhas e documentos, incluindo resposta 413 quando aplicável; verificar que limites são configuráveis, positivos e não permitem valores ilimitados.
- [x] 4.3 Revisar SQL/JPA/native queries para parâmetros e escopo de tenant, incluindo filtros e ordenação; verificar com testes de injeção e inspeção das queries críticas.
- [x] 4.4 Neutralizar fórmula/injeção em exportações Excel e respostas tabulares sem alterar dados legítimos; verificar fixtures com células iniciadas por `=`, `+`, `-` e `@`.
- [x] 4.5 Aplicar rate limiting e proteção contra abuso/DoS nas operações caras, incluindo IA, relatórios, imports e consultas analíticas; verificar limites por usuário/empresa e respostas 429.

## 5. Uploads Excel e documentos RAG

- [x] 5.1 Endurecer validação de tipo, extensão, conteúdo, tamanho, número de linhas e nome de arquivo para uploads; verificar rejeição controlada de extensão falsa, caminho traversal e arquivo inválido.
- [x] 5.2 Proteger leitura XLSX/ZIP contra ZIP bomb, excesso de entries, tamanho descompactado e taxa de inflação, sem executar macros/scripts; verificar fixtures maliciosas e arquivos XLSX compatíveis.
- [x] 5.3 Garantir armazenamento fora de caminhos controlados pelo cliente e escopo empresa/setor no acesso a documentos RAG; verificar que nomes e referências não escapam do diretório permitido.
- [x] 5.4 Adicionar testes de regressão para importações válidas, limites de linhas/bytes e mensagens de erro seguras; verificar que nenhum arquivo parcialmente processado é persistido após rejeição.

## 6. Banco, consultas e concorrência

- [x] 6.1 Medir query count, latência e planos dos endpoints críticos usando o dataset disponível e PostgreSQL quando disponível; verificar relatório com evidência antes de escolher otimizações.
- [x] 6.2 Corrigir N+1 e carregamentos excessivos demonstrados em Dashboard, drill-down, produção, qualidade, auditoria e relatórios usando projections/fetch pontuais sem EAGER global; verificar query count e equivalência funcional.
- [x] 6.3 Substituir paginação/agregação em memória demonstradamente problemática por paginação/agregação limitada no banco, preservando escopo, ordenação e resultados; verificar testes de equivalência e memória.
- [x] 6.4 Criar apenas migrations novas para índices/constraints comprovados pelos planos; verificar aplicação do schema do zero e rollback/compatibilidade sem editar migrations existentes.
- [x] 6.5 Validar queries, migrations e constraints no perfil PostgreSQL/Testcontainers quando disponível e registrar pendência operacional quando Docker/PostgreSQL não estiverem disponíveis; verificar comando/documento reproduzível.
- [x] 6.6 Revisar transações para não mantê-las abertas durante IA/integrações e testar concorrência nos imports e fluxos com risco de lost update; aplicar `@Version` somente quando justificado e verificar com teste concorrente.

## 7. Desempenho da aplicação

- [x] 7.1 Otimizar Dashboard, qualidade, auditoria e relatórios somente com base no baseline, mantendo filtros, escopo e paginação; verificar p95/query count e testes funcionais antes/depois.
- [x] 7.2 Medir imports, geração de relatórios e chamadas de IA mockadas, corrigindo gargalos demonstrados sem bloquear threads ou transações por chamadas externas; verificar latência, timeout e comportamento de erro.
- [x] 7.3 Revisar Hikari, cache e limites de threads para valores finitos e coerentes com o ambiente, sem aumento especulativo de pool; verificar métricas e ausência de exaustão sob teste.

## 8. Dataset e validação de carga

- [x] 8.1 Implementar gerador programático de dataset sintético ajustável para uma empresa, setores, funcionários, peças, máquinas, produção, qualidade, auditoria e documentos; verificar execução idempotente sem arquivos gigantes versionados.
- [x] 8.2 Criar perfil k6 versionado para login, Dashboard, drill-down por setor/funcionário e produção, usando IA fake; verificar que o script valida status, payload e isolamento.
- [x] 8.3 Executar carga com o dataset padrão quando k6 estiver disponível e registrar p50/p95/p99, throughput, 4xx/5xx, resposta, CPU, memória, GC, Hikari e queries; se indisponível, registrar o comando e a pendência sem apresentar SLA.
- [x] 8.4 Reexecutar o perfil após as correções e comparar com o baseline relativo, documentando máquina, banco, dataset, limites e conclusões; verificar que regressões de segurança ou funcionalidade bloqueiam a conclusão.

## 9. Documentação e integração final

- [x] 9.1 Criar `docs/SECURITY_HARDENING.md` com matriz de endpoints, ameaças cobertas, limites, headers, uploads, IA read-only, scanner e procedimento de rollout/rollback; verificar que os comandos documentados são executáveis.
- [x] 9.2 Criar `docs/PERFORMANCE_BACKEND.md` com dataset, baseline, query plans, decisões, perfil k6, métricas, limitações e pendências PostgreSQL/Docker/k6; verificar reprodução em ambiente limpo.
- [x] 9.3 Publicar checklist pré-homologação cobrindo autenticação, isolamento, entradas, uploads, Actuator/OpenAPI, CVEs, migrations, carga e observabilidade; verificar que cada item aponta para teste, comando ou evidência.
- [x] 9.4 Executar regressão completa, build limpo, validação OpenSpec estrita e inspeção final de mudanças; verificar `clean test`, `build`, `openspec validate --strict` e que todas as caixas desta change estão marcadas somente após a evidência correspondente.
