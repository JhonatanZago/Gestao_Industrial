# Tasks

## 1. Fundação e modelo de auditoria

- [x] 1.1 Adicionar Apache POI OOXML e configurações de limite de upload/linhas; verificar resolução da dependência e compilação.
- [x] 1.2 Criar enum de origem da produção e entidades de auditoria com status, hash, contagens, timestamps e erros; verificar relações sem cascade remove.
- [x] 1.3 Criar migrations V10/V11 para auditoria, origem e rastreabilidade opcional em `registro_producao`; verificar aplicação em H2 limpo e compatibilidade com registros antigos.
- [x] 1.4 Criar repositories, DTOs de resumo/detalhe e mappers da auditoria; verificar isolamento por Empresa, paginação e payload limitado.

## 2. Leitura e normalização XLSX

- [x] 2.1 Criar modelo intermediário de linha importada e códigos de erro estáveis.
- [x] 2.2 Implementar leitor XLSX seguro com fallback para primeira aba válida, rejeição de arquivos inválidos e sem execução de macros.
- [x] 2.3 Implementar mapeamento por cabeçalho normalizado e aliases para campos obrigatórios/opcionais.
- [x] 2.4 Implementar parser de códigos, quantidade, data e horário, preservando zeros à esquerda e suportando virada de meia-noite.
- [x] 2.5 Adicionar testes unitários de parsing, cabeçalhos, linhas vazias/parciais e formatos inválidos.

## 3. Pré-validação e resolução

- [x] 3.1 Criar serviço de pré-validação com contexto obrigatório de Empresa/Setor e normalização.
- [x] 3.2 Implementar resolução em lote de Funcionário, Peça, Máquina e Turno por Empresa, sem criação automática.
- [x] 3.3 Implementar validação de quantidade, período, setor/máquina, turno e sobreposição.
- [x] 3.4 Detectar duplicidade e sobreposição entre linhas do arquivo e contra produções persistidas.
- [x] 3.5 Adicionar testes de integração da pré-validação com arquivo válido, linha inválida, referências opcionais e zero persistência de produção.

## 4. Importação transacional

- [x] 4.1 Implementar pipeline all-or-nothing que só persiste produções após todas as linhas válidas.
- [x] 4.2 Reutilizar as tabelas de produção existentes, registrando origem `IMPORTACAO`, importação e linha de origem.
- [x] 4.3 Implementar hash SHA-256 e bloqueio de reimportação de hash concluído por Empresa/Setor.
- [x] 4.4 Persistir resultados por linha, contagens e estados do ciclo de vida da importação.
- [x] 4.5 Adicionar testes de sucesso com produção importada, zeros à esquerda, máquina/turno e períodos noturnos.

## 5. APIs e histórico

- [x] 5.1 Criar endpoint multipart de pré-validação no contexto de Empresa/Setor.
- [x] 5.2 Criar endpoint multipart de importação com rejeição integral em arquivo inválido.
- [x] 5.3 Criar endpoints paginados de histórico, detalhe, filtros e erros.
- [x] 5.4 Adicionar logs estruturados sem expor conteúdo sensível da planilha.
- [x] 5.5 Adicionar testes MockMvc de contratos de validação, importação, duplicidade e detalhe.

## 6. Documentação, regressão e aceite

- [x] 6.1 Atualizar README com formato XLSX, aliases, endpoints, atomicidade, limites e códigos de erro.
- [x] 6.2 Criar fixtures sintéticas controladas para XLSX válido e inválido nos testes.
- [x] 6.3 Executar `./gradlew.bat clean test` e confirmar a suíte verde.
- [x] 6.4 Executar `./gradlew.bat build` e `openspec validate "importacao-excel-producao" --type change --strict`.
