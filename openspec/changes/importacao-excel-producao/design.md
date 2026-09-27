# Design

## Context

O sistema possui cadastros de Empresa, Setor, Funcionário, Peça, Máquina e Turno, além do `RegistroProducao` com validações de quantidade, período, sobreposição e associações opcionais. Ainda não há dependência para leitura de planilhas, entidade de auditoria ou fluxo de importação.

## Goals / Non-Goals

**Goals:**

- Separar leitura XLSX, normalização, resolução/validação e persistência final.
- Reaproveitar as mesmas regras e tabelas de produção do cadastro manual.
- Garantir relatório por linha, hash, histórico e atomicidade all-or-nothing.
- Manter uma extensão futura para perfis, processamento assíncrono e modo parcial sem implementá-los agora.

**Non-Goals:**

- Suporte obrigatório a `.xls`, frontend, edição/exportação de Excel, dashboard, mensageria, autenticação e importação parcial.
- Criação automática de cadastros ausentes ou parada independente de produção.

## Decisions

### Apache POI e XLSX somente

Adicionar Apache POI OOXML (`poi-ooxml`) e aceitar `.xlsx` com verificação de extensão, MIME quando disponível e tentativa de abertura do workbook. `.xls` será rejeitado nesta primeira versão para evitar ampliar o conjunto de parsers e riscos de compatibilidade.

### Leitor em camadas e perfil interno

Separar `ExcelWorkbookReader`, `HeaderMapper`, `CellValueParser` e modelo intermediário `LinhaProducaoImportada`. Aliases serão centralizados em um perfil interno versionável, com seleção de aba explícita opcional e fallback para a primeira aba que contenha cabeçalhos obrigatórios. Não haverá CRUD de perfil nesta task.

### Contexto explícito de Empresa e Setor

Os endpoints são aninhados em Empresa/Setor. Códigos da planilha serão normalizados e resolvidos em lote por Empresa para mapas de funcionário, peça, máquina e turno. Setor informado no arquivo será apenas uma validação adicional contra o setor da URL.

### Auditoria e resultados por linha

Persistir `ImportacaoProducao` e `ResultadoLinhaImportacao`, com status técnico estável, hash, contagens e erros. A linha intermediária não será persistida antes da decisão final; resultados de erro serão persistidos para auditoria. Produções terão origem `MANUAL`/`IMPORTACAO` e referência de importação/linha, com colunas opcionais para preservar dados antigos.

### Pré-validação seguida de persistência transacional

Validação e importação usarão o mesmo pipeline. O endpoint `validar` executa somente leitura e grava a auditoria/erros; o endpoint de importação só inicia a transação de produção após o arquivo completo ser válido. Uma única transação grava importação, resultados e produções; falha inesperada faz rollback de todos os novos registros e atualiza a auditoria de falha por uma operação transacional segura.

### Datas, horários e fórmulas

Usar `DataFormatter`/formatos explícitos para preservar o valor exibido de códigos. Datas aceitas serão células Excel válidas, `dd/MM/yyyy` e `yyyy-MM-dd`; horários `HH:mm` e `HH:mm:ss`. Fim menor que início só avança um dia quando o perfil/contexto declarar operação noturna; fórmulas não serão avaliadas como código executável e campos críticos sem valor seguro serão rejeitados.

### APIs e limites

Adicionar endpoints multipart de validar/importar, histórico paginado e detalhe paginado de erros. Configurar limite de tamanho no Spring e um limite de linhas por arquivo; arquivos acima do limite são rejeitados antes da persistência. Logs incluirão identificador, Empresa, Setor, arquivo, status, contagens e duração, sem conteúdo desnecessário da planilha.

## Risks / Trade-offs

- [Risco] Planilhas reais têm aliases e formatações inconsistentes. → [Mitigação] Perfil centralizado, aliases explícitos e relatório de cabeçalho/linha sem heurística ilimitada.
- [Risco] All-or-nothing pode rejeitar arquivos com um único erro. → [Mitigação] Pré-validação detalhada e arquitetura separada para futuro modo parcial.
- [Risco] Grandes arquivos podem consumir memória. → [Mitigação] Limite configurável, leitura controlada, mapas de códigos distintos e processamento em lote; mensageria fica futura.
- [Risco] Alterações em produção podem quebrar construtores/consultas existentes. → [Mitigação] Colunas nulas, origem manual como default e regressão completa das TASKS 01–05.
- [Risco] Fórmulas ou macros podem conter conteúdo não confiável. → [Mitigação] XLSX sem execução de macros, parser seguro e rejeição de campos críticos sem valor calculado confiável.

## Migration Plan

1. Adicionar dependência POI e configuração de limite de upload/linhas.
2. Criar migrations incrementais para auditoria, resultados por linha e origem/rastreabilidade opcional em produção.
3. Implantar endpoints de validação e importação; dados antigos permanecem como origem manual e sem importação associada.
4. Validar fixtures XLSX e suíte completa antes da liberação.
5. Em rollback de código, manter tabelas e colunas nulas; migrations aplicadas não serão editadas nem removidas automaticamente.
