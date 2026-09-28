## Purpose

Disponibilizar RAG textual interno com ingestão validada, chunks versionados,
metadados de escopo e recuperação filtrada antes do modelo, mantendo documentos
de empresas e setores separados e permitindo reindexação segura.

## ADDED Requirements

### Requirement: Documentos devem ser ingeridos por operação autorizada

Se a ingestão for exposta por API, o sistema SHALL oferecer uma operação
administrativa vinculada à empresa, como
`POST /api/v1/empresas/{empresaId}/assistente/documentos`, exigindo permissão
de gestão de documentos/IA. A operação SHALL validar empresa, setor e
permissões antes de armazenar ou indexar qualquer conteúdo.

#### Scenario: Administrador autorizado envia documento textual

- **WHEN** um administrador autorizado envia TXT ou MD válido para empresa e
  setor permitidos
- **THEN** o sistema registra o documento, calcula hash, normaliza o texto e
  agenda ou executa a indexação sem disponibilizá-lo a outro escopo

#### Scenario: Usuário comum tenta ingerir documento

- **WHEN** um usuário sem a permissão administrativa envia um documento
- **THEN** o sistema rejeita a operação antes da persistência e não revela se
  já existe documento com aquele nome

### Requirement: Ingestão deve validar conteúdo e formato com segurança

O sistema SHALL impor limites de tamanho, nome, extensão, conteúdo extraível e
tipo MIME. TXT e MD SHALL ser suportados inicialmente. PDF somente poderá ser
aceito quando houver extractor seguro configurado; macros, scripts, conteúdo
executável e arquivos ilegíveis SHALL ser rejeitados.

#### Scenario: Arquivo não suportado ou perigoso

- **WHEN** o upload contém extensão proibida, macro, script, nome malicioso ou
  conteúdo não extraível
- **THEN** o sistema rejeita o arquivo, não o envia ao embedding/provider e
  registra apenas o motivo operacional mínimo

#### Scenario: PDF sem infraestrutura segura

- **WHEN** um PDF é enviado enquanto o extractor seguro não está habilitado
- **THEN** o sistema rejeita o formato com erro de validação e mantém TXT/MD
  disponíveis

### Requirement: Chunks devem ser determinísticos e versionados

O pipeline SHALL executar extração, normalização e chunking determinístico,
calcular hash do conteúdo normalizado e associar a cada chunk
`empresaId`, `documentId`, tipo, título, versão/hash, `sectorId` opcional e
permissões requeridas. Uma atualização SHALL invalidar a versão anterior de
forma atômica ou marcada como indisponível antes de liberar a nova.

#### Scenario: Mesmo documento é indexado duas vezes

- **WHEN** o conteúdo normalizado e seus metadados são iguais a uma versão já
  indexada
- **THEN** o sistema reconhece o mesmo hash, evita duplicação de chunks ativos
  e mantém o índice idempotente

#### Scenario: Documento é atualizado

- **WHEN** um administrador autorizado substitui o conteúdo por uma nova versão
- **THEN** os chunks antigos deixam de ser recuperáveis para novas perguntas e
  somente os chunks da versão válida podem ser retornados

### Requirement: Recuperação deve filtrar escopo antes do modelo

Toda busca SHALL aplicar `empresaId`, setores permitidos, permissões requeridas,
status de versão e limite de documentos antes do ranking final e antes de
enviar trechos ao provider. O vector store SHALL receber esses filtros como
parte da consulta, não como pós-processamento opcional.

#### Scenario: Pergunta cruza setores

- **WHEN** existem documentos iguais em vários setores, mas o usuário só pode
  acessar um deles
- **THEN** a busca retorna somente chunks do setor autorizado e a resposta não
  revela títulos, contagens ou conteúdo do setor excluído

#### Scenario: Documento de outra empresa tem alta similaridade

- **WHEN** um chunk de outra empresa seria o resultado semanticamente mais
  próximo
- **THEN** o filtro de empresa o exclui antes do ranking/contexto e ele nunca é
  enviado ao modelo

### Requirement: Reindexação e remoção devem preservar isolamento

O sistema SHALL permitir reindexar, invalidar ou remover documentos por
identificador dentro do escopo administrativo autorizado. Operações SHALL ser
idempotentes e não poderão alterar chunks de outra empresa/setor.

#### Scenario: Reindexação falha no embedding

- **WHEN** a nova versão não pode ser embeddada ou persistida no vector store
- **THEN** o sistema mantém a versão anterior válida, marca a falha de forma
  operacional e não publica índice parcial como versão completa

#### Scenario: Remoção de documento

- **WHEN** um administrador autorizado remove um documento
- **THEN** os chunks ativos deixam de ser recuperáveis, o evento administrativo
  é auditável sem conteúdo sensível e documentos de outros escopos permanecem
  intactos

### Requirement: RAG não pode substituir dados estruturados do ERP

Trechos recuperados SHALL ser usados para procedimentos, manuais e explicações;
fatos atuais de produção, paradas, metas, desempenho, financeiro, qualidade e
auditoria SHALL vir das tools correspondentes quando a pergunta exigir números
operacionais.

#### Scenario: Manual contém exemplo numérico

- **WHEN** o usuário pergunta pela produção atual e o RAG encontra um exemplo
  numérico em um manual
- **THEN** o sistema usa a tool operacional para a produção e identifica o
  trecho como explicação, sem tratá-lo como medição atual
