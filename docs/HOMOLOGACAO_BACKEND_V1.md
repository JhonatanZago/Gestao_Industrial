# Homologação Backend V1

> Documento de trabalho da change `homologacao-backend-v1`. A decisão final GO/NO-GO só pode ser emitida após a execução dos gates previstos.

## Baseline

- Commit de referência: `bdf2433688dc6f242652c51aaeefc85983af73af`
- Data da execução inicial: 2026-09-29
- Ambiente: Windows, Gradle 9.7.1, Spring Boot 4.1.1, JDK 21.0.12.1; o projeto declara Java 17 via toolchain.
- Banco da suíte rápida: H2 em memória com Flyway.
- Banco de runtime: PostgreSQL 16 via Docker Compose.
- Resultado: `./gradlew.bat clean test` e `./gradlew.bat build` passaram.
- Dependência externa: a suíte passou sem provedor de IA ou API paga; gateways fake/disabled permanecem no código.

## Inventário inicial

### Resources `/api/v1`

| Área | Resources observados | Cobertura a homologar |
| --- | --- | --- |
| Identidade e empresa | `AuthResource`, `EmpresaResource`, `UsuarioResource` | login, token, perfis, empresa e usuários |
| Cadastros | `SetorResource`, `FuncionarioResource`, `PecaResource`, `MaquinaResource`, `TurnoResource`, `MotivoParadaResource` | CRUD, escopo, duplicidade e ativação |
| Produção | `RegistroProducaoResource`, `ParadaProducaoResource` | produção, paradas, duração e filtros |
| Importação | `ImportacaoProducaoResource`, `ImportacaoHistoricoResource` | XLSX, limites, histórico e linhas |
| Analytics | `AnalyticsResource`, `DesempenhoResource`, `FinanceiroIndustrialResource` | agregações, metas, desempenho e valor industrial |
| Qualidade | `QualidadeDadosResource` | resumo, problemas, duplicidades e recurso |
| Relatórios | `RelatorioExecutivoResource`, `RelatorioExportResource`, `RelatoriosOperacionaisResource` | JSON/CSV/XLSX realmente implementados |
| Auditoria | `AuditoriaResource` | consulta e snapshots saneados |
| IA/RAG | `AssistenteResource`, `AssistenteStatusResource`, `RagDocumentResource` | grounding, isolamento e degradação |
| Operação | `OperationalStatusResource` | status operacional autorizado |

Todos os resources usam o prefixo `/api/v1`; health/readiness/liveness e Actuator são tratados separadamente pela configuração operacional.

### Profiles, configuração e banco

- Profiles encontrados: `dev`, `test` e `prod`, além das propriedades comuns.
- Configurações críticas: banco/Flyway, JWT, CORS, bootstrap administrativo, limites de importação/ZIP, relatórios, qualidade, IA, Actuator/Prometheus, Hikari, payload, shutdown e chamadas remotas futuras.
- Migrations Flyway: `V1` a `V22`, incluindo empresa, setores, funcionários, peças, produção, paradas, máquinas/turnos, importação, metas, valores, autorização, auditoria, qualidade, RAG e operações.
- Dependências de IA: gateways fake/disabled e armazenamento vetorial em memória; não há provedor remoto obrigatório para a suíte.

### Evidência pendente do inventário

Os cenários detalhados por endpoint, perfil e permissão serão ligados à matriz final depois da execução E2E. O inventário deve ser atualizado se uma correção alterar o contrato.

## Análise de dependências/CVEs

- O Gradle possui `dependencies`, `dependencyInsight` e `dependencyManagement`, mas não possui a task `dependencyCheckAnalyze`.
- Não foram encontrados `k6`, `trivy`, `osv-scanner` ou `dependency-check` no PATH desta máquina durante o baseline.
- A suíte/build não substituem um scanner dedicado. A análise CVE dedicada fica como pendência operacional até que uma ferramenta aprovada esteja disponível.

## Critério de decisão

Resultados permitidos na matriz: `APROVADO`, `REPROVADO` e `NÃO APLICÁVEL`. A decisão final será preenchida somente após os gates de E2E, segurança, banco, performance, documentação, regressão, build e validação OpenSpec.

## Evidências executadas até o momento

| Funcionalidade | Cenário | Resultado | Evidência |
| --- | --- | --- | --- |
| Baseline | suíte limpa e build | APROVADO | `clean test`, `build` |
| Cadastros | empresa/setor e isolamento | APROVADO | `EmpresaSetorIntegrationTests` |
| Importação | XLSX válido, inválido, hash e rollback | APROVADO | `ImportacaoExcelIntegrationTests`, `ExcelImportReaderTests` |
| Produção/paradas | duração, paradas, lifecycle e sobreposição | APROVADO | `ParadasTemposIntegrationTests` |
| Analytics | filtros, limites, taxas e comparação | APROVADO | `AnalyticsIntegrationTests`, `AnalyticsMetricsTests` |
| Qualidade/relatórios | cobertura, duplicidade, regras e exportação | APROVADO | testes `QualidadeDados*` e `Relatorio*` |
| Segurança | JWT, permissões, IDOR/BOLA, headers e rate limit | APROVADO | `AuthIntegrationTests`, `MultiEmpresaEscopoIntegrationTests`, `SecurityFilterChainIntegrationTests` |
| IA/RAG | escopo, grounding, injection e documento | APROVADO | testes `assistente/*` e `RagDocumentTests` |
| Operação | health, métricas, correlation, payload e resiliência | APROVADO | `OperationalHealthIntegrationTests`, `HttpServerMetricsIntegrationTests`, testes `observability/*` |
| Banco vazio | migrations PostgreSQL 16.15 | APROVADO | instância Docker temporária, migrations aplicadas |
| Performance k6 | p50/p95/p99 e carga final | NÃO APLICÁVEL | `k6` ausente no ambiente; comando em `docs/PERFORMANCE_BACKEND.md` |
| Scanner CVE dedicado | análise externa | NÃO APLICÁVEL | task `dependencyCheckAnalyze` e binários dedicados ausentes |

## Limitações observadas

- A imagem Docker foi reconstruída após a correção do fallback de IA; a aplicação atual subiu em profile `prod`, validou as 22 migrations existentes e retornou readiness HTTP 200.
- O endpoint executivo dedicado de dashboard não existe no inventário atual; `analytics/resumo` é o endpoint disponível e alimenta a métrica de dashboard.
- A suíte rápida usa H2; a execução PostgreSQL é evidência complementar, não substituto de todos os testes.

## Defeitos

Nenhum defeito crítico ou alto foi encontrado nos cenários executados até esta etapa.

| Severidade | Item | Tratamento |
| --- | --- | --- |
| BAIXO | Gerador de fixtures dependia de assemblies ZIP não carregados no Windows PowerShell | Corrigido carregando os assemblies explicitamente e usando escrita binária para o arquivo vazio; execução repetida com hashes iguais |
| BAIXO | Primeira versão do fixture de horário usava intervalo noturno, que é válido no contrato | Corrigido para horário sintaticamente inválido `25:99`/`26:00` |
| ALTO corrigido | Imagem atual não iniciava em `prod` por ausência de beans fallback de IA/RAG | `Disabled*` passou a ser fornecido fora do profile `test`; `ProductionAiFallbackContextTests` passou e o container atual retornou readiness 200 |

## Decisão final

BACKEND V1: NO-GO

O código, testes, build, migrations e readiness passaram, mas a homologação não possui evidência executada para dois gates obrigatórios: o binário k6 não está instalado para medir a carga final, e não há scanner dedicado de CVEs disponível/configurado. Esses itens permanecem como pendências operacionais explícitas; nenhum SLA ou aprovação de dependências foi declarado.
