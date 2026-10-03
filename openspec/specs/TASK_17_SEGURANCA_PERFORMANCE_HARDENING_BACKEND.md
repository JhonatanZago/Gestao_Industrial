# TASK EXECUTÁVEL 17 — Segurança, Performance e Hardening Final do Backend

## Instrução principal
Implemente esta task integralmente no **Sistema de Gestão e Inteligência de Produção da Metalúrgica**, usando **OpenSpec + Codex**, somente após estabilizar as TASKS 01–16.

Antes de programar: leia projeto/OpenSpec completos; rode `clean test` e `build`; inventarie endpoints, permissões, queries e índices; revise Dashboard, Excel, relatórios, auditoria, qualidade, IA/RAG e observabilidade; crie a change OpenSpec; meça antes de otimizar; execute testes de segurança, carga e regressão.

**Não implementar frontend. Não reescrever o domínio. Não otimizar sem evidência. Não desabilitar segurança para ganhar performance. Não iniciar a TASK 18 automaticamente.**

## 1. Objetivo
Realizar a revisão final de segurança e performance antes da homologação do Backend V1:

```text
Backend completo
   ├── Segurança endpoint por endpoint
   ├── Isolamento empresa/setor
   ├── Hardening OWASP
   ├── Uploads seguros
   ├── IA/RAG segura
   ├── Queries e índices
   ├── N+1
   ├── Paginação
   ├── Concorrência
   └── Testes de carga
```

## 2. OpenSpec
Criar change:
```text
hardening-seguranca-performance-backend
```
Capabilities:
```text
security-hardening
api-hardening
database-performance
application-performance
load-validation
```
Documentar modelo de ameaças, superfície de ataque, autenticação/autorização, CORS/CSRF, headers, validação, uploads, rate limiting, Actuator, IA/RAG, SQL/JPA, índices, N+1, paginação, concorrência e benchmarks.

## 3. Inventário e default deny
Inventariar TODOS os endpoints com:
```text
método
path
autenticação
permissão
empresa
setor
financeiro
auditoria
paginação/limite
```
Adotar **NEGAR POR PADRÃO**. Somente endpoints explicitamente públicos (ex.: login e health mínimo) podem ficar sem autenticação.

## 4. Autorização por objeto / IDOR
Testar alteração de IDs em URL/body para acessar outro recurso. Validar empresa, setor, funcionário, peça, máquina, documento RAG, relatório, importação e auditoria quando aplicável.

Esperado para recurso não autorizado:
```text
403 ou 404 conforme política definida
```
Nunca retornar o recurso.

## 5. Mass assignment
DTOs não podem aceitar alteração arbitrária de:
```text
empresaId interno
criadoPor
auditoria
roles/permissões
status protegido
campos calculados
```
Usar DTOs explícitos.

## 6. JWT, senhas e secrets
Revisar assinatura, algoritmo, expiração, validação, issuer/audience se usados, refresh/revogação quando aplicável. Rejeitar tokens expirados, malformados e inválidos.

Garantir hash forte de senha e ausência de senha/hash em DTO/log.

Procurar secrets reais em Git/config/README/OpenSpec/logs/testes/Docker. Manter apenas placeholders.

## 7. Login e enumeração
Avaliar rate limit contra brute force. Não criar bloqueio permanente inseguro. Mensagens não devem facilitar enumeração de usuários.

## 8. CORS / CSRF / HTTPS
CORS configurável externamente, por exemplo:
```text
APP_CORS_ALLOWED_ORIGINS
```
Não usar `*` com credenciais em produção.

Documentar decisão de CSRF conforme estratégia JWT/cookies.

Produção deve pressupor HTTPS; documentar forwarded headers se TLS terminar em reverse proxy.

## 9. Headers de segurança
Revisar quando aplicáveis:
```text
X-Content-Type-Options
Referrer-Policy
Content-Security-Policy
frame-ancestors / X-Frame-Options
Strict-Transport-Security
```
Sem quebrar OpenAPI/Swagger sem análise.

## 10. Validação e paginação
Revisar todos os DTOs:
```text
null
tamanho
formato
ranges
datas
enums
quantidades
IDs
```
Toda listagem grande deve ser paginada/limitada. Definir `size` default e máximo seguro. Ordenação somente por allowlist.

## 11. SQL Injection
Revisar native SQL, JPQL, Specifications e filtros. Nunca concatenar entrada do usuário em SQL. IA continua proibida de executar SQL livre.

## 12. Upload Excel e RAG
Validar tamanho, tipo, estrutura, nome e limite de linhas. Não confiar somente em `.xlsx`.

Como XLSX é ZIP, aplicar proteções contra zip bomb quando a biblioteca permitir: tamanho descompactado, quantidade de entries e compression ratio.

Evitar path traversal; nome do usuário nunca define caminho arbitrário.

Aplicar proteções equivalentes aos documentos RAG. Não executar macros/scripts.

## 13. Formula Injection
Em CSV/XLSX exportado, sanitizar conteúdo controlável iniciado por:
```text
=
+
-
@
```
quando houver risco de interpretação como fórmula.

## 14. IA/RAG
Reexecutar testes de:
```text
prompt injection
document injection
tool parameter injection
cross-company retrieval
cross-sector retrieval
```
Autorização permanece no backend.

Confirmar que IA é READ-ONLY e nenhuma tool altera/exclui/cadastra dados.

## 15. SSRF
Se houver URL dinâmica, restringir protocolos/hosts. Preferir endpoints configurados no servidor. Bloquear acesso arbitrário a localhost, metadata services, redes privadas e `file://` quando aplicável.

## 16. Actuator e erros
Health público deve ser mínimo; metrics/prometheus restritos; `env`, `heapdump` etc. não públicos.

Erros não podem revelar stack trace, SQL, tabela, path local, token ou secret. Correlation ID permanece disponível.

## 17. Rate limiting / DoS
Avaliar limites para:
```text
login
IA
relatórios/exportações
uploads
```
Revisar também:
```text
payload máximo
page size
top N
intervalo de datas
timeout
AI max tokens
RAG top K
```
Não permitir consultas pesadas ilimitadas por acidente.

## 18. Baseline de performance
Antes de otimizar, medir:
```text
Dashboard principal
Dashboard setor
Dashboard funcionário
Produções
Qualidade
Auditoria
Relatórios
Importação
Assistente IA mockado
```
Registrar massa, latência, quantidade aproximada de queries e ambiente. Não apresentar máquina local como SLA de produção.

## 19. Dataset sintético
Criar fixture/gerador razoável, por exemplo:
```text
1 empresa
10 setores
500 funcionários
1.000 peças
100 máquinas
100.000 produções
20.000 paradas
metas
tempos padrão
valores industriais
auditoria
```
Ajustar à capacidade do ambiente sem versionar arquivos gigantes.

## 20. N+1 e JPA
Detectar N+1 nos endpoints críticos. Corrigir com abordagem adequada:
```text
projections
fetch join
EntityGraph
DTO query
batch fetching
```
Não transformar tudo em `EAGER`. Revisar `EAGER` existentes com cuidado.

## 21. Índices e banco alvo
Revisar índices conforme queries reais, especialmente combinações envolvendo:
```text
empresa_id
setor_id
data/hora
funcionario_id
peca_id
maquina_id
status
```
Não indexar tudo.

Analisar `EXPLAIN` no banco alvo quando disponível. H2 não deve ser única evidência se produção utilizar PostgreSQL.

## 22. Testcontainers e Flyway
Avaliar Testcontainers para validar banco alvo em queries nativas, constraints e migrations, mantendo a suíte rápida separada.

Executar migrations do zero em banco limpo. Nunca editar migration já aplicada.

## 23. Dashboard
Medir e otimizar TASK 14. Evitar query por setor/funcionário/peça quando o banco puder agregar. Medir tamanho do JSON. Endpoint principal continua resumido; drill-down carrega somente período/contexto solicitado.

## 24. Qualidade e auditoria
Revisar queries pesadas da TASK 13 (duplicidades, cobertura, lacunas). Preferir agregação no banco e paginação.

Auditoria deve ser paginada e indexada conforme filtros reais de empresa/data/usuário/recurso/ação.

## 25. Importação
Revisar consumo de memória. Usar streaming quando adequado/possível ou impor limite seguro. Avaliar persistência em lotes com `flush/clear` controlado sem quebrar atomicidade.

Evitar consulta individual desnecessária por linha para validações de duplicidade.

## 26. Relatórios
Não carregar milhões de registros em memória. Usar período, paginação/streaming. Para XLSX grande com Apache POI, avaliar `SXSSFWorkbook` quando aplicável.

## 27. IA e RAG — performance
Backend agrega antes de enviar ao modelo. Revisar:
```text
max tokens
top K
máximo de tools
timeout
chunk size
metadata filters
```
Nunca enviar dataset transacional gigante ao LLM.

## 28. Concorrência
Testar cenários reais como importações simultâneas e alterações concorrentes de configurações/vigências/metas.

Avaliar `@Version` somente onde houver risco real de lost update. Não aplicar em tudo.

## 29. Transações
Evitar transação de banco aberta durante chamada de IA ou integração externa. Revisar limites transacionais e usar `readOnly=true` em consultas quando adequado, sem tratá-lo como segurança.

## 30. Pool e cache
Usar métricas da TASK 16 para revisar HikariCP. Não aumentar pool para esconder query lenta.

Cache somente se medição justificar. Se houver:
```text
chave inclui empresa
escopo/permissão quando necessário
TTL
invalidação documentada
```
Nunca compartilhar cache privado/financeiro entre contextos.

## 31. Teste de carga
Escolher UMA ferramenta:
```text
k6
Gatling
JMeter
```
Cenários mínimos:
```text
login
Dashboard
drill-down setor
drill-down funcionário
consulta produção
```
IA externa deve ser mockada no teste padrão.

Definir objetivos técnicos com base no baseline, sem inventar SLA empresarial.

Durante carga observar CPU, memória, GC, pool DB, latência, 5xx e queries.

## 32. Testes de segurança
Automatizar ao menos:
1. endpoint sem token;
2. token inválido;
3. token expirado;
4. empresa diferente;
5. setor diferente;
6. IDOR;
7. financeiro proibido;
8. auditoria proibida;
9. Actuator;
10. mass assignment;
11. page size abusivo;
12. sort inválido;
13. upload inválido;
14. path traversal;
15. formula injection;
16. prompt injection;
17. RAG cross-company;
18. RAG cross-sector.

## 33. Dependências e OWASP
Executar análise de dependências/CVEs com ferramenta compatível. Não atualizar major versions indiscriminadamente. Documentar vulnerabilidades corrigidas, mitigadas, aceitas temporariamente ou falsos positivos.

Usar OWASP Top 10 como checklist, especialmente:
```text
Broken Access Control
Cryptographic Failures
Injection
Insecure Design
Security Misconfiguration
Vulnerable Components
Authentication Failures
Logging/Monitoring Failures
SSRF
```

## 34. OpenAPI
Definir política para Swagger/OpenAPI em produção: protegido ou desabilitado. Não deixar documentação administrativa aberta sem decisão explícita.

## 35. Documentação obrigatória
Criar:
```text
docs/SECURITY_HARDENING.md
docs/PERFORMANCE_BACKEND.md
```

`SECURITY_HARDENING.md` deve documentar autorização, OWASP, uploads, IA/RAG, Actuator, CORS, headers, rate limits, secrets e pendências.

`PERFORMANCE_BACKEND.md` deve documentar baseline, dataset, endpoints, problemas, otimizações, antes/depois, limitações e como repetir os testes.

## 36. Checklist pré-homologação
Criar checklist:
```text
[ ] endpoints protegidos
[ ] empresa
[ ] setor
[ ] financeiro
[ ] auditoria
[ ] Actuator
[ ] CORS/CSRF
[ ] headers
[ ] uploads
[ ] IA/RAG
[ ] migrations
[ ] índices
[ ] N+1
[ ] paginação
[ ] performance
[ ] dependências
[ ] logs/secrets
```

## 37. Fora do escopo
NÃO implementar:
```text
frontend
redesign de domínio
Kubernetes
microservices
cluster
troca completa de banco
nova funcionalidade industrial
novo modelo de IA
pentest externo profissional
```

## 38. Regressão e comandos
Todos os testes das TASKS 01–16 devem continuar passando.

Windows:
```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```
Executar também validação OpenSpec, análise de dependências, testes de segurança e profile/teste de performance adotado.

## 39. Proibições
É proibido:
- remover autenticação para teste passar;
- wildcard CORS com credenciais em produção;
- concatenar SQL;
- confiar em IDs sem autorização;
- aumentar pool indiscriminadamente;
- adicionar EAGER/índice em tudo;
- cachear entre empresas;
- ignorar CVE crítica aplicável;
- inventar benchmark;
- concluir com build falhando.

## 40. Critérios de aceite
Concluir somente quando:
1. endpoints estiverem inventariados;
2. default deny estiver validado;
3. empresa/setor estiverem isolados;
4. IDOR estiver testado;
5. financeiro/auditoria continuarem protegidos;
6. JWT/secrets estiverem revisados;
7. CORS/CSRF/headers estiverem definidos;
8. DTOs/paginação/sort estiverem protegidos;
9. queries não concatenarem entrada;
10. uploads/XLSX estiverem protegidos;
11. formula injection estiver tratada quando aplicável;
12. IA/RAG continuar isolada/read-only;
13. Actuator estiver protegido;
14. rate limits críticos existirem;
15. baseline existir;
16. N+1 críticos forem removidos;
17. índices forem revisados;
18. migrations funcionarem do zero;
19. banco alvo for validado quando aplicável;
20. Dashboard/importação/relatórios forem medidos;
21. transações longas forem evitadas;
22. concorrência relevante for testada;
23. teste de carga existir;
24. dependências/OWASP forem revisados;
25. `SECURITY_HARDENING.md` existir;
26. `PERFORMANCE_BACKEND.md` existir;
27. regressão passar;
28. `clean test` passar;
29. `build` passar;
30. OpenSpec refletir a implementação.

## 41. Entrega esperada do Codex
Ao concluir, apresentar:
- OpenSpec: change/proposal/design/tasks/specs/validação;
- inventário de segurança e endpoints;
- autenticação/autorização/IDOR/CORS/CSRF/headers/uploads/Actuator/IA-RAG;
- baseline, dataset, N+1, índices, queries e otimizações antes/depois;
- migrations e banco alvo;
- ferramenta/cenário/resultado do teste de carga;
- localização dos dois documentos obrigatórios;
- testes de segurança/performance/concorrência/regressão;
- resultados de `clean test` e `build`;
- somente pendências reais.

**Não iniciar a TASK 18.**

## 42. Próxima etapa prevista
Após aprovação:
```text
TASK 18 — Homologação, Testes E2E, Documentação e Fechamento do Backend V1
```

A última task validará o fluxo completo:
```text
Login
→ Empresa/permissões
→ Cadastros industriais
→ Importação Excel
→ Produção/paradas
→ Analytics
→ Metas/desempenho
→ Valor industrial
→ Qualidade
→ Dashboard
→ Relatórios
→ Auditoria
→ Assistente IA/RAG
```

E entregará:
```text
documentação final
OpenAPI final
guia de execução/configuração/deploy
dados de homologação
testes E2E
checklist de aceite
Backend V1
```

Somente depois da aprovação da TASK 18 será iniciada a fase **FRONTEND PREMIUM DARK**.
