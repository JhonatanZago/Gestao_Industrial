# Tasks

## 1. Recursos produtivos

- [ ] 1.1 Criar entidade `Maquina` com Empresa, Setor atual, código, nome, descrição, tipo, ativo e auditoria; verificar FKs obrigatórias e ausência de remoção em cascata.
- [ ] 1.2 Criar migration V8 para `maquina`, unicidade `(empresa_id, codigo)`, índices por setor/status e constraints obrigatórias; verificar aplicação em H2 limpo.
- [ ] 1.3 Criar repository, request/response, mapper, service e endpoints paginados de máquinas com filtros e ciclo de vida; verificar isolamento por Empresa e normalização.
- [ ] 1.4 Adicionar testes de máquina para cadastro, setor de outra empresa, duplicidade, filtro, atualização, desativação/reativação e acesso cruzado; verificar suíte da capability verde.

## 2. Turnos

- [ ] 2.1 Criar entidade `Turno` com Empresa, código, nome, horaInicio, horaFim, ativo e auditoria, sem enum fixo; verificar representação de horários noturnos.
- [ ] 2.2 Criar migration V8 para `turno`, unicidade `(empresa_id, codigo)`, campos obrigatórios e índices; verificar coexistência com a tabela de máquinas e aplicação em H2.
- [ ] 2.3 Criar repository, DTOs, mapper, service e endpoints paginados de turnos com filtros e ciclo de vida; verificar escopo e conflitos.
- [ ] 2.4 Implementar cálculo derivado da duração nominal, aceitando passagem pela meia-noite e rejeitando início igual ao fim; verificar 06:00-14:00 e 22:00-06:00 como 480 minutos.
- [ ] 2.5 Adicionar testes de turno diurno/noturno, duração, igualdade de horários, duplicidade, empresas diferentes, filtros e inativação; verificar suite da capability verde.

## 3. Persistência das alocações

- [ ] 3.1 Criar migration V9 com colunas opcionais `maquina_id` e `turno_id` em `registro_producao` e `maquina_id` em `parada_producao`, FKs e índices por recurso/turno/período; verificar compatibilidade com dados nulos e ausência de cascade remove.
- [ ] 3.2 Evoluir entidades, requests, responses e mappers de produção para os vínculos opcionais e blocos resumidos de máquina/turno; verificar que payloads antigos continuam válidos.
- [ ] 3.3 Evoluir entidades, requests, responses e mappers de parada para máquina opcional; verificar preservação de duração, tempo parado e histórico.

## 4. Regras de alocação e APIs integradas

- [ ] 4.1 Integrar no service de produção a validação de máquina/turno da mesma Empresa, ativos e máquina no mesmo Setor; verificar rejeição de inexistente, outra empresa, inativo e setor incompatível.
- [ ] 4.2 Integrar no service de parada a validação opcional de máquina consistente com a produção; verificar parada sem máquina, máquina igual, máquina divergente, inativa ou de outra Empresa.
- [ ] 4.3 Preservar produção atravessando troca de turno sem divisão e sem inferência automática; verificar turno explicitamente informado e associação nula quando omitida.
- [ ] 4.4 Adicionar testes de integração de produção com/sem máquina e turno, atualização/remoção opcional, turno noturno e produção atravessando troca de turno; verificar respostas e compatibilidade dos contratos.
- [ ] 4.5 Adicionar testes de integração de parada com/sem máquina, cálculo de tempo parado e regras de sobreposição; verificar regressão da capability `paradas-tempos-produtivos`.

## 5. Documentação e aceite

- [ ] 5.1 Atualizar README com recursos produtivos, turnos, endpoints, duração noturna, associações opcionais e itens fora do escopo; verificar exemplos coerentes com a API.
- [ ] 5.2 Executar `./gradlew.bat clean test`, corrigir regressões das TASKS 01-04 e confirmar todos os testes novos; verificar suíte Gradle verde.
- [ ] 5.3 Executar `./gradlew.bat build` e `openspec validate "maquinas-e-turnos" --type change --strict`; verificar build e validação aprovados.
