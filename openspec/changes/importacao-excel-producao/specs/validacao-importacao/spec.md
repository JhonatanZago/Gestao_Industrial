# Spec Delta

## Purpose

Validar integralmente as linhas normalizadas contra cadastros, regras temporais e conflitos antes de persistir qualquer registro de produção.

## ADDED Requirements

### Requirement: Import has explicit company and sector context
Toda validação MUST receber Empresa e Setor pelo contexto do endpoint; setor informado no arquivo, quando existir, MUST corresponder ao contexto, e códigos MUST ser resolvidos sem criação automática de funcionários, peças, máquinas ou turnos.

#### Scenario: Resolve valid references
- **WHEN** funcionário, peça, máquina opcional e turno opcional existem na Empresa e estão ativos
- **THEN** a linha é considerada válida e pronta para conversão em produção

#### Scenario: Reject unknown or inconsistent references
- **WHEN** um código não existe, está inativo, pertence a outra Empresa ou o setor da planilha diverge do contexto
- **THEN** a linha recebe código técnico específico e a importação não pode ser concluída

### Requirement: Validate all rows before persistence
O sistema MUST validar todas as linhas, duplicidades exatas, conflitos entre linhas do próprio arquivo e conflitos com produções já persistidas antes de gravar qualquer produção.

#### Scenario: Detect internal employee overlap
- **WHEN** duas linhas do arquivo sobrepõem o período do mesmo funcionário
- **THEN** ambas são reportadas com `SOBREPOSICAO` e nenhuma produção é persistida

#### Scenario: Detect database overlap
- **WHEN** uma linha sobrepõe uma produção existente conforme a regra de produção
- **THEN** a importação é rejeitada com `SOBREPOSICAO` sem duplicar a regra temporal do domínio

#### Scenario: Reject exact duplicate lines
- **WHEN** duas linhas possuem funcionário, peça, quantidade e período idênticos
- **THEN** a importação é rejeitada com `LINHA_DUPLICADA`

### Requirement: Import is atomic all-or-nothing
Uma importação de produção MUST persistir todas as linhas válidas somente quando não houver erro; qualquer falha MUST deixar zero novas produções e registrar o resultado da tentativa como rejeitado ou falho.

#### Scenario: Reject one invalid row in valid workbook
- **WHEN** 99 linhas são válidas e uma linha contém funcionário inexistente
- **THEN** nenhuma das 100 linhas é persistida e o relatório identifica a linha inválida

#### Scenario: Persist complete valid workbook
- **WHEN** todas as linhas passam pelas validações
- **THEN** o sistema grava todas as produções na transação final e marca a importação como concluída

### Requirement: Return stable line error codes
Cada erro de linha MUST informar número da linha, campo quando aplicável, código estável e mensagem segura, sem stack trace; códigos MUST incluir pelo menos erros de cabeçalho, referência, quantidade, data/horário, período, sobreposição, duplicidade e arquivo duplicado.

#### Scenario: Report multiple line errors
- **WHEN** linhas diferentes possuem quantidade inválida e peça inexistente
- **THEN** o relatório retorna ambos os erros com suas linhas e códigos independentes
