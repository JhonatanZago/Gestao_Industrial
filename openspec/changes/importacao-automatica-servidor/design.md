# Design

## Context

O projeto usa Java 17, Spring Boot, JPA/Flyway, Apache POI e PostgreSQL/H2. A entrada manual atual está em `ImportacaoProducaoResource`, que chama `ImportacaoProducaoService`; este calcula SHA-256, cria o histórico, lê com `ExcelImportReader`, valida com `ImportacaoValidator` e persiste via `ImportacaoLifecycleService`. O histórico `ImportacaoProducao` já registra empresa, setor, nome, hash, status, contagens, timestamps, correlação e erro. `RegistroProducao` já possui origem, referência à importação e linha de origem.

O fluxo manual exposto é:

`POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/validar` ou `POST .../importacoes/producao` → resource → `ImportacaoProducaoService` → hash/reader → validator → lifecycle → produção/auditoria.

O código atual rejeita o mesmo hash concluído por empresa/setor e detecta sobreposição temporal por funcionário, mas o repositório não demonstra uma chave natural de registro que permita afirmar, sem investigação adicional, que linhas antigas de uma nova versão serão ignoradas individualmente.

## Goals / Non-Goals

**Goals:**

- Fazer a auditoria do comportamento real antes de qualquer refatoração.
- Manter o importador manual e suas validações, segurança e respostas compatíveis.
- Definir um contrato de pipeline único para manual, futuro watcher e futura reconciliação.
- Registrar origem, versão do arquivo e resultado sem duplicar estruturas já existentes.
- Provar o que o modelo atual garante sobre reimportação e interromper a decisão onde faltarem dados de identificação segura.
- Deixar configuração futura desativada por padrão e segura para ambientes reais.

**Non-Goals:**

- Não ativar nem implementar WatchService, polling, fila concorrente, retry ou scheduler das 18:00.
- Não conectar ao compartilhamento da empresa, criar credenciais, caminho real ou tela frontend.
- Não inventar regra de identidade de uma produção nem alterar silenciosamente a regra de negócio existente.

## Decisions

### Pipeline central por contexto de fonte

O processamento deverá ser modelado em torno de um serviço de domínio equivalente a `ImportacaoProducaoService`, recebendo um arquivo/stream e contexto de empresa, setor, nome, caminho opcional e origem. Manual e futuras entradas deverão atravessar reader, validação, persistência, auditoria e resultado comuns. A alternativa de criar serviços separados foi rejeitada por duplicar regras e permitir divergência.

Nesta primeira etapa, a extração só será feita se a auditoria mostrar acoplamento que impeça esse contrato; uma simples renomeação ou camada vazia não será criada.

### Origem compatível

Ampliar o enum existente `OrigemProducao` somente se isso puder ser feito sem quebrar dados legados. `MANUAL` preserva registros manuais; `FILE_WATCHER` e `RECONCILIACAO` ficam disponíveis para o pipeline futuro, sem produtores ativos nesta fase. Se a origem atual `IMPORTACAO` for necessária para compatibilidade histórica, a decisão deverá ser documentada e coberta por teste/migration, não substituída cegamente.

### Histórico existente antes de novo modelo

Usar `ImportacaoProducao` como histórico da tentativa quando seus campos forem suficientes. Só criar migration/entidade complementar se a auditoria comprovar lacuna indispensável para caminho, tamanho, origem, estados de estabilização ou contagem de tentativas. Uma entidade duplicada conceitualmente a `ImportacaoArquivo` é proibida.

### Hash de arquivo separado da idempotência de registros

Centralizar o cálculo determinístico SHA-256 para `byte[]`/stream reutilizável e testar conteúdo igual/diferente. O hash identifica a versão do arquivo e não autoriza concluir que os registros internos são novos. A decisão de idempotência por linha deve reutilizar uma chave existente do domínio; se ela não existir, a entrega documentará `LIMITATION` e não inventará uma combinação de campos.

### Configuração segura e inativa

Adicionar somente propriedades externalizadas equivalentes a `industrial.importacao.automatica.enabled=false` e estrutura futura para setor/caminho, sem valor real. Caminhos, usuários e senhas entram apenas pelo ambiente seguro de implantação; não serão logados nem enviados ao frontend. A alternativa de embutir caminho no código foi rejeitada.

### Transação e compatibilidade

Preservar o comportamento all-or-nothing e o mecanismo atual de lifecycle/transações até que testes comprovem uma mudança segura. O endpoint manual continuará chamando o pipeline comum e retornando os contratos atuais. Falhas inesperadas continuarão registradas sem deixar produção parcial.

## Architecture

Arquitetura preparada, mas não ativada nesta etapa:

```text
Upload manual ───────────────┐
                             ▼
                      Pipeline central
                      validação → POI
                      regras → banco

Pasta do servidor → Watcher → estabilização → hash → fila ─┘

Scheduler 18:00 → reconciliação → hash/histórico → Pipeline central
```

O watcher e a reconciliação serão consumidores futuros do mesmo contrato, não implementações alternativas das regras de produção.

## File Watcher phase

`FileChangeDetector → FileChangeDebouncer → FileStabilityService → SHA-256 existente → idempotência → ImportacaoProducaoService → histórico`

`FileChangeDetector` é uma abstração com implementação `WatchServiceFileChangeDetector`. WatchService não é garantia absoluta em filesystem de rede; um `PollingFileChangeDetector` poderá ser adicionado futuramente sem alterar o pipeline. O callback apenas agenda o caminho. A estabilização compara tamanho e `lastModified`, tenta abrir o arquivo e respeita timeout.

O controle de concorrência é por caminho, permitindo arquivos de setores distintos em paralelo. Eventos durante processamento aguardam o lock do mesmo arquivo e não são descartados. `industrial.importacao.automatica.enabled` é `false` por padrão; diretórios usam `empresa-id`, `setor-id` e `path` por configuração externa. Diretórios inválidos são isolados e logados.

## Reconciliation phase

`@Scheduled(cron = propriedade, zone = propriedade) → ReconciliationService.reconciliar() → scan de diretórios → filtro comum → FileStabilityService → hash do ImportacaoProducaoService → histórico → pipeline existente`

O cron padrão é `0 0 18 * * *` e o timezone padrão é `America/Sao_Paulo`, ambos externalizados. O scheduler não contém regras próprias de importação e só chama o serviço interno. A reconciliação ignora hashes concluídos, tenta novamente apenas versões sem histórico concluído e registra resumo em memória/logs; cada arquivo é isolado para que falhas não interrompam os demais.

Watcher = baixa latência. Reconciliação 18h = garantia complementar para eventos perdidos. O mesmo lock por caminho deve ser compartilhado entre watcher e reconciliação, evitando importação concorrente da mesma versão.

## Risks / Trade-offs

- [Risco] O hash atual bloqueia o arquivo inteiro, mas não resolve idempotência por linha em uma versão alterada. → [Mitigação] Criar testes A/A alterado, inspecionar o modelo e registrar `LIMITATION` se não houver chave segura.
- [Risco] Alterar enum ou schema pode quebrar registros existentes. → [Mitigação] Preservar valores legados, usar migration incremental e executar regressão completa.
- [Risco] Refatorar cedo pode mudar o fluxo manual validado. → [Mitigação] Auditar primeiro, extrair apenas seams necessários e comparar testes antes/depois.
- [Risco] Caminhos compartilhados e credenciais podem vazar em logs/configuração. → [Mitigação] Defaults inativos, placeholders, configuração por ambiente e testes de sanitização.
- [Risco] O escopo automático crescer durante a implementação. → [Mitigação] Tratar Watcher, scheduler, fila e servidor real como fases posteriores e parar após a fundação.

## Migration Plan

1. Registrar a auditoria do fluxo e a linha de base da suíte.
2. Implementar apenas ajustes de fundação necessários, com migrations incrementais e compatibilidade legada.
3. Executar os testes obrigatórios, a suíte completa e build.
4. Validar os artefatos OpenSpec.
5. Parar e apresentar auditoria, idempotência, pipeline, histórico, hash, testes, build, riscos e arquivos alterados para aprovação antes de qualquer fase automática.

## Idempotência entre versões — resultado da Fase 1

Os testes de integração executados em `ImportacaoExcelIntegrationTests` comprovaram:

- O mesmo XLSX, reenviado com o mesmo conteúdo, recebe `409 CONFLICT` pelo hash já concluído e não cria nova produção.
- Uma V1 com duas linhas é concluída e grava 2 registros.
- Uma V2 com as mesmas duas linhas e uma terceira linha produz hash diferente, mas é rejeitada com status de importação `REJEITADA`, pois as duas linhas antigas são detectadas como `SOBREPOSICAO` contra produções já persistidas.
- O banco permanece com exatamente 2 registros de produção; a linha nova não é importada.

Conclusão histórica da Fase 1: `LIMITATION`. Esta limitação foi resolvida nesta fase pela identidade segura documentada abaixo; nenhuma automação foi ativada.

Os campos atualmente disponíveis em `RegistroProducao` são: empresa, setor, funcionário, peça, máquina opcional, turno opcional, origem, referência opcional à importação, linha de importação, fingerprint do registro, quantidade, início, fim, observação, ativo, `createdAt` e `updatedAt`.

## Idempotência incremental entre versões — decisão desta fase

### Identidade analisada

| Campo | Classificação | Participa da identidade? |
|---|---|---|
| empresa, setor | identidade de negócio / escopo | sim |
| funcionário, peça | identidade de negócio | sim |
| quantidade, início, fim | dado do registro | sim |
| máquina, turno | dado opcional, atualmente não persistido pelo importador Excel | não |
| observação | dado do registro, não existe no template oficial | não |
| origem, importação/linha de origem | metadado de importação | não |
| id, timestamps, status | campo gerado/metadado | não |

A identidade segura é `empresa + setor + funcionário + peça + quantidade + início + fim`. A regra existente de sobreposição impede duas produções do mesmo funcionário em intervalos sobrepostos, portanto duas produções legítimas não podem possuir simultaneamente esses valores. Duplicatas dentro do mesmo arquivo continuam rejeitadas.

`fingerprintRegistro` é SHA-256 da forma canônica `empresaId|setorId|funcionarioId|pecaId|quantidade|inicio|fim`, com decimal determinístico e null consistente. Número da linha, ID do banco, ID da importação, timestamp e hash do arquivo não participam. `hashArquivo` identifica a versão física do XLSX; `fingerprintRegistro` identifica o conteúdo relevante da produção.

Na evolução incremental, fingerprints existentes são registrados como `IGNORADO`/`JA_PROCESSADO`; somente linhas novas são persistidas. Alteração de conteúdo gera outro fingerprint, mas o intervalo sobreposto continua conflito de negócio e é rejeitado, sem atualização silenciosa. Registros legados com fingerprint nulo são encontrados por fallback nos mesmos campos durante a transição; novos registros recebem o fingerprint e ficam sujeitos ao índice único do banco.

## Open Questions

- A identidade incremental está definida para o modelo atual. File Watcher, reconciliação e Scheduler continuam fora desta fase.
## Shared server integration phase

`AutomaticImportProperties` e a unica fonte de configuracao. Cada entrada possui `empresaId`, `setorId`, `path` e `kind` (`DIRECTORY` ou `FILE`), podendo ser populada por variaveis de ambiente/indexed properties. O padrao continua `enabled=false`; nenhum caminho real ou credencial e conhecido pelo codigo.

`AutomaticImportFilePolicy` concentra o filtro de XLSX, temporarios e arquivos ocultos e e compartilhado pelo watcher e pela reconciliacao. `AutomaticImportConfigurationValidator` verifica preenchimento, sintaxe, existencia, leitura e tipo sem modificar o filesystem. `ImportacaoAutomaticaHealthService` fornece diagnostico interno por empresa/setor sem endpoint publico.

O modo `dry-run` interrompe o coordenador antes do pipeline transacional, nao cria `ImportacaoProducao` nem producao e emite log explicito. `initial-mode=IGNORE_EXISTING` e o default para impedir importacao silenciosa do acervo na primeira conexao; `PROCESS_EXISTING` exige configuracao explicita. `ImportacaoBaseline` persiste somente escopo, chave da fonte, nome e hash observado; ela nao e historico de importacao concluida. Assim, apos restart, um hash inalterado e ignorado e uma alteracao posterior pode seguir para o pipeline.

O detector observa CREATE, MODIFY e DELETE; remocao nunca apaga producao. Em rede, perda de acesso nao derruba a aplicacao: o diagnostico marca a fonte indisponivel e a reconciliacao pode recuperar o estado. A abstracao `FileChangeDetector` preserva uma futura implementacao polling sem reescrever o pipeline.
## Status API

`ImportacaoAutomaticaStatusService` consulta `AutomaticImportProperties`, `ServidorExcelWatcher`, `ImportacaoAutomaticaHealthService`, `ImportacaoProducaoRepository` e `SetorRepository`. A rota e `GET /api/v1/empresas/{empresaId}/importacoes/automaticas/status`, protegida por `OPERACAO_MONITORAR` e pelo escopo normal da empresa. O DTO nao inclui path, UNC, IP ou credenciais.

`ultimaVerificacao` e um estado em memoria atualizado quando o diagnostico e consultado; reiniciar a aplicacao reinicia esse valor. A ultima importacao e derivada do historico existente. A reconciliação retorna cron/timezone reais, sem parser artesanal de cron. O endpoint nao controla watcher, scheduler ou credenciais.
