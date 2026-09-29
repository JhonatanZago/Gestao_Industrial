# Spec Delta

## Purpose

Define métricas operacionais estáveis e de baixa cardinalidade para detectar erros, latência e resultados dos fluxos críticos sem expor dados industriais.

## ADDED Requirements

### Requirement: Métricas HTTP do backend
O sistema SHALL medir quantidade, status e duração das requisições HTTP, permitindo distinguir respostas bem-sucedidas, erros 4xx e erros 5xx por método e rota normalizada.

#### Scenario: Endpoint retorna erro interno
- **WHEN** uma requisição termina com HTTP 500
- **THEN** o contador HTTP de 5xx é incrementado e a duração é registrada sem usar path contendo IDs como tag

### Requirement: Métricas de importação
O sistema SHALL registrar início, conclusão, falha técnica, duração e quantidade de linhas processadas das importações, sem usar empresa, usuário, nome de arquivo ou identificador da importação como tag.

#### Scenario: Importação concluída
- **WHEN** uma importação termina com sucesso
- **THEN** os contadores de início e conclusão e a duração são atualizados, juntamente com a quantidade agregável de linhas processadas

#### Scenario: Importação falha tecnicamente
- **WHEN** uma importação termina por falha técnica
- **THEN** o contador de falhas e a duração são atualizados uma única vez

### Requirement: Métricas de relatórios e Dashboard
O sistema SHALL medir total, falhas e duração da geração de relatórios JSON, CSV e XLSX, além da latência da API consolidada usada pelo Dashboard, sem registrar o conteúdo retornado.

#### Scenario: Exportação XLSX concluída
- **WHEN** um relatório XLSX é gerado com sucesso
- **THEN** total e duração são atualizados com formato e resultado como tags de baixa cardinalidade

#### Scenario: Dashboard falha
- **WHEN** a consulta consolidada do Dashboard falha
- **THEN** a falha e sua duração são registradas sem dados de filtros ou entidades como tags

### Requirement: Métricas de IA
O sistema SHALL medir requisições, falhas classificadas, duração, timeouts, chamadas de tools e tokens informados pelo provedor, sem usar pergunta, prompt, resposta, usuário, empresa ou documento como tag.

#### Scenario: Provedor de IA indisponível
- **WHEN** uma requisição de IA falha por indisponibilidade do provedor
- **THEN** a falha classificada e a duração são incrementadas sem registrar a pergunta como métrica

### Requirement: Política de cardinalidade e dados sensíveis
Todas as métricas customizadas MUST usar somente tags enumeradas e de baixa cardinalidade. Secrets, IDs de negócio, nomes livres e payloads MUST NOT aparecer em nomes, tags ou descrições de métricas.

#### Scenario: Medição de fluxo empresarial
- **WHEN** uma operação contém IDs de empresa, usuário, funcionário, peça ou arquivo
- **THEN** esses valores não são incluídos em tags de métrica

### Requirement: Exposição opcional e protegida para Prometheus
Quando a exportação Prometheus estiver habilitada, o sistema SHALL expor o endpoint de scrape somente a clientes autorizados. A aplicação MUST iniciar e operar sem Prometheus ou Grafana.

#### Scenario: Scrape não autenticado
- **WHEN** um cliente não autenticado consulta o endpoint Prometheus
- **THEN** o acesso é negado sem revelar as métricas

