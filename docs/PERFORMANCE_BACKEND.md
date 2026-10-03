# Performance backend

## Revisão de homologação Backend V1

Os testes funcionais de analytics, relatórios, observabilidade, Hikari e limites passaram na regressão. O perfil k6 versionado continua reproduzível, mas não foi executado nesta máquina porque o binário k6 não está instalado; portanto não há SLA ou p95 final declarado.

## Baseline inicial

Data: 29/09/2026. Ambiente: workspace local, Gradle 9.7.1, testes Spring Boot com perfil `test`. Dataset de carga representativo ainda não foi provisionado. A suíte `gradlew.bat --no-daemon test` passou; esse resultado valida regressão de código, não é SLA nem substitui medição de runtime.

| Fluxo | Estado inicial | Evidência pendente |
|---|---|---|
| Dashboard/analytics | código e limites revisados | p50/p95/p99, query count e memória |
| Drill-down setor/funcionário | endpoints paginados | dataset sintético e carga |
| Produção/qualidade/auditoria | cobertura de testes existente | planos PostgreSQL e N+1 |
| Relatórios/exportações | limites de período/top-N/linhas configurados | duração, resposta e pool |
| Importação | multipart 10 MB e 10.000 linhas configurados | dataset, concorrência e tempo |
| IA/RAG | fake/provider policy e rate limit de IA existentes | perfil mockado e tokens |

## Reprodução

```powershell
$env:GRADLE_USER_HOME=(Resolve-Path '.gradle-user-home-2').Path
$env:GRADLE_OPTS='-Djava.io.tmpdir=' + (Resolve-Path '.gradle-tmp-2').Path
.\gradlew.bat --no-daemon test
.\gradlew.bat --no-daemon dependencies --configuration runtimeClasspath
```

O perfil de carga previsto usa k6 e deve registrar login, Dashboard, drill-downs e produção com IA fake. Os resultados devem informar máquina, JVM, banco, tamanho do dataset, configuração do pool, p50/p95/p99, throughput, 4xx/5xx, resposta, CPU, memória, GC, Hikari e queries. Comparações são relativas ao baseline local e não representam capacidade ou disponibilidade de produção.

## Dataset e carga

Gerar os artefatos sob demanda, sem versionar o volume produzido:

```powershell
.\scripts\performance\generate-dataset.ps1 -OutputDirectory .\performance-data
k6 run -e BASE_URL=http://localhost:8080 -e COMPANY_ID=1 -e SECTOR_ID=1 -e EMPLOYEE_ID=1 -e TEST_EMAIL=usuario@example.test -e TEST_PASSWORD=senha scripts/performance/backend-load.js
```

O gerador usa seed fixa, volumes ajustáveis e produz `manifest.json` e `production.jsonl`; o diretório deve ser removido após o benchmark. O script k6 usa somente fluxos protegidos e não chama provedor externo de IA. Neste ambiente `k6` não está instalado, portanto a execução de carga permanece pendência manual.

## Guardrails de otimização

- Medir antes de trocar paginação, agregação, fetch strategy, índice, pool ou cache.
- Não tornar associações globalmente `EAGER`.
- Não manter transação aberta durante chamada externa de IA/integração.
- Qualquer cache futuro deve incluir empresa, setor e permissões no escopo, TTL e invalidação.
- Toda otimização precisa de teste funcional, isolamento, query count e comparação antes/depois.

## Achados atuais

- O Dashboard/analytics ainda agrega registros na aplicação (`AnalyticsService.load`); query plans PostgreSQL, query count e N+1 precisam de dataset e banco alvo antes de uma alteração.
- Não há cache privado novo nesta change; Hikari já possui limites finitos configurados.
- Nenhuma migration ou associação JPA foi alterada por falta de evidência suficiente.

Em 29/09/2026, o PostgreSQL 16.15 local confirmou 20 tabelas e 66 índices. O plano para a consulta tenant-scoped de produção por `empresa_id`, `ativo` e janela de `inicio` usa `idx_producao_empresa_inicio` (`Index Scan`, custo estimado 0.14..8.16). A base local não contém o dataset sintético padrão, portanto N+1, p95 e memória ainda não podem ser comparados com volume representativo.

Sem dataset representativo, não foi demonstrado N+1, saturação de pool ou regressão funcional que autorize trocar as agregações atuais, paginação ou fetch strategies. A decisão nesta execução é conservar o comportamento e deixar a comparação reproduzível para homologação. As chamadas externas de IA/embeddings não mantêm mais transações abertas.

## Dependências e banco alvo

`gradlew.bat dependencies --configuration runtimeClasspath` foi executado com sucesso. O relatório de dependências lista Spring Boot/Security, Hibernate, Apache POI, PostgreSQL, H2 e Micrometer; o wrapper Gradle não possui scanner dedicado de CVEs. A análise de vulnerabilidades deve ser executada no pipeline com a ferramenta aprovada e anexada a este documento.

PostgreSQL/Testcontainers é um perfil separado da suíte rápida. Se Docker ou banco alvo não estiverem disponíveis, registrar a pendência e manter a regressão H2, sem afirmar equivalência completa de planos ou migrations.
