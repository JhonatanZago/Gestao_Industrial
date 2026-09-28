# Tasks

## 1. Modelo financeiro e persistência

- [x] 1.1 Criar `TipoValor`, entidade `ValorProducao`, auditoria e relacionamentos com empresa, peça, setor e máquina; verificar compilação sem uso de `double`/`float`.
- [x] 1.2 Criar migration Flyway da tabela `valor_producao` com `DECIMAL(19,6)`, moeda, vigência, FKs, checks e índices; verificar aplicação limpa em H2/PostgreSQL de teste sem editar migrations antigas.
- [x] 1.3 Criar repositório com consultas paginadas por empresa/contexto, tipo, moeda, ativo e período de vigência; verificar isolamento entre empresas e filtros combinados.
- [x] 1.4 Criar testes de entidade/migration para valor positivo, precisão de seis casas, moeda, intervalo válido e referências relacionais; verificar mensagens de integridade esperadas.

## 2. Cadastro de valores

- [x] 2.1 Criar `ValorProducaoRequest`, `ValorProducaoResponse`, mapper, serviço e endpoints CRUD/lifecycle; verificar que a API não expõe entidades JPA.
- [x] 2.2 Implementar validação de valor positivo, moeda ISO normalizada, tipo, pertencimento à empresa e compatibilidade máquina/setor; verificar erros claros para cada referência inválida.
- [x] 2.3 Implementar conflito de vigência por empresa/peça/setor/máquina/tipo/moeda, incluindo intervalos abertos e atualização da própria linha; verificar casos consecutivos e sobrepostos.
- [x] 2.4 Implementar ativação/desativação lógica e consulta histórica; verificar que valores inativos não são candidatos futuros e permanecem consultáveis.
- [x] 2.5 Documentar os endpoints e a distinção entre `VALOR_PROCESSAMENTO`, `VALOR_PRODUCAO`, `RECEITA_ESTIMADA` e faturamento; verificar revisão do contrato no README/API.

## 3. Resolução histórica

- [x] 3.1 Implementar resolver em lote de valores por empresa, período, moeda e tipo, indexando candidatos por peça/setor/máquina; verificar ausência de query por registro.
- [x] 3.2 Implementar data de referência pelo início da produção e precedência máquina específica > fallback geral; verificar troca histórica de 0.80 para 0.85 e fallback de Cabine 02/01.
- [x] 3.3 Integrar a mesma resolução para produções manuais e importadas sem exigir valor na planilha; verificar resultados idênticos para mesmo contexto e data.
- [x] 3.4 Implementar cobertura financeira e preservação da quantidade real quando não houver valor; verificar cenário 8 de 10 registros, 80%, e ausência distinta de zero monetário.
- [x] 3.5 Criar testes de recursos posteriormente inativados, valores desativados e histórico por vigência; verificar que consultas históricas não usam valor atual indevidamente.

## 4. Cálculos e respostas financeiras

- [x] 4.1 Criar DTOs financeiros de resumo, agregação, série e comparativo com moeda, tipo, quantidade, valor e cobertura; verificar contratos específicos sem campos ambíguos.
- [x] 4.2 Implementar valor por registro como quantidade vezes valor unitário usando `BigDecimal` e arredondamento somente na apresentação; verificar 350 × 0.85 = 297.50 e valores com quatro/seis casas.
- [x] 4.3 Implementar agregações por setor, funcionário, peça, máquina e turno; verificar múltiplos registros (300 + 200 unidades = 425.00) e valor do funcionário limitado às próprias produções.
- [x] 4.4 Implementar valor por hora bruta e produtiva, retornando nulo para denominador zero; verificar BRL 500 em 120 minutos = BRL 250/h produtiva.
- [x] 4.5 Implementar separação por moeda e tipo, filtro de moeda/tipo e receita estimada sem chamar o resultado de faturamento; verificar que BRL e USD nunca são somados.
- [x] 4.6 Garantir que não sejam calculados custo, lucro, margem ou perda financeira por parada; verificar respostas e documentação sem esses indicadores.

## 5. APIs analíticas

- [x] 5.1 Implementar resumo financeiro com filtros de período, dimensões, moeda e tipo; verificar grupos separados quando moeda não for informada.
- [x] 5.2 Implementar endpoints financeiros por setores, funcionários, peças, máquinas e turnos; verificar paginação, filtros e campos de quantidade/valor independentes.
- [x] 5.3 Implementar série temporal DIA/SEMANA/MES com cobertura e moeda/tipo; verificar agrupamento temporal e ausência de valor parcial explícita.
- [x] 5.4 Implementar comparativo entre períodos com valor atual/anterior, variação absoluta/percentual e base anterior zero; verificar percentual nulo quando não há base.
- [x] 5.5 Documentar que valor produzido não é faturamento e que processos múltiplos podem representar valor processado, não receita final; verificar exemplos e mensagens da API.

## 6. Integração e verificação

- [x] 6.1 Adicionar testes unitários e de integração cobrindo cadastro, moeda, vigência, fallback, histórico, cobertura, agregações, série, comparativo e N+1; verificar cenários matemáticos obrigatórios.
- [x] 6.2 Executar `./gradlew.bat clean test` e corrigir regressões das TASKS 01–08; verificar suíte completa verde.
- [x] 6.3 Executar `./gradlew.bat build` e revisar migrations, contratos REST e precisão monetária; verificar build concluído sem falhas.
- [x] 6.4 Executar `openspec validate valorizacao-producao --type change --strict`; verificar todos os artefatos e tarefas concluídos.
