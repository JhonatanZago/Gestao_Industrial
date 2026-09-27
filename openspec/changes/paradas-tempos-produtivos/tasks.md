# Tasks

## 1. Motivos de parada

- [x] 1.1 Criar entidade MotivoParada com Empresa, codigo, nome, descricao, categoria textual opcional, ativo e auditoria; verificar compilacao e ausencia de enum para motivos.
- [x] 1.2 Criar migration V6 com FK para empresa, unicidade `(empresa_id, codigo)`, campos obrigatorios e indices; verificar aplicacao em H2 sem alterar V1-V5.
- [x] 1.3 Criar repository, DTOs, mapper, service e endpoints paginados de motivos com filtros e ciclo de vida; verificar isolamento e conflitos.
- [x] 1.4 Adicionar testes de motivo para normalizacao, duplicidade, empresas diferentes, atualizacao, ativacao/desativacao e acesso cruzado; verificar suite da capability.

## 2. Paradas de producao

- [x] 2.1 Criar entidade ParadaProducao com Empresa, RegistroProducao, MotivoParada, inicio/fim, observacao, ativo e auditoria; verificar FKs obrigatorias e ausencia de cascade remove.
- [x] 2.2 Criar migration V7 com FKs, campos, constraints e indices por producao, empresa, motivo e periodo; verificar aplicacao em H2 limpo.
- [x] 2.3 Criar repositories para escopo, periodo, soma de duracao ativa e existencia de sobreposicao no banco; verificar adjacencia permitida sem carregar todo o sistema.
- [x] 2.4 Criar DTOs, mappers, service e endpoints aninhados de parada para criar, listar, consultar, atualizar, ativar e desativar; verificar contratos HTTP e isolamento.
- [x] 2.5 Implementar validacao de producao/motivo ativos, mesma Empresa, intervalo positivo, parada contida na producao e sobreposicao; verificar mensagens de conflito.
- [x] 2.6 Adicionar testes de limites, periodo invalido, producao/motivo ausentes ou inativos, empresa divergente, sobreposicao, adjacencia, update e reativacao; verificar todos os cenarios temporais.

## 3. Tempos produtivos e integracao

- [x] 3.1 Calcular duracao da parada, tempo parado ativo e tempo produtivo derivado, excluindo paradas inativas e protegendo contra valor negativo; verificar cenario 240 - 35 = 205 minutos.
- [x] 3.2 Evoluir response/listagem de producao com duracao bruta, tempo parado e tempo produtivo sem N+1; verificar valores apos desativar e reativar uma parada.
- [x] 3.3 Revalidar alteracao do periodo da producao para impedir que paradas existentes fiquem fora, permitindo fim exatamente no fim da parada; verificar update e conflito.
- [x] 3.4 Adicionar testes de calculos, historico apos inativacao de motivo/recurso, producao inativa e update de producao com paradas; verificar regressao da TASK 03.

## 4. Documentacao, regressao e aceite

- [x] 4.1 Atualizar README com motivos, paradas, endpoints, calculos, fuso operacional e itens fora do escopo; verificar exemplos executaveis.
- [x] 4.2 Executar `./gradlew.bat clean test`, corrigir regressao das TASKS 01-03 e confirmar novos testes; verificar suite Gradle verde.
- [x] 4.3 Executar `./gradlew.bat build` e `openspec validate "paradas-tempos-produtivos" --type change --strict`; verificar build e validacao aprovados.
