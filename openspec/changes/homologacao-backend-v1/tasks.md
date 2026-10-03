# Tasks

## 1. Baseline e inventário

- [x] 1.1 Executar `./gradlew.bat clean test` e `./gradlew.bat build`, registrar versão/commit/ambiente e confirmar que o baseline não depende de internet ou API paga.
- [x] 1.2 Inventariar resources `/api/v1`, perfis, permissões, profiles, propriedades, migrations, integrações fake/disabled, formatos de relatório e documentação; entregar uma matriz inicial de cobertura e verificar que cada item aponta para código ou teste existente.
- [x] 1.3 Inventariar TODO/FIXME/HACK e executar o scanner de dependências/CVEs disponível; registrar ferramenta, versão, achados e limitações em um artefato versionado.

## 2. Massa e fixtures de homologação

- [x] 2.1 Criar dataset determinístico com Metalúrgica Homologação, Empresa B, setores Pintura/Solda/KTL/Laser, funcionários 00147--00149, peças PEC-A--PEC-C e máquinas PINT-01/SOLDA-01/LASER-01; verificar repetibilidade em ambiente limpo.
- [x] 2.2 Preparar os registros dos períodos oficial e anterior, incluindo produção, paradas, meta e valor industrial; verificar por consulta direta os totais esperados antes de testar consumidores.
- [x] 2.3 Criar fixture XLSX válida compatível com o importador real e fixtures negativas para arquivo vazio, formato inválido, coluna ausente, funcionário/peça/quantidade/horário inválidos e arquivo acima do limite; verificar que os arquivos não contêm dados reais nem secrets.
- [x] 2.4 Documentar comando de geração, chaves de lookup, limpeza e volumes da massa em `docs/` ou no artefato de homologação; verificar execução sem arquivo gigante versionado.

## 3. Fluxo E2E e cadastros

- [x] 3.1 Implementar o cenário E2E de login, criação/consulta dos cadastros existentes, importação, produção e paradas, propagando IDs entre etapas; verificar status, payloads e persistência com MockMvc ou servidor de teste.
- [x] 3.2 Homologar CRUD, validação, duplicidade e ativação/inativação de setores, funcionários, peças, máquinas, turnos, motivos e demais cadastros realmente implementados; adicionar regressões para cada defeito encontrado e registrar evidências.
- [x] 3.3 Validar a importação válida como fluxo all-or-nothing até analytics, dashboard e relatório; verificar hash, contagens, status, auditoria e ausência de linhas parciais.
- [x] 3.4 Validar todas as fixtures negativas e os limites de upload/linhas/ZIP; verificar códigos de erro seguros, estado final do lote e rollback de qualquer persistência parcial.

## 4. Analytics, metas, qualidade e financeiro

- [x] 4.1 Homologar filtros de empresa, setor, funcionário, peça, máquina, turno e período em produção, paradas e analytics; verificar limites inclusivos, exclusão de inativos e grupos técnicos sem máquina/turno.
- [x] 4.2 Comparar os números oficiais entre resumo, série temporal, comparativos, dashboard, relatórios e desempenho; verificar produção +25%, paradas -25%, atingimento aproximado de 90,91%, parada 10% e valor +21,43%.
- [x] 4.3 Homologar vigência, cobertura e ausência de meta/tempo padrão/valor sem converter ausência em zero; verificar desempenho, quantidade esperada, moeda e proteção financeira com testes positivos e negativos.
- [x] 4.4 Homologar resumo, problemas, duplicidades e detalhe de qualidade de dados com a massa determinística; verificar contagens, severidades, ausência de mutação e coerência com os registros de origem.

## 5. Segurança, auditoria e IA/RAG

- [x] 5.1 Executar matriz de autenticação para login válido/inválido, sem token, token inválido/expirado, perfis, permissões, financeiro e auditoria; verificar 401/403 seguros, sem secrets ou stack trace.
- [x] 5.2 Executar testes IDOR/BOLA em path, query e body entre empresas e setores para todos os módulos autorizados; verificar negação ou not-found seguro e ausência de vazamento em agregações/drill-downs.
- [x] 5.3 Executar operações auditáveis e validar ator, data/hora, ação, recurso, empresa, correlation ID e snapshots saneados; verificar que tokens, hashes, senhas e secrets nunca aparecem.
- [x] 5.4 Testar perguntas determinísticas do assistente com fake/mock, SEM_DADOS, financeiro/auditoria sem permissão, empresa/setor não autorizado, prompt injection e tool injection; verificar grounding nos mesmos números do backend.
- [x] 5.5 Indexar documento sintético de Pintura quando RAG estiver disponível e testar fonte, empresa/setor, recuperação correta e bloqueio cross-company/cross-sector; registrar como não aplicável somente quando a capability não estiver implementada.
- [x] 5.6 Reexecutar hardening final de JWT, CORS/CSRF, headers, SQL injection, uploads, ZIP/XLSX, formula injection, path traversal, Actuator, rate limits e configuração; verificar que nenhum defeito crítico/alto impeditivo permanece.

## 6. Observabilidade, performance e banco

- [x] 6.1 Revalidar health, liveness, readiness, metrics, correlation ID, logs, graceful shutdown, limites, timeouts e degradação de IA; verificar que fluxos não dependentes de IA continuam operacionais quando ela falha.
- [x] 6.2 Reexecutar o perfil de performance da TASK 17 com a mesma massa/ambiente quando possível e comparar baseline, p50/p95/p99, throughput, erros, queries, Hikari, CPU, memória e GC; registrar limitações sem declarar SLA local.
- [x] 6.3 Executar migrations em banco vazio PostgreSQL quando disponível, subir a aplicação e rodar smoke test; verificar FK, UNIQUE, NOT NULL, CHECK, índices e queries nativas sem editar migration histórica.
- [x] 6.4 Revisar contratos críticos OpenAPI/HTTP, paginação, enums, erros e segurança; criar ou atualizar testes de contrato e verificar que todos usam `/api/v1` e refletem resources reais.

## 7. Documentação e operação

- [x] 7.1 Atualizar `README.md` e criar/revisar `CHANGELOG.md`, `docs/ARQUITETURA_BACKEND.md`, `docs/BANCO_DE_DADOS.md`, `docs/EXECUTAR_PROJETO.md`, `docs/DESENVOLVIMENTO.md`, `docs/DEPLOY_BACKEND.md` e `docs/OPERACAO_BACKEND.md`; verificar comandos e Mermaid contra o código real.
- [x] 7.2 Criar/revisar `docs/HOMOLOGACAO_BACKEND_V1.md` com versão, data, commit, ambiente, matriz de cenários, evidências, defeitos, segurança, performance, banco, limitações e decisão GO/NO-GO; verificar que cada linha aponta para teste, comando ou artefato.
- [x] 7.3 Revisar `docs/SECURITY_HARDENING.md`, `docs/PERFORMANCE_BACKEND.md`, `docs/ASSISTENTE_IA_RAG.md` e `docs/IMPORTACAO_EXCEL.md` conforme o comportamento final; verificar que nenhum recurso ausente é documentado como aprovado.
- [x] 7.4 Criar coleção Postman ou equivalente, environment sem secrets e smoke test reproduzível para health, login, consulta básica e dashboard; verificar importação sem credenciais reais e execução contra a API local.
- [x] 7.5 Documentar profiles, variáveis, banco, migrations, backup, rollback, health, logs, métricas e preparação não destrutiva do tag `backend-v1.0.0`; verificar que nenhuma instrução publica ou apaga dados automaticamente.

## 8. Correções e fechamento

- [x] 8.1 Para cada falha encontrada, registrar severidade, reprodução, impacto, correção mínima e teste de regressão; verificar que nenhuma mudança amplia escopo ou mascara teste.
- [x] 8.2 Reexecutar todos os cenários afetados após cada correção relevante e atualizar a matriz de evidências; verificar que correções não quebram isolamento, cálculos, contratos ou documentação.
- [x] 8.3 Executar a regressão final completa, build, validação OpenSpec estrita, inspeção de diff e smoke test; verificar que os comandos obrigatórios passam e que limitações permanecem explícitas.
- [x] 8.4 Emitir decisão final `BACKEND V1: GO` somente com todos os gates obrigatórios aprovados e sem CRÍTICO/ALTO impeditivo; caso contrário emitir `BACKEND V1: NO-GO` listando exatamente os bloqueios e pendências.
