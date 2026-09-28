# Tasks

## 1. Modelo e persistência

- [x] 1.1 Criar enums, entidades `TempoPadraoProducao` e `MetaProducao`, relacionamentos, auditoria e regras básicas de domínio; verificar compilação do módulo de domínio.
- [x] 1.2 Criar migração Flyway do tempo padrão com chaves, vigência, ativo e índices por empresa/contexto; verificar aplicação limpa da migração em banco de teste.
- [x] 1.3 Criar migração Flyway da meta com tipos, periodicidades, vigência, ativo e índices; verificar aplicação limpa e ausência de alterações nas migrações anteriores.
- [x] 1.4 Criar repositórios e consultas paginadas por empresa, contexto, ativo e vigência; verificar testes de isolamento e filtros.

## 2. Tempos padrão

- [x] 2.1 Criar DTOs, mapeadores, serviço e endpoints CRUD de tempos padrão; verificar contrato HTTP sem exposição de entidades JPA.
- [x] 2.2 Implementar validações de referências, segundos, base por unidade/lote e quantidade de lote; verificar testes para entradas válidas e inválidas.
- [x] 2.3 Implementar validação de sobreposição, atualização e ativação/desativação lógica; verificar testes de intervalos abertos, consecutivos, sobrepostos e máquina nula.
- [x] 2.4 Implementar resolução por data de início, precedência de máquina e fallback geral do setor; verificar testes de fallback e troca histórica de 20 para 18 segundos.
- [x] 2.5 Documentar e testar a preservação do histórico após desativação e o isolamento por empresa; verificar que padrões inativos não são candidatos futuros.

## 3. Metas de produção

- [x] 3.1 Criar DTOs, mapeadores, serviço e endpoints CRUD de metas; verificar respostas paginadas e ciclo de vida lógico.
- [x] 3.2 Implementar validação de tipos, contextos, quantidade positiva e periodicidades turno/dia/semana/mês; verificar testes de combinações aceitas e rejeitadas.
- [x] 3.3 Implementar conflitos de vigência por contexto e periodicidade, seleção de meta aplicável e desativação histórica; verificar testes de sobreposição e isolamento.
- [x] 3.4 Implementar regra de meta mensal sem rateio em período parcial e expor periodicidade/vigência no resultado; verificar teste de consulta parcial.

## 4. Métricas de desempenho

- [x] 4.1 Criar o serviço de resolução em lote e enriquecer registros com padrão, tempo produtivo, origem do fallback e cobertura; verificar ausência de consulta por registro em teste de integração.
- [x] 4.2 Implementar quantidade esperada por unidade/lote, desempenho e arredondamento de saída; verificar cenários de 180 esperadas/100% e 198/110%.
- [x] 4.3 Implementar cobertura parcial e ausência de denominador sem zerar ou ocultar a produção real; verificar cenário 8 de 10 registros com padrão e indicadores nulos.
- [x] 4.4 Implementar atingimento de metas e situação de meta indisponível; verificar cenários 18500/20000 = 92,50% e 22000/20000 = 110%.
- [x] 4.5 Adicionar DTOs de desempenho e integrar os filtros e agregações existentes sem alterar respostas base; verificar regressão dos testes atuais de analytics.

## 5. Endpoints comparativos

- [x] 5.1 Implementar endpoint de desempenho geral e endpoint de metas reutilizando o contexto analítico existente; verificar filtros de empresa, período e granularidade.
- [x] 5.2 Implementar comparativos por funcionários, setores, máquinas e peças; verificar agregação por dimensão com padrões distintos por registro.
- [x] 5.3 Expor nos comparativos quantidade real, esperada, desempenho, meta, atingimento e cobertura quando aplicáveis; verificar divisão por zero e dimensões sem padrão.
- [x] 5.4 Garantir que respostas não emitam ranking subjetivo e que ordenação opcional seja apenas por métrica; verificar contrato e testes de ordenação.
- [x] 5.5 Documentar os endpoints, filtros, campos de cobertura e semântica histórica; verificar que exemplos não prometem OEE, qualidade ou ranking.

## 6. Integração e verificação

- [x] 6.1 Executar a suíte completa de testes unitários e de integração e corrigir regressões; verificar todos os testes verdes.
- [x] 6.2 Executar build limpo e validar migrações, endpoints e contratos em ambiente de teste; verificar comando de build concluído sem erro.
- [x] 6.3 Executar `openspec validate metas-tempos-padrao-desempenho --type change --strict` e revisar todos os artefatos; verificar validação estrita aprovada.
