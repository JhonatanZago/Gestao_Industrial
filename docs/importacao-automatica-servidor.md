# Integracao segura com pasta compartilhada

O backend aceita fontes configuradas por ambiente, sem endereco real no repositorio. O formato e uma lista indexada Spring:

```properties
IMPORT_AUTO_ENABLED=false
IMPORT_AUTO_DRY_RUN=true
IMPORT_AUTO_INITIAL_MODE=IGNORE_EXISTING
INDUSTRIAL_IMPORT_AUTOMATICA_DIRECTORIES_0_EMPRESA_ID=1
INDUSTRIAL_IMPORT_AUTOMATICA_DIRECTORIES_0_SETOR_ID=1
INDUSTRIAL_IMPORT_AUTOMATICA_DIRECTORIES_0_PATH=<caminho-fornecido-pelo-ambiente>
INDUSTRIAL_IMPORT_AUTOMATICA_DIRECTORIES_0_KIND=DIRECTORY
```

Use os nomes de propriedades do ambiente de execucao conforme o binding Spring adotado; nunca grave IP, UNC, usuario ou senha no Git. `kind=FILE` monitora somente um XLSX; `DIRECTORY` lista os XLSX elegiveis.

Para homologacao, mantenha `IMPORT_AUTO_ENABLED=true` com `IMPORT_AUTO_DRY_RUN=true`, use uma pasta temporaria controlada, confira os logs `AUTO_IMPORT_DRY_RUN` e o diagnostico interno, e depois desative rapidamente com `IMPORT_AUTO_ENABLED=false`. O modo inicial seguro e `IGNORE_EXISTING`; ele impede importar o acervo antigo implicitamente. `PROCESS_EXISTING` exige decisao operacional explicita.

O processo necessita apenas permissao de leitura. Ele nao edita, remove, renomeia ou move planilhas. O WatchService oferece baixa latencia; a reconciliacao das 18h cobre eventos perdidos em filesystem de rede. O acesso ao servidor real e uma etapa operacional posterior e nao e executado automaticamente pelos testes.
