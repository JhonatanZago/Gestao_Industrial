# Spec Delta

## Purpose

Oferecer auditoria consultável de cada importação de produção, incluindo hash do arquivo, estado do processamento, contagens, erros e rastreabilidade futura entre produção e linha de origem.

## ADDED Requirements

### Requirement: Track import lifecycle and summary
O sistema MUST criar uma auditoria com Empresa, Setor, nome e hash SHA-256 do arquivo, status técnico, contagens de linhas e timestamps de início/fim; os estados MUST representar recebida, validação, validada, importação, concluída, falha ou rejeição.

#### Scenario: Record rejected validation
- **WHEN** a pré-validação encontra erros
- **THEN** o histórico registra a tentativa, contagens, status rejeitada e seus resultados de linha sem produções persistidas

#### Scenario: Record successful import
- **WHEN** todas as linhas são persistidas
- **THEN** o histórico registra status concluída, linhas importadas e timestamps finais

### Requirement: Prevent accidental reimport of identical content
O sistema MUST impedir por padrão a reimportação de um mesmo hash concluído para a mesma Empresa e Setor, independentemente do nome do arquivo; um arquivo corrigido com hash diferente pode ser processado se não conflitar com o banco.

#### Scenario: Reject renamed duplicate file
- **WHEN** o mesmo conteúdo já concluído é enviado com outro nome no mesmo contexto
- **THEN** o sistema rejeita a operação com `ARQUIVO_DUPLICADO`

#### Scenario: Accept corrected file
- **WHEN** o arquivo corrigido possui hash diferente e suas linhas são válidas
- **THEN** o sistema permite nova importação, sujeita às regras normais de conflito

### Requirement: Expose validation, import and history APIs
O sistema MUST expor endpoints multipart para pré-validar e importar produção no contexto `/api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao`, além de listagem paginada e detalhe por identificador; detalhe e erros devem ser pagináveis ou limitados para evitar respostas gigantescas.

#### Scenario: Validate without persistence
- **WHEN** o cliente chama `POST .../validar` com um arquivo
- **THEN** recebe resumo e erros sem criar Registros de Produção

#### Scenario: Import and query history
- **WHEN** o cliente chama `POST .../importacoes/producao` com arquivo válido e depois consulta o histórico
- **THEN** recebe resumo da importação concluída e consegue consultar seu detalhe no escopo da Empresa

### Requirement: Preserve production provenance
Produções criadas por importação MUST poder ser distinguidas da origem manual e relacionadas à Importação e à linha de origem sem criar uma tabela paralela de produção importada.

#### Scenario: Trace imported production
- **WHEN** uma produção é criada pela importação
- **THEN** sua origem e referência de importação/linha ficam disponíveis para auditoria sem alterar a semântica das consultas de produção
