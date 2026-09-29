# Spec Delta

## Purpose

Define observabilidade e comportamento de falha dos fluxos críticos de importação, relatórios, Dashboard e IA/RAG para diagnóstico operacional confiável.

## ADDED Requirements

### Requirement: Estado terminal confiável da importação
Após a criação de uma importação, qualquer falha técnica SHALL persistir o estado terminal existente `FALHOU`, horário de término, código/mensagem sanitizados e correlation ID. A importação MUST NOT permanecer indefinidamente em `VALIDANDO` ou `IMPORTANDO` por causa de exceção conhecida pelo processo.

#### Scenario: Falha técnica durante persistência de linhas
- **WHEN** ocorre uma falha técnica depois que a importação entrou em processamento
- **THEN** o lote termina como `FALHOU`, sem dados técnicos sensíveis, e pode ser localizado pelo correlation ID

#### Scenario: Arquivo rejeitado por validação
- **WHEN** o arquivo viola regra de entrada antes da importação das linhas
- **THEN** o lote mantém o estado funcional `REJEITADA`, distinto de falha técnica

### Requirement: Observabilidade sem conteúdo de importação
O sistema SHALL registrar e medir o ciclo da importação usando resultado, duração e contagens, mas MUST NOT usar nome de arquivo, empresa, usuário, conteúdo de célula ou IDs de entidades como tags de métrica.

#### Scenario: Importação com linhas inválidas
- **WHEN** uma importação é rejeitada com linhas inválidas
- **THEN** logs correlacionados podem conter identificadores técnicos permitidos e contagens, enquanto métricas usam somente resultado de baixa cardinalidade

### Requirement: Monitoramento de relatórios e exportações
O sistema SHALL registrar métricas de execução e falha para relatórios JSON, CSV e XLSX e SHALL produzir logs correlacionados sem registrar conteúdo, planilhas ou dados financeiros retornados.

#### Scenario: Geração de relatório falha
- **WHEN** a geração de um relatório lança uma falha técnica
- **THEN** a falha é medida, o log contém correlation ID e a resposta segue o contrato seguro de erros

### Requirement: Monitoramento da API consolidada do Dashboard
O sistema SHALL medir a latência e o resultado da consulta consolidada usada pelo Dashboard, mantendo filtros e entidades fora das tags e do log de payload.

#### Scenario: Dashboard com alta latência
- **WHEN** a consulta consolidada é concluída
- **THEN** sua duração é observável por rota e resultado para posterior análise de performance

### Requirement: Degradação observável da IA e RAG
Falhas, indisponibilidade, timeout, tool calls e tokens disponíveis da IA/RAG SHALL ser medidos e correlacionados sem registrar prompt, resposta completa ou documento. A degradação MUST permanecer isolada do ERP principal.

#### Scenario: Busca RAG indisponível
- **WHEN** a busca vetorial opcional está indisponível
- **THEN** o assistente retorna erro classificado, a falha é medida e as demais APIs permanecem disponíveis

### Requirement: Runbook operacional
O projeto SHALL fornecer documentação operacional para verificar health, localizar erros por correlation ID, consultar métricas e diagnosticar HTTP 500, banco indisponível, IA indisponível, importação falha, latência elevada e circuito aberto.

#### Scenario: Operador investiga HTTP 500
- **WHEN** um operador recebe um correlation ID de uma resposta 500
- **THEN** o runbook explica como localizar o evento correspondente e quais verificações seguras executar
