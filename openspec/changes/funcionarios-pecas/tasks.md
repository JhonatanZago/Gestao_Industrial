# Tasks

## 1. Modelo de Funcionario e persistencia

- [x] 1.1 Criar entidade Funcionario com codigo String, nome, ativo, timestamps e Empresa obrigatoria, sem setorPrincipal nesta change; verificar compilacao e mapeamento JPA.
- [x] 1.2 Criar migration Flyway para funcionario com FK para empresa, campos obrigatorios, unicidade `(empresa_id, codigo)` e indices de consulta; verificar aplicacao em H2 limpo.
- [x] 1.3 Criar FuncionarioRepository com busca por ID+Empresa, codigo, nome, ativo e paginacao; verificar consultas em teste de repositorio ou integracao.

## 2. API e regras de Funcionario

- [x] 2.1 Criar FuncionarioRequest/Response e mapper manual seguindo o padrao existente; verificar que a API nao expoe entidade JPA.
- [x] 2.2 Implementar FuncionarioService com normalizacao de codigo/nome, preservacao de zeros, duplicidade por Empresa, existencia e ciclo de vida logico; verificar testes unitarios ou de integracao.
- [x] 2.3 Implementar endpoints aninhados de Funcionario para criar, listar, consultar, atualizar, ativar e desativar, com Pageable e filtros por codigo/nome/ativo; verificar status HTTP e resposta paginada.
- [x] 2.4 Adicionar testes de Funcionario para codigo `00147`, campos invalidos, empresa inexistente, duplicidade na mesma Empresa e mesmo codigo em Empresas diferentes; verificar suite da capability verde.
- [x] 2.5 Adicionar teste de isolamento que tente acessar, atualizar e alterar status de funcionario por Empresa incorreta; verificar retorno 404 e ausencia de modificacao.

## 3. Modelo de Peca e persistencia

- [x] 3.1 Criar entidade Peca com codigo String, descricao, ativo, timestamps e Empresa obrigatoria, sem vinculo fixo a Setor; verificar compilacao e mapeamento JPA.
- [x] 3.2 Criar migration Flyway para peca com FK para empresa, campos obrigatorios, unicidade `(empresa_id, codigo)` e indices de consulta; verificar aplicacao em H2 limpo sem alterar migrations anteriores.
- [x] 3.3 Criar PecaRepository com busca por ID+Empresa, codigo, descricao, ativo e paginacao; verificar consultas em teste de repositorio ou integracao.

## 4. API e regras de Peca

- [x] 4.1 Criar PecaRequest/Response e mapper manual seguindo o padrao existente; verificar resposta sem entidade JPA e preservacao textual do codigo.
- [x] 4.2 Implementar PecaService com normalizacao, suporte a codigos numericos/alfanumericos/hifenizados, duplicidade por Empresa, existencia e ciclo de vida logico; verificar testes de regra.
- [x] 4.3 Implementar endpoints aninhados de Peca para criar, listar, consultar, atualizar, ativar e desativar, com Pageable e filtros por codigo/descricao/ativo; verificar status HTTP e paginacao.
- [x] 4.4 Adicionar testes de Peca para zeros, codigo alfanumerico, hifen, campos invalidos, empresa inexistente, duplicidade e mesmo codigo em Empresas diferentes; verificar suite da capability verde.
- [x] 4.5 Adicionar teste de isolamento de Peca por Empresa e teste comprovando que a Peca pode ser criada sem setor fixo; verificar retorno 404 no acesso cruzado.

## 5. Regressao, documentacao e aceite

- [x] 5.1 Reutilizar o tratamento global de excecoes da TASK 01 para conflitos, validacao e recursos ausentes; verificar contrato JSON e ausencia de stack trace nos testes.
- [x] 5.2 Atualizar README com entidades, endpoints, regras de codigo String, unicidade por Empresa e ausencia de setor fixo; verificar comandos e exemplos documentados.
- [x] 5.3 Executar `./gradlew.bat clean test`, corrigir regressao da TASK 01 e confirmar todos os testes novos; verificar relatorio Gradle verde.
- [x] 5.4 Executar `./gradlew.bat build` e `openspec validate "funcionarios-pecas" --type change --strict`; verificar build e validacao OpenSpec aprovados.
