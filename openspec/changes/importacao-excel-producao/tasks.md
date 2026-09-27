# Tasks

## 1. Fundação e modelo de auditoria

- [ ] 1.1 Adicionar Apache POI OOXML e configurações de limite de upload/linhas; verificar resolução da dependência e compilação.
- [ ] 1.2 Criar enum de origem da produção e entidades `ImportacaoProducao`/`ResultadoLinhaImportacao` com status, hash, contagens, timestamps e erros; verificar relações sem cascade remove.
- [ ] 1.3 Criar migration V10 para auditoria e V11 para origem/rastreabilidade opcional em `registro_producao`; verificar aplicação em H2 limpo e compatibilidade com registros antigos.
- [ ] 1.4 Criar repositories, DTOs de resumo/detalhe e mappers da auditoria; verificar isolamento por Empresa, paginação e payload limitado.

## 2. Leitura e normalização XLSX

- [ ] 2.1 Criar modelo intermediário `LinhaProducaoImportada` e códigos de erro estáveis; verificar separação entre valores da planilha e entidades de domínio.
- [ ] 2.2 Implementar leitor XLSX com seleção de aba explícita e fallback para primeira aba válida, rejeitando workbook vazio/corrompido, extensão insegura, macros e fórmulas críticas sem valor seguro; verificar fixtures válidas e problemáticas.
- [ ] 2.3 Implementar mapeamento por cabeçalho normalizado e aliases para campos obrigatórios/opcionais; verificar colunas reordenadas, aliases aceitos e `CABECALHO_AUSENTE`.
- [ ] 2.4 Implementar parser de códigos, quantidade, data, horário e data+horário, preservando zeros à esquerda e tratando virada de meia-noite somente pela regra documentada; verificar formatos válidos e inválidos.
- [ ] 2.5 Adicionar testes unitários de parsing, cabeçalhos, linhas vazias/parciais, fórmulas, datas seriais e códigos empresariais; verificar todos os códigos de erro do parser.

## 3. Pré-validação e resolução

- [ ] 3.1 Criar serviço de pré-validação com contexto obrigatório de Empresa/Setor e perfil interno de aliases; verificar setor da planilha divergente, campos obrigatórios e normalização.
- [ ] 3.2 Implementar resolução em lote de Funcionário, Peça, Máquina e Turno por Empresa, sem criação automática e respeitando status; verificar códigos inexistentes, inativos e de outra Empresa.
- [ ] 3.3 Implementar validação de quantidade, período, setor/máquina, turno, produção atravessando troca de turno e demais regras usando os serviços/repositories existentes; verificar mensagens e códigos estáveis.
- [ ] 3.4 Detectar duplicidade exata e sobreposição entre linhas do próprio arquivo e contra produções persistidas; verificar que ambas as linhas conflitantes aparecem no relatório.
- [ ] 3.5 Adicionar testes unitários e de integração da pré-validação com arquivo todo válido, uma linha inválida, conflito interno, conflito com banco e referências opcionais; verificar zero persistência durante validação.

## 4. Importação transacional

- [ ] 4.1 Implementar pipeline all-or-nothing que só persiste produções depois de todas as linhas válidas; verificar rollback completo quando uma linha falha.
- [ ] 4.2 Reutilizar validações e criação de produção existentes, registrando origem `IMPORTACAO`, importação e linha de origem sem criar tabela paralela de produção; verificar compatibilidade de payloads manuais.
- [ ] 4.3 Implementar hash SHA-256 e bloqueio de reimportação de hash concluído por Empresa/Setor, permitindo arquivo corrigido com hash diferente quando sem conflitos; verificar `ARQUIVO_DUPLICADO`.
- [ ] 4.4 Adicionar persistência/atualização consistente dos resultados por linha, contagens e estados `RECEBIDA` a `CONCLUIDA`/`REJEITADA`/`FALHOU`; verificar auditoria em sucesso e falha.
- [ ] 4.5 Adicionar testes de sucesso com produção manual/importada, zeros à esquerda, máquina/turno e turno noturno; verificar quantidades, períodos e rastreabilidade.

## 5. APIs e histórico

- [ ] 5.1 Criar endpoint multipart de pré-validação em `/api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/validar`; verificar resumo e erros sem produções persistidas.
- [ ] 5.2 Criar endpoint multipart de importação no contexto de Empresa/Setor; verificar status HTTP, resumo concluído e rejeição integral em arquivo inválido.
- [ ] 5.3 Criar endpoints paginados de histórico e detalhe com filtros por setor/status/período/nome e erros pagináveis; verificar isolamento e ausência de stack trace.
- [ ] 5.4 Adicionar logs estruturados com importacaoId, empresaId, setorId, arquivo, status, contagens e duração; verificar que conteúdo sensível da planilha não é exposto.
- [ ] 5.5 Adicionar testes MockMvc de contratos, arquivo ausente/excedente/corrompido, acesso cruzado, hash duplicado e consulta do detalhe; verificar códigos de erro seguros.

## 6. Documentação, regressão e aceite

- [ ] 6.1 Atualizar README com formato XLSX, aliases, endpoints, atomicidade, limites, códigos de erro, histórico e itens fora do escopo; verificar exemplos executáveis.
- [ ] 6.2 Criar fixtures sintéticas controladas para XLSX válido, inválido, noturno, duplicado, conflito e zeros à esquerda; verificar que não dependem de arquivos externos instáveis.
- [ ] 6.3 Executar `./gradlew.bat clean test`, corrigir regressões das TASKS 01–05 e confirmar todos os testes novos; verificar suíte Gradle verde.
- [ ] 6.4 Executar `./gradlew.bat build` e `openspec validate "importacao-excel-producao" --type change --strict`; verificar build e validação aprovados.
