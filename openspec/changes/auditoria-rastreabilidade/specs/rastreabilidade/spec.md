# Spec Delta

## Purpose

Esta capability conecta importações e operações compostas aos registros produzidos, permitindo localizar um lote, seus resultados e os dados derivados sem armazenar caminhos locais inseguros ou depender de logs técnicos.

## ADDED Requirements

### Requirement: Lote de importação auditável
O sistema SHALL registrar um evento de lote para validação/importação de Excel com identificador da importação, nome de arquivo sanitizado, empresa, setor ou contexto, total de linhas, sucessos, erros, usuário autenticado, data/hora e status da operação.

#### Scenario: Importação concluída registra resumo do lote
- **WHEN** o usuário importa `producao-pintura.xlsx` com 100 linhas, 98 válidas e 2 inválidas
- **THEN** o sistema registra evento de `IMPORTACAO` com nome sanitizado, contagens 100/98/2, ator e contexto da Pintura

#### Scenario: Caminho local não é persistido
- **WHEN** o arquivo recebido possui nome ou metadados contendo caminho local
- **THEN** o evento armazena somente nome sanitizado e não persiste caminho absoluto, credenciais ou conteúdo indevido

### Requirement: Correlação entre lote e registros
Registros criados por uma importação SHALL possuir origem `IMPORTACAO_EXCEL` e compartilhar o `correlationId` da importação. Deve ser possível localizar o lote e os registros relacionados através da auditoria e das APIs existentes de importação.

#### Scenario: Registros derivados são localizáveis
- **WHEN** 98 registros são criados a partir da importação de id 42
- **THEN** os eventos dos registros usam o mesmo `correlationId` do lote 42 e origem `IMPORTACAO_EXCEL`, permitindo recuperar a relação sem analisar logs

#### Scenario: Validação sem persistência de produção
- **WHEN** uma planilha é apenas validada ou é rejeitada antes da criação dos registros
- **THEN** o sistema registra o evento da importação e seu resultado, mas não cria eventos de produção para linhas que não foram persistidas

### Requirement: Origens e correlação estáveis
O sistema SHALL distinguir pelo menos as origens `MANUAL`, `IMPORTACAO_EXCEL`, `SISTEMA` e `API`, e SHALL preservar o mesmo `correlationId` em todos os eventos de uma operação composta sem exigir infraestrutura distribuída.

#### Scenario: Operação manual e importação são distinguíveis
- **WHEN** uma produção é criada manualmente e outra é criada pela importação
- **THEN** os eventos possuem respectivamente origem `MANUAL` e `IMPORTACAO_EXCEL`

#### Scenario: Correlação não vaza entre operações
- **WHEN** duas importações diferentes são processadas para a mesma empresa
- **THEN** cada lote e seus registros possuem correlation IDs distintos e não compartilham eventos
