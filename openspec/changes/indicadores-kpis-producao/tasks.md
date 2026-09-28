# Tasks

## 1. Contratos analíticos e núcleo de métricas

- [x] 1.1 Criar enums/requests para granularidade temporal, período obrigatório e filtros analíticos; verificar parsing de ISO `LocalDateTime` e rejeição de campos ausentes.
- [x] 1.2 Criar DTOs próprios para resumo, métricas, dimensões, motivos, série temporal e comparativo; verificar payloads sem entidades JPA ou registros brutos ilimitados.
- [x] 1.3 Implementar validador de Empresa, IDs de dimensões e períodos inclusivos; verificar erro seguro para período inválido e referência de outra Empresa.
- [x] 1.4 Implementar núcleo de fórmulas com `BigDecimal`, minutos decimais, escala 2 e HALF_UP; verificar taxas/percentuais e `null` quando o denominador é zero.
- [x] 1.5 Adicionar testes unitários do cenário determinístico de 500 peças/240 minutos/30 parados; verificar 125,00 peças/h, 142,86 peças/h produtivas e 12,50%.

## 2. Consultas e agregações de produção

- [x] 2.1 Criar camada de consulta analítica com projections agregadas por Empresa, período, ativo e filtros; verificar que a consulta não carrega toda a base em memória.
- [x] 2.2 Implementar soma de durações de produção e paradas ativas em consultas em lote sem join multiplicador; verificar que múltiplas paradas não duplicam a duração bruta.
- [x] 2.3 Implementar resumo geral e agregação por Setor; verificar quantidades, registros, tempos e taxas por grupo e total.
- [x] 2.4 Implementar agregações paginadas por Funcionário, Peça, Máquina e Turno; verificar ordenação determinística e grupos `SEM_MAQUINA`/`SEM_TURNO` sem criar entidades.
- [x] 2.5 Implementar agregação por Motivo de Parada com ocorrências, categoria, duração e percentual do tempo parado; verificar exclusão de paradas inativas.
- [x] 2.6 Implementar detalhe analítico de Funcionário com peças e setores agrupados; verificar que a resposta é limitada/paginada e respeita o escopo da Empresa.
- [x] 2.7 Adicionar testes de integração com dois setores, funcionários, peças, máquinas, turnos e empresas; verificar isolamento, soma dos grupos e filtros combináveis.

## 3. Séries temporais e comparativos

- [x] 3.1 Implementar buckets `DIA`, `SEMANA` e `MES` com ordenação cronológica; verificar que períodos sem produção são omitidos e que a granularidade inválida é rejeitada.
- [x] 3.2 Implementar comparação entre período atual e anterior reutilizando o mesmo caso de uso de métricas; verificar valor atual, anterior, diferença absoluta e percentual.
- [x] 3.3 Tratar denominador anterior zero no comparativo; verificar que a diferença é preservada e a variação percentual retorna `null`.
- [x] 3.4 Testar limites inclusivos e registros que atravessam o período; verificar inclusão baseada no início e ausência de rateio proporcional de quantidade/duração.
- [x] 3.5 Testar produções importadas e manuais com dados equivalentes; verificar que `OrigemProducao` não altera a matemática analítica.
- [x] 3.6 Testar produção ativa com funcionário, peça, setor ou máquina posteriormente inativados; verificar preservação no histórico e exclusão de produção inativa.

## 4. APIs e observabilidade

- [x] 4.1 Criar resource de `/analytics/resumo` com filtros de Empresa, período e dimensões; verificar contrato MockMvc e métricas calculadas.
- [x] 4.2 Criar resources paginados de setores, funcionários, peças, máquinas, turnos e `/paradas/motivos`; verificar limites, ordenação e isolamento.
- [x] 4.3 Criar endpoints de detalhe de Funcionário, série temporal e comparativo; verificar parâmetros, erros seguros e payloads previsíveis.
- [x] 4.4 Integrar `GlobalExceptionHandler` para período, granularidade e contexto inválidos; verificar ausência de stack trace na resposta.
- [x] 4.5 Adicionar logs técnicos de consultas analíticas lentas com Empresa, filtros resumidos, endpoint e duração; verificar que códigos e payloads grandes não são registrados.

## 5. Performance, documentação e aceite

- [x] 5.1 Revisar índices existentes com volume sintético e plano de consulta; verificar se uma migration V12 é necessária antes de criá-la.
- [x] 5.2 Se justificado pelos planos, criar migration de índices analíticos sem alterar V1–V11; verificar aplicação em H2 limpo e compatibilidade com PostgreSQL.
- [x] 5.3 Adicionar avaliação controlada de ausência de N+1 e paginação/limites; verificar número fixo de consultas por endpoint agregado.
- [x] 5.4 Documentar endpoints, filtros, fórmulas, unidades, timezone, arredondamento, regra de período, grupos nulos e escopo fora do change; verificar exemplos executáveis.
- [x] 5.5 Executar `./gradlew.bat clean test`, corrigir regressões das TASKS 01–06 e verificar todos os cenários analíticos determinísticos.
- [x] 5.6 Executar `./gradlew.bat build` e `openspec validate "indicadores-kpis-producao" --type change --strict`; verificar build e validação aprovados.
