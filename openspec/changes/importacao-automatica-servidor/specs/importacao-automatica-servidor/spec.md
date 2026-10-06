# Spec Delta

## Purpose

Define a fundação segura e rastreável para que fontes manuais e futuras fontes automáticas de planilhas XLSX usem o mesmo pipeline sem substituir o importador manual existente.

## ADDED Requirements

### Requirement: Reconciliação diária deve descobrir candidatos e reutilizar o pipeline

O sistema SHALL disponibilizar `ReconciliationService.reconciliar()` e um scheduler Spring externalizado para o cron padrão `0 0 18 * * *` em `America/Sao_Paulo`. O scheduler SHALL apenas descobrir arquivos elegíveis e encaminhá-los ao mesmo pipeline usado pelo watcher, com origem `RECONCILIACAO`; não SHALL criar regras próprias de importação.

#### Scenario: Reconciliação desabilitada
- **WHEN** a automação global ou a reconciliação está desabilitada
- **THEN** nenhum scan ou processamento automático é iniciado

#### Scenario: Evento perdido pelo watcher
- **WHEN** o histórico contém V1 e o diretório contém V2 com hash diferente
- **THEN** a reconciliação estabiliza V2, verifica o hash, importa somente o pendente e registra origem `RECONCILIACAO`

#### Scenario: Versão já concluída
- **WHEN** o hash do arquivo já possui importação concluída no escopo empresa/setor
- **THEN** o arquivo é ignorado sem nova importação

### Requirement: Reconciliação deve isolar arquivos e compartilhar coordenação

Cada arquivo SHALL ser processado com o mesmo filtro e `FileStabilityService` do watcher. Falha, diretório inacessível ou instabilidade de um candidato SHALL ser registrada sem interromper os demais. Watcher e reconciliação SHALL impedir processamento concorrente do mesmo caminho, preservar eventos posteriores e permitir caminhos independentes.

#### Scenario: Falha isolada
- **WHEN** Pintura falha durante o scan e KTL/Solda são válidos
- **THEN** KTL e Solda continuam, Pintura é contabilizada como erro e a reconciliação termina com resumo

### Requirement: Monitoramento automático deve reutilizar o pipeline

O sistema SHALL detectar `ENTRY_CREATE` e `ENTRY_MODIFY` por uma abstração `FileChangeDetector`, com implementação inicial baseada em `WatchService`, e SHALL encaminhar o caminho ao mesmo `ImportacaoProducaoService` usado manualmente. Arquivos não-XLSX, ocultos e temporários SHALL ser ignorados, e o callback não SHALL importar diretamente.

#### Scenario: Watcher desativado
- **WHEN** `industrial.importacao.automatica.enabled=false` ou não configurado
- **THEN** nenhum diretório é aberto e nenhum watcher inicia

#### Scenario: XLSX válido detectado
- **WHEN** um XLSX é criado ou modificado em diretório configurado
- **THEN** o evento passa por debounce, estabilidade, SHA-256, idempotência e pipeline com origem `FILE_WATCHER`

### Requirement: Estabilização e isolamento do watcher

O sistema SHALL aguardar debounce por caminho e considerar o arquivo estável somente após verificações consecutivas iguais de tamanho e `lastModified`, tentativa de leitura e timeout configurável. Timeout ou falha de um arquivo SHALL não interromper outros setores. O shutdown SHALL fechar WatchService, cancelar pendências e encerrar executors.

#### Scenario: Arquivo instável
- **WHEN** tamanho ou `lastModified` continuam mudando, ou o arquivo permanece inacessível até o timeout
- **THEN** nenhuma importação é iniciada e o watcher continua ativo para os demais caminhos

#### Scenario: Eventos concorrentes
- **WHEN** o mesmo caminho recebe vários MODIFY durante debounce ou processamento
- **THEN** os eventos são consolidados/serializados por caminho sem bloquear arquivos de setores diferentes

### Requirement: O fluxo manual existente deve permanecer compatível

O sistema SHALL manter os endpoints manuais atuais, a autenticação/autorização de empresa e setor, as validações existentes, o processamento all-or-nothing e os contratos de resposta enquanto a fundação for introduzida.

#### Scenario: Importação manual válida
- **WHEN** um usuário autorizado envia um XLSX válido pelo endpoint manual existente
- **THEN** o sistema importa a produção pelo pipeline vigente e registra o resultado sem exigir watcher ou scheduler

#### Scenario: Importação manual inválida
- **WHEN** um usuário envia arquivo inválido, fórmula, quantidade fracionária, código inexistente ou horário inválido
- **THEN** o sistema rejeita o arquivo conforme as regras atuais e não persiste produção parcial

### Requirement: Fontes devem compartilhar o pipeline de negócio

O sistema SHALL possuir um contrato central de processamento que receba arquivo e contexto, execute leitura, validação, regras, persistência e resultado, permitindo que manual, `FILE_WATCHER` e `RECONCILIACAO` sejam fontes do mesmo pipeline.

#### Scenario: Fonte manual usa pipeline central
- **WHEN** uma importação manual é processada
- **THEN** ela atravessa o mesmo serviço de negócio destinado às fontes futuras e registra origem manual compatível

#### Scenario: Fontes automáticas ainda não ativadas
- **WHEN** a aplicação é iniciada nesta fase
- **THEN** nenhum watcher, polling, scheduler ou fila concorrente inicia processamento automaticamente

### Requirement: A origem da importação deve ser rastreável

O sistema SHALL representar, de forma compatível com dados existentes, as origens `MANUAL`, `FILE_WATCHER` e `RECONCILIACAO` para auditoria futura.

#### Scenario: Origem manual registrada
- **WHEN** uma importação manual conclui com sucesso
- **THEN** o histórico/produção registra a origem manual sem remover ou reinterpretar registros legados

### Requirement: A versão do arquivo deve ser identificável por hash determinístico

O sistema SHALL calcular SHA-256 de modo determinístico para o conteúdo do arquivo e SHALL tratar o hash como identificação da versão do arquivo, não como substituto da identidade de seus registros.

#### Scenario: Mesmo conteúdo
- **WHEN** o mesmo conteúdo binário é submetido duas vezes
- **THEN** o cálculo produz o mesmo hash

#### Scenario: Conteúdo diferente
- **WHEN** qualquer conteúdo binário do arquivo é alterado
- **THEN** o cálculo produz um hash diferente, salvo colisão criptográfica não considerada no domínio

### Requirement: Duplicidade e reimportação não podem ser ampliadas sem prova

O sistema SHALL impedir reprocessamento desnecessário de uma versão já concluída conforme a regra existente e SHALL testar uma nova versão que preserve linhas antigas e acrescente uma linha. O sistema MUST NOT inventar uma chave de registro para declarar idempotência por linha; quando o domínio não fornecer informação suficiente, a limitação SHALL ser documentada e a operação SHALL ser interrompida para essa decisão.

#### Scenario: Arquivo concluído reenviado
- **WHEN** o mesmo arquivo, empresa e setor já concluídos são enviados novamente
- **THEN** o comportamento de duplicidade existente é preservado e nenhuma nova produção é criada

#### Scenario: Nova versão com linha adicional
- **WHEN** uma nova versão contém as linhas antigas e uma linha adicional
- **THEN** os testes verificam explicitamente se o modelo atual identifica somente o pendente; se não houver informação segura, o resultado é reportado como `LIMITATION` sem deduplicação inventada

### Requirement: Versões incrementais devem deduplicar por registro

O sistema SHALL calcular uma assinatura SHA-256 por registro usando empresa, setor, funcionário, peça, quantidade, início e fim em representação canônica. O hash do arquivo SHALL permanecer separado dessa assinatura. Quando uma nova versão contiver registros já processados e registros novos, os já processados SHALL ser ignorados individualmente e os novos SHALL ser importados, sem usar número da linha, ID ou timestamp como identidade.

#### Scenario: V1 seguida de V2 incremental
- **WHEN** V1 contém A e B e V2 contém A, B e C
- **THEN** V2 conclui com dois registros ignorados, um importado e o banco contém A, B e C

#### Scenario: Conteúdo alterado
- **WHEN** uma linha existente muda quantidade ou outro campo da assinatura
- **THEN** a alteração é tratada como conflito de negócio conforme a regra de sobreposição, sem atualização silenciosa

#### Scenario: Proteção concorrente
- **WHEN** duas importações tentam persistir o mesmo fingerprint
- **THEN** o índice único do banco impede a segunda duplicação

### Requirement: Configuração automática deve ser externalizada e desativada

O sistema SHALL expor configuração futura para habilitar importação automática e mapear setor/caminho sem inserir caminho real ou credencial no código, no Git, no frontend ou nos logs, e o valor padrão SHALL ser desativado.

#### Scenario: Inicialização padrão
- **WHEN** a aplicação inicia sem configuração explícita de automação
- **THEN** a importação automática permanece desativada e nenhuma pasta é acessada

#### Scenario: Segredo fora da aplicação
- **WHEN** a implantação fornece credenciais ou caminhos por configuração segura do ambiente
- **THEN** esses valores não aparecem em código versionado, resposta HTTP ou log operacional

### Requirement: A fundação deve ter cobertura de regressão

O sistema SHALL manter testes para arquivo válido, inválido, zeros à esquerda, quantidade fracionária, horário inválido, fórmula, duplicidade, reimportação, pipeline central, origem manual e hash, e a suíte existente SHALL continuar executável.

#### Scenario: Regressão completa
- **WHEN** a suíte Gradle e o build são executados
- **THEN** os testes existentes e os novos testes da fundação passam sem regressão
### Requirement: Integracao com compartilhamento deve ser externalizada e somente leitura
O sistema SHALL configurar fontes por empresa, setor, caminho e tipo `DIRECTORY`/`FILE` sem hardcode de servidor ou credencial. A configuracao SHALL permanecer desabilitada por padrao e o backend SHALL usar apenas operacoes de leitura.

#### Scenario: Configuracao ausente
- **WHEN** `IMPORT_AUTO_ENABLED` nao e fornecida
- **THEN** a aplicacao inicia sem acessar filesystem externo

#### Scenario: Fonte por setor
- **WHEN** duas entradas configuradas apontam para setores distintos
- **THEN** cada fonte e validada e monitorada independentemente, sem novo watcher Java por setor

### Requirement: Fontes devem ser validadas e diagnosticadas
O sistema SHALL validar empresa/setor, path, acessibilidade, permissao de leitura e tipo, sem alterar arquivos, e SHALL disponibilizar diagnostico interno por fonte.

#### Scenario: Fonte inacessivel
- **WHEN** um path configurado nao existe ou nao e legivel
- **THEN** a fonte e marcada indisponivel e as demais continuam operacionais

### Requirement: Homologacao e bootstrap devem ser seguros
`dry-run` SHALL interromper antes da persistencia; o modo inicial padrao SHALL ser `IGNORE_EXISTING` ou `DRY_RUN`, nunca `PROCESS_EXISTING` implicito. Baseline tecnica nao SHALL ser registrada como importacao concluida.

#### Scenario: Dry-run
- **WHEN** uma planilha estavel e encontrada com dry-run ativo
- **THEN** filtro, estabilidade e hash sao executados, mas nenhuma producao/historico de sucesso e criado

#### Scenario: Primeiro apontamento
- **WHEN** existem arquivos antigos no primeiro acesso
- **THEN** eles nao sao importados silenciosamente

### Requirement: Substituicao, remocao e indisponibilidade nao podem apagar dados
O detector SHALL aceitar CREATE/MODIFY/DELETE, e remocao de XLSX SHALL somente gerar diagnostico. Perda de acesso SHALL isolar a fonte e permitir recuperacao via reconciliacao.

#### Scenario: Replace do Excel
- **WHEN** um temporario e renomeado para substituir o XLSX
- **THEN** CREATE/MODIFY do arquivo elegivel pode gerar novo candidato apos estabilidade

#### Scenario: Arquivo removido
- **WHEN** o XLSX monitorado e removido
- **THEN** nenhuma producao existente e apagada
### Requirement: Status automatico deve ser consultavel com escopo e seguranca
O sistema SHALL expor `GET /api/v1/empresas/{empresaId}/importacoes/automaticas/status` como consulta read-only protegida por `OPERACAO_MONITORAR`, refletindo enabled, watcher em execucao, fontes acessiveis, ultima verificacao em memoria, ultima importacao do historico e configuracao real de reconciliacao. O DTO MUST NOT expor caminhos, credenciais ou enderecos internos.

#### Scenario: Automacao desativada
- **WHEN** a feature flag esta desativada
- **THEN** a resposta informa `DESATIVADO` e nao afirma que o watcher esta rodando

#### Scenario: Fonte indisponivel
- **WHEN** a automacao esta habilitada mas uma fonte configurada nao esta acessivel
- **THEN** a resposta informa `ATENCAO` e a fonte `INDISPONIVEL`

#### Scenario: Usuario sem permissao
- **WHEN** uma requisicao nao possui `OPERACAO_MONITORAR` ou tenta outra empresa
- **THEN** a API aplica a resposta de seguranca existente e nao vaza dados

### Requirement: Historico automatico deve reutilizar a API existente
O sistema SHALL manter `GET /api/v1/empresas/{empresaId}/importacoes/producao` como historico paginado, com filtros existentes de setor, origem, status, arquivo e periodo; SHALL NOT criar historico automatico paralelo.

#### Scenario: Historico paginado
- **WHEN** o frontend consulta o endpoint de importacoes de producao
- **THEN** recebe pagina, setor, arquivo, origem, status e contagens disponiveis no DTO atual
