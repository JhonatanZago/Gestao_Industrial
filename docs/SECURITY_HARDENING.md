# Security hardening

## Revisão de homologação Backend V1

Os testes finais de autenticação, isolamento, auditoria, IA/RAG, headers, uploads, ZIP/XLSX, formula/path traversal e rate limiting foram reexecutados na change `homologacao-backend-v1`. A suíte não depende de internet ou API paga; pendências de scanner dedicado e imagem Docker desatualizada permanecem explícitas no relatório de homologação.

## Estado da evidência

Esta revisão cobre o backend V1 e preserva as regras industriais existentes. A suíte atual foi executada em 29/09/2026 com `gradlew.bat --no-daemon test` e terminou com sucesso. A validação de runtime depende de ambiente configurado com banco e credenciais de teste; nenhum segredo é registrado neste documento.

## Inventário de superfície

Os controllers abaixo são a fonte do inventário. Rotas `/api/v1/auth/**` são a fronteira de autenticação; probes de saúde são públicas; demais rotas exigem JWT e passam pelo `RequestAuthorizationFilter`. Rotas empresariais exigem pertencimento à empresa; quando há `setorId` no path ou query, o escopo setorial também é verificado.

| Área | Base | Autorização/política | Limites esperados |
|---|---|---|---|
| Auth | `/api/v1/auth` | login público; `me` e senha autenticados | DTO de senha/email |
| Empresas | `/api/v1/empresas` | `EMPRESA_VISUALIZAR/GERENCIAR`; empresa é sensível | paginação |
| Usuários | `/api/v1/empresas/{empresaId}/usuarios` | `USUARIO_VISUALIZAR/GERENCIAR` | paginação; sem campos internos |
| Setores | `/api/v1/empresas/{empresaId}/setores` | `SETOR_VISUALIZAR/GERENCIAR`; escopo de setor | paginação |
| Cadastros | `/funcionarios`, `/pecas`, `/maquinas`, `/turnos`, `/motivos-parada` | permissão por recurso; empresa/setor | paginação |
| Produção | `/producoes`, `/paradas` | produção/parada; empresa/setor | paginação e datas |
| Importação | `/setores/{setorId}/importacoes/producao`, `/importacoes/producao` | importação/visualização; setor | multipart, linhas e páginas |
| Analytics | `/analytics/**` | `ANALYTICS_VISUALIZAR` e financeiro/desempenho quando aplicável | janela, top-N e páginas |
| Qualidade | `/qualidade-dados/**` | `QUALIDADE_DADOS_VISUALIZAR` | janela, registros e páginas |
| Relatórios | `/relatorios/**` | `RELATORIO_VISUALIZAR` ou financeiro | período, top-N, linhas e exportação |
| Auditoria | `/auditoria/**` | `AUDITORIA_VISUALIZAR` | filtros e paginação |
| IA/RAG | `/assistente/**` | analytics; documentos exigem `AI_DOCUMENTO_GERENCIAR` | pergunta, documentos, top-K e rate limit |
| Operação | `/api/v1/operacao/**` | `OPERACAO_MONITORAR`; Actuator administrativo protegido | payload operacional finito |

Qualquer rota empresarial que não seja reconhecida pelo filtro recebe `403` antes da operação. A matriz detalhada deve ser atualizada junto com cada novo controller.

## Controles atuais e decisões

- JWT é stateless, assinado com HS256 e requer segredo externo com pelo menos 32 bytes.
- Senhas usam encoder BCrypt; respostas e tokens não contêm senha ou hash.
- CORS aceita somente `security.cors.allowed-origins`; wildcard e credenciais não são permitidos.
- CSRF permanece desabilitado porque a API usa bearer token stateless, sem sessão/cookie de autenticação; o tráfego de produção deve ser HTTPS.
- Actuator expõe apenas health, info, metrics e prometheus; detalhes de health não são expostos.
- Erros externos usam correlation ID e não devem incluir stack trace, SQL, paths locais, tokens ou secrets.
- IA/RAG recebe contexto de empresa/setor e deve executar somente ferramentas de leitura autorizadas.
- Uploads têm limites de multipart e de linhas; a leitura XLSX deve validar estrutura e não usar nome do cliente como caminho.

## OWASP, rollout e rollback

O review cobre Broken Access Control (default deny, empresa/setor e objetos), Injection (queries parametrizadas, sort allowlist e fórmula em exportação), Security Misconfiguration (CORS, headers e Actuator), Identification/Authentication (JWT, BCrypt e rate limit) e arquivos não confiáveis (limites de upload e ausência de integração arbitrária). Pentest externo não faz parte desta change.

O rollout recomendado é gradual: validar limites e headers em homologação, observar 4xx/429 e correlation IDs, e então promover a configuração externa. O rollback consiste em reverter a aplicação e migrations novas compatíveis; não se editam migrations já aplicadas e não se desabilita isolamento para facilitar benchmark.

## Checklist pré-homologação

- [ ] Cada método/path novo foi adicionado ao inventário e ao teste de acesso.
- [ ] Tentativas cross-company/cross-sector em path, query e body retornam negação sem vazamento.
- [ ] DTOs rejeitam mass assignment de empresa, setor, status, auditoria e permissões.
- [ ] JWT inválido/expirado, login inválido e mudança de senha têm respostas uniformes.
- [ ] CORS, CSRF, HTTPS/forwarded headers e headers de segurança foram verificados.
- [ ] Actuator, OpenAPI e operação não expõem detalhes sem autorização.
- [ ] Paginação, datas, top-N, uploads, ZIP/XLSX e fórmula injection têm limites e testes.
- [ ] SQL/JPQL/Specifications são parametrizados e preservam tenant/sector scope.
- [ ] Migrations novas foram testadas do zero; índices são justificados por plano.
- [ ] N+1, CVEs, carga, Hikari, GC, resposta e erros possuem evidência registrada.
- [ ] Logs e documentação foram revisados para ausência de secrets e tokens.

## Limitações abertas

Testcontainers/PostgreSQL, k6 e scanner de CVEs dedicado dependem das ferramentas disponíveis no ambiente. Quando indisponíveis, a execução deve ser registrada como pendência operacional, nunca convertida em aprovação implícita.

## Revisão de queries

As queries `@Query` encontradas usam parâmetros nomeados e escopo por empresa; filtros dinâmicos usam `Specification` e a ordenação recebida pela API passa pela allowlist global. Não foi encontrada concatenação de entrada em JPQL/SQL. A confirmação final de planos e volume depende do dataset sintético e do PostgreSQL.
