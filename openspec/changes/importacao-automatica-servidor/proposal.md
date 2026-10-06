# Proposal

## Why

O sistema já importa XLSX manualmente, mas ainda não possui uma fundação segura para aceitar futuramente arquivos mantidos em uma pasta compartilhada sem duplicar produções ou criar regras diferentes. Esta mudança organiza a auditoria, a idempotência verificável e um pipeline comum antes de qualquer watcher, polling ou scheduler.

## What Changes

- Auditar e documentar o fluxo manual existente, suas classes, endpoints, validações, transações, segurança, persistência e testes.
- Extrair, somente quando necessário, um pipeline central de importação capaz de receber diferentes fontes sem duplicar regras de negócio.
- Preparar origem explícita de importação (`MANUAL`, `FILE_WATCHER`, `RECONCILIACAO`) e reaproveitar o histórico existente quando ele for suficiente.
- Confirmar e testar o hash SHA-256 como identificação de versão do arquivo, separando-o da idempotência dos registros de produção.
- Externalizar a configuração futura da importação automática, desativada por padrão e sem caminhos ou credenciais reais no código.
- Criar testes de regressão e de idempotência para preservar o importador manual e documentar qualquer limitação do modelo atual.
- Não implementar nesta etapa WatchService definitivo, polling, scheduler das 18:00, retry, fila concorrente, frontend novo ou conexão com o servidor real.

## File Watcher phase

Esta fase implementa monitoramento WatchService, debounce, estabilização, processamento automático pelo pipeline existente e isolamento de falhas. O Scheduler das 18h, polling completo, retry avançado, frontend e conexão com servidor real permanecem fora do escopo.

## Reconciliation phase

Esta fase adiciona reconciliação interna diária às 18:00, em `America/Sao_Paulo`, como segunda camada de segurança para eventos perdidos do watcher. A reconciliação apenas descobre candidatos, reutiliza estabilidade, hash, histórico e o pipeline existente, e permanece desativada por padrão junto com a automação global.

## Capabilities

### New Capabilities

- `importacao-automatica-servidor`: Fundação observável para múltiplas fontes de XLSX, pipeline central, versionamento por hash, rastreabilidade de origem e preparação segura da importação automática.

### Modified Capabilities

Nenhuma capability principal versionada foi encontrada em `openspec/specs/`; o contrato desta mudança será criado como capability nova.

## Impact

- Classes existentes de importação: `ImportacaoProducaoService`, `ImportacaoLifecycleService`, `ImportacaoValidator`, `ExcelImportReader` e `ImportacaoProducaoResource`.
- Modelos, repositories e migrations de `ImportacaoProducao`, `ResultadoLinhaImportacao`, `RegistroProducao`, `OrigemProducao` e `StatusImportacao`.
- Configuração Spring externalizada em `application*.properties`, sem segredo ou caminho real.
- Testes `ExcelImportReaderTests`, `ImportacaoExcelIntegrationTests`, `ImportacaoFailureIntegrationTests` e a suíte completa Gradle.
- Nenhuma alteração de comportamento deve retirar ou substituir os endpoints manuais existentes.
## Shared server integration phase

Esta fase prepara a execucao contra pasta compartilhada sem conhecer o endereco real do servidor. As fontes sao configuradas externamente por empresa, setor e caminho, com suporte a diretorio ou arquivo especifico. `enabled=false`, `dry-run=false` e `initial-mode=IGNORE_EXISTING` sao os padroes seguros.

O acesso e somente leitura. Validacao e diagnostico sao isolados por fonte; o WatchService continua sendo baixa latencia e a reconciliacao das 18h e a garantia complementar para filesystem de rede. Nenhum IP, UNC, usuario, senha ou arquivo real e versionado, e o frontend permanece fora do escopo.
## API de status para consumo administrativo

Foi adicionado um endpoint somente leitura escopado por empresa para expor o estado real da automacao, sem misturar o contrato de banco/IA/RAG de `/api/v1/operacao/status` e sem criar historico paralelo. O historico continua sendo servido pelo endpoint existente de importacoes de producao.
