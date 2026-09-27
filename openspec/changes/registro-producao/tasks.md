# Tasks

## 1. Modelo persistente e migration

- [x] 1.1 Criar entidade RegistroProducao com Empresa, Setor, Funcionario, Peca, BigDecimal quantidade, LocalDateTime inicio/fim, observacao, ativo e auditoria; verificar compilacao e ausencia de setor fixo adicional.
- [x] 1.2 Criar migration Flyway V5 com tabela, FKs obrigatorias, DECIMAL(19,3), timestamps, status e indices para empresa/per periodo/recursos; verificar aplicacao em H2 limpo sem alterar V1-V4.
- [x] 1.3 Criar RegistroProducaoRepository com busca por escopo, filtros e consulta de existencia de sobreposicao no banco; verificar que intervalos adjacentes nao sao considerados conflito.

## 2. DTOs, calculo e regras de dominio

- [x] 2.1 Criar request/response e mapper sem expor JPA, incluindo dados suficientes dos recursos e duracao bruta em minutos; verificar payload do cenario 350 unidades retorna 88 minutos.
- [x] 2.2 Implementar validacao de quantidade positiva, inicio/fim, atravessamento de meia-noite e normalizacao de observacao; verificar rejeicao de zero, negativo e intervalo invalido.
- [x] 2.3 Implementar service para validar Empresa, Setor, Funcionario e Peca na mesma empresa e ativos em create/update; verificar rejeicao de recurso ausente, inativo ou de outra empresa.
- [x] 2.4 Implementar verificacao de sobreposicao para create e update ignorando o proprio ID; verificar parcial, contido e envolvente rejeitados, adjacente e funcionarios diferentes permitidos.

## 3. API e consultas

- [x] 3.1 Implementar Resource aninhado para POST, GET paginado, GET por ID, PUT e PATCH de ativar/desativar; verificar status HTTP, isolamento e ausencia de DELETE fisico.
- [x] 3.2 Implementar Specification para filtros combinados por setor, funcionario, peca, periodo e ativo com ordenacao `inicio DESC`; verificar filtros e paginacao.
- [x] 3.3 Reutilizar o GlobalExceptionHandler para erros de validacao, recurso ausente e conflito temporal; verificar mensagens claras sem stack trace.

## 4. Testes da capability

- [x] 4.1 Criar testes para registro valido, duracao de 88 minutos, intervalo atravessando meia-noite, quantidade e horarios invalidos; verificar suite de dominio/API.
- [x] 4.2 Criar testes para Empresa/Setor/Funcionario/Peca ausentes, inativos e pertencentes a outra empresa; verificar que nenhum registro inconsistente e criado.
- [x] 4.3 Criar testes de sobreposicao parcial, contida, envolvente, adjacente, funcionarios diferentes e update ignorando o proprio registro; verificar regras temporais.
- [x] 4.4 Criar testes de filtros, paginacao, consulta por escopo, ativacao/desativacao e historico apos inativacao dos recursos; verificar contratos HTTP.

## 5. Regressao, documentacao e aceite

- [x] 5.1 Atualizar README com Registro de Produção, payload, endpoints, duracao bruta, fuso operacional e limites fora do escopo; verificar exemplos executaveis.
- [x] 5.2 Executar `./gradlew.bat clean test`, corrigir regressao das TASKS 01/02 e confirmar testes novos; verificar relatorio Gradle verde.
- [x] 5.3 Executar `./gradlew.bat build` e `openspec validate "registro-producao" --type change --strict`; verificar build e validacao OpenSpec aprovados.
