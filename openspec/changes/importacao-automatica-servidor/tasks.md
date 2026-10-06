# Tasks

## 1. Auditoria do importador existente

- [x] 1.1 Mapear frontend/endpoint → `ImportacaoProducaoResource` → `ImportacaoProducaoService` → `ExcelImportReader` → `ImportacaoValidator` → `ImportacaoLifecycleService` → banco, listando arquivos e métodos reais; verificar a matriz no relatório da mudança.
- [x] 1.2 Confirmar no código onde o arquivo é recebido, Apache POI é executado, linhas são normalizadas, validação ocorre, persistência ocorre, empresa/setor são resolvidos, transações são abertas e autorização é aplicada; verificar cada ponto com classe/método ou teste citado.
- [x] 1.3 Inventariar DTOs, entidades, repositories, migrations, status, auditoria, tratamento de duplicidade/reimportação e testes existentes; verificar que o inventário não introduz componentes não encontrados.
- [x] 1.4 Executar a suíte existente antes das alterações e registrar a quantidade de testes, falhas e comando usado no relatório final; verificar o baseline reproduzível com `gradlew.bat test`.

## 2. Idempotência e versão de arquivo

- [x] 2.1 Criar teste que importa o arquivo A com sucesso e reenvia A, verificando o comportamento de duplicidade e ausência de nova produção; verificar contagem de `RegistroProducao` e histórico.
- [x] 2.2 Criar teste que altera A adicionando uma linha e submete a nova versão, verificando o resultado contra a regra de negócio efetivamente encontrada; verificar que nenhuma chave natural é inventada.
- [x] 2.3 Se o modelo não permitir deduplicação segura por registro, documentar `LIMITATION` no artefato/relatório e interromper qualquer implementação dessa parte; verificar que a decisão está explícita antes de prosseguir.
- [x] 2.4 Extrair ou consolidar o cálculo SHA-256 para uma unidade testável sem alterar o contrato manual; verificar que conteúdo igual gera hash igual e conteúdo diferente gera hash diferente.

## 2A. Idempotência incremental entre versões

- [x] 2A.1 Classificar campos do Excel, dados persistidos, metadados e campos gerados; confirmar que a combinação empresa/setor/funcionário/peça/quantidade/início/fim é segura no domínio atual.
- [x] 2A.2 Implementar fingerprint SHA-256 canônico, separado do hash do arquivo, com fallback compatível para registros legados.
- [x] 2A.3 Deduplicar registros já processados, preservar conflito de sobreposição para alterações, registrar linhas ignoradas e proteger novos fingerprints com índice único.
- [x] 2A.4 Cobrir V1, reenvio, V1→V2, reordenação/inserção, alteração de conteúdo e duplicidade no mesmo arquivo.

## 3. Pipeline central e origem

- [x] 3.1 Comparar o fluxo manual atual com o contrato de pipeline central e identificar o menor seam necessário para receber arquivo + contexto + origem; verificar a decisão no design e não criar serviços paralelos.
- [x] 3.2 Refatorar somente o acoplamento necessário para que a entrada manual utilize o pipeline central preservando validação, persistência, resposta e transação; verificar os testes manuais existentes.
- [x] 3.3 Preparar origem compatível para `MANUAL`, `FILE_WATCHER` e `RECONCILIACAO`, preservando valores legados ou documentando migration; verificar persistência e leitura do valor manual.
- [x] 3.4 Reutilizar `ImportacaoProducao` como histórico quando suficiente e só adicionar campos/migration para lacunas comprovadas pela auditoria; verificar schema Flyway em H2/PostgreSQL e ausência de entidade duplicada.

## 4. Configuração e segurança da fundação

- [x] 4.1 Adicionar configuração externalizada da automação com `enabled=false` por padrão e estrutura futura de setor/caminho sem caminho real; verificar que a aplicação inicia sem acessar pasta alguma.
- [x] 4.2 Verificar que nenhuma credencial, caminho real, conteúdo de planilha ou segredo é hardcoded, retornado ao frontend ou escrito em log; verificar com busca no repositório e testes de sanitização/log.

## 4A. File Watcher e estabilização

- [x] 4A.1 Implementar `FileChangeDetector` e `WatchServiceFileChangeDetector` para CREATE/MODIFY, com filtros de XLSX/temporários e shutdown limpo.
- [x] 4A.2 Implementar debounce por caminho e serialização por arquivo, preservando eventos durante processamento e permitindo caminhos independentes.
- [x] 4A.3 Implementar `FileStabilityService` com tamanho, lastModified, leituras consecutivas, teste de acesso e timeout.
- [x] 4A.4 Encaminhar arquivos estáveis ao pipeline existente com origem `FILE_WATCHER`, histórico e isolamento de falhas.
- [x] 4A.5 Cobrir unitários e E2E em diretório temporário, sem caminho real e sem Scheduler.

## 4B. Reconciliação diária

- [x] 4B.1 Expandir configuração com reconciliação habilitável, cron externalizado e timezone `America/Sao_Paulo`, mantendo automação global desativada por padrão.
- [x] 4B.2 Implementar `ReconciliationService.reconciliar()` como scan de candidatos, sem regras próprias de importação e sem endpoint público.
- [x] 4B.3 Reutilizar filtro de arquivo, `FileStabilityService`, SHA-256, histórico, coordenação por caminho e pipeline com origem `RECONCILIACAO`.
- [x] 4B.4 Isolar erros por arquivo/diretório e produzir resumo de execução sem criar tabela desnecessária.
- [x] 4B.5 Adicionar `@Scheduled` apenas como delegação ao serviço, validando cron e timezone sem esperar o relógio real.

## 5. Testes de regressão e aceite da fase

- [x] 5.1 Garantir testes para importação manual, arquivo válido/inválido, zeros à esquerda, quantidade fracionária, horários inválidos e fórmulas; verificar cada caso com teste unitário ou integração existente.
- [x] 5.2 Garantir testes para duplicidade, reimportação, pipeline central, origem `MANUAL` e hash determinístico; verificar contagens, status, origem e efeitos persistidos.
- [x] 5.3 Executar todos os testes existentes e novos testes, registrar testes antes/depois e corrigir qualquer regressão; verificar comando e quantidade no relatório final.
- [x] 5.4 Executar `gradlew.bat build` e `openspec validate "importacao-automatica-servidor" --type change --strict`; verificar ambos com sucesso antes de encerrar a fase.
- [x] 5.5 Apresentar auditoria, status de idempotência (`PASS`, `FAIL` ou `LIMITATION`), pipeline, histórico, hash, importação manual, testes, build, OpenSpec, arquivos alterados e riscos; verificar que nenhum Watcher/scheduler foi iniciado.
## 4C. Integracao com pasta compartilhada

- [x] 4C.1 Externalizar configuracao de fonte por empresa/setor/path, suportar diretorio e arquivo especifico e manter defaults seguros.
- [x] 4C.2 Centralizar politica de arquivos elegiveis e observar CREATE/MODIFY/DELETE sem alterar arquivos.
- [x] 4C.3 Adicionar validador e diagnostico interno por fonte, isolando indisponibilidade de um setor.
- [x] 4C.4 Adicionar dry-run como barreira antes da persistencia e documentar bootstrap `IGNORE_EXISTING`/`PROCESS_EXISTING`.
- [x] 4C.5 Persistir baseline tecnica de bootstrap para sobreviver a restart sem criar historico falso.
- [x] 4C.6 Documentar operacao segura da pasta compartilhada, sem valores reais, credenciais ou frontend.
## 4D. API de status da importacao automatica

- [x] 4D.1 Auditar escopo de empresa, permissao `OPERACAO_MONITORAR`, watcher, diagnostico e historico existente.
- [x] 4D.2 Criar DTO seguro e servico de status com estado real de enabled/running/fontes/reconciliacao.
- [x] 4D.3 Expor apenas GET escopado por empresa; nao criar comandos de start/stop/reprocessamento.
- [x] 4D.4 Derivar ultima importacao do historico existente e documentar ultima verificacao em memoria.
- [x] 4D.5 Cobrir serializacao sem dados sensiveis e executar regressao backend.
