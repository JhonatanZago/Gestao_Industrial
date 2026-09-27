# Tasks

## 1. Projeto e configuracao

- [x] 1.1 Configurar dependencias Spring Web, JPA, Validation, Lombok, PostgreSQL, Flyway, H2, JUnit 5 e Mockito no Gradle, mantendo Java 17; verificar com `./gradlew.bat test` que o projeto compila.
- [x] 1.2 Criar configuracoes separadas para runtime PostgreSQL e testes H2, com migrations habilitadas e sem depender de `ddl-auto=create`; verificar a inicializacao do contexto no teste de aplicacao.
- [x] 1.3 Criar a estrutura de pacotes para domain, dto/request, dto/response, exception, mapper, repository, resource, service e config; verificar a compilacao.

## 2. Modelo persistente e migrations

- [x] 2.1 Implementar entidades Empresa e Setor com campos, status, timestamps, relacionamento obrigatorio e mapeamento sem serializacao circular; verificar testes de persistencia e associacao.
- [x] 2.2 Criar migrations versionadas para tabelas, campos obrigatorios, foreign key, unicidade de CNPJ e unicidade de `(empresa_id, codigo)`; verificar aplicacao em banco H2 limpo.
- [x] 2.3 Implementar repositories para CNPJ, codigo/nome por Empresa, listagem paginada e filtros; verificar compilacao e consultas da integracao.

## 3. Empresa

- [x] 3.1 Criar DTOs request/response, mapper manual e validacoes basicas para Empresa; verificar rejeicao de campos obrigatorios vazios.
- [x] 3.2 Implementar service de Empresa com normalizacao, duplicidade e timestamps/status; verificar testes de criacao, atualizacao e conflito.
- [x] 3.3 Implementar endpoints de Empresa com paginacao e ativacao/desativacao; verificar testes de integracao de ciclo de vida.

## 4. Setor dinamico e isolamento

- [x] 4.1 Criar DTOs, mapper e service de Setor com codigo canonico, nome normalizado, descricao, Empresa obrigatoria e unicidade; verificar setor novo sem enum.
- [x] 4.2 Implementar endpoints aninhados de Setor para criar, listar, consultar, atualizar, ativar e desativar; verificar testes de integracao.
- [x] 4.3 Validar `empresaId + setorId` em toda operacao, impedindo acesso cruzado e setores orfaos; verificar teste entre Empresas A e B.

## 5. Erros, testes e documentacao

- [x] 5.1 Implementar excecoes especificas e `@RestControllerAdvice` com erros padronizados, sem stack trace; verificar contratos JSON nos testes.
- [x] 5.2 Adicionar testes automatizados de Empresa para criacao, consulta, ciclo de vida e duplicidade; verificar suite verde.
- [x] 5.3 Adicionar testes automatizados de Setor para normalizacao, associacao, isolamento e duplicidade; verificar suite verde.
- [x] 5.4 Criar README com objetivo, stack, Java 17, PostgreSQL, execucao, testes, migrations, estrutura e endpoints; verificar comandos documentados.

## 6. Integracao e aceite

- [x] 6.1 Executar `./gradlew.bat clean test`, corrigir falhas reais e confirmar a suite verde; verificar relatorio Gradle.
- [x] 6.2 Executar `./gradlew.bat build` e revisar endpoints e criterios da spec; verificar build sem erros e registrar limitacoes reais.
