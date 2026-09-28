# TASK EXECUTÁVEL 14 --- API Consolidada do Dashboard e Visão Executiva

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**, somente após concluir e estabilizar as TASKS 01--13.

Antes de alterar código: leia projeto e OpenSpec completos, rode todos
os testes, confirme build estável, mapeie Services/endpoints analíticos,
preserve segurança/auditoria/qualidade, crie a change OpenSpec,
implemente composição sem duplicar regras e execute regressão.

**Não implementar frontend. Não criar gráficos no backend. Não duplicar
fórmulas. Não implementar IA/RAG. Não iniciar a próxima task
automaticamente.**

## 1. Objetivo

Criar a API consolidada que alimentará o futuro Dashboard:

``` text
Dashboard
  ↓
API consolidada
  ├── resumo executivo
  ├── produção/comparativos
  ├── série temporal
  ├── setores
  ├── peças
  ├── máquinas
  ├── funcionários
  ├── paradas
  ├── metas/desempenho
  ├── valor industrial
  └── qualidade dos dados
```

## 2. OpenSpec

Criar change:

``` text
api-dashboard-executivo
```

Capabilities:

``` text
dashboard-executivo
dashboard-producao
dashboard-drilldown
```

Documentar composição, reutilização de Services, filtros, períodos,
comparativos, séries, permissões, escopo setorial, campos condicionais,
performance, contratos e versionamento.

## 3. Arquitetura

A API é uma camada de composição:

``` text
Repositories
→ Services de domínio/Analytics
→ DashboardFacade
→ DTOs
→ REST
```

`DashboardFacade` coordena; não recalcula regras já implementadas.

Para filtros idênticos, Analytics, Relatório Executivo e Dashboard devem
produzir os mesmos valores-base.

## 4. Endpoint principal

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/dashboard
```

Filtros:

``` text
inicio
fim
inicioAnterior?
fimAnterior?
setorId?
funcionarioId?
pecaId?
maquinaId?
turnoId?
granularidade?
topPecas?
topFuncionarios?
topMotivosParada?
```

Response composto equivalente a:

``` text
DashboardExecutivoResponse
├── contexto
├── resumo
├── comparativo
├── serieTemporal
├── setores
├── pecas
├── maquinas
├── funcionarios
├── paradas
├── metas
├── desempenho
├── financeiroIndustrial?
└── qualidadeDados
```

Usar DTOs explícitos e compostos; evitar `Map<String,Object>` e DTO
monolítico.

## 5. Contexto e períodos

Retornar:

``` text
empresaId
empresaNome
inicio
fim
inicioComparacao
fimComparacao
setorSelecionado?
geradoEm
```

Se comparação automática, usar período imediatamente anterior de mesma
duração e informar as datas efetivamente utilizadas.

## 6. Cards

Preparar:

``` text
quantidadeProduzida
tempoProdutivoMinutos
tempoParadoMinutos
percentualTempoParado
taxaProducaoProdutiva
atingimentoMeta?
indiceDesempenho?
valorIndustrial?
```

Para comparáveis:

``` text
atual
anterior
variacaoAbsoluta
variacaoPercentual
```

Tratar base anterior zero.

Tendência matemática:

``` text
AUMENTO
REDUCAO
ESTAVEL
SEM_BASE_COMPARACAO
```

Não classificar genericamente aumento como "bom" ou "ruim".

## 7. Série temporal

Preparar dados para gráficos:

``` text
periodo
quantidade
tempoProdutivo
tempoParado
valorIndustrial?
```

Granularidade:

``` text
DIA
SEMANA
MES
```

Preencher lacunas com zero somente quando zero for semanticamente
correto. Nunca confundir zero com dado indisponível.

## 8. Setores

Retornar setores autorizados:

``` text
setorId
codigo
nome
quantidade
tempoProdutivo
tempoParado
percentualParada
taxaProducao
atingimentoMeta?
indiceDesempenho?
valorIndustrial?
qualidadeDados
```

Ordenação controlada por métricas autorizadas, nunca campo SQL
arbitrário.

## 9. Peças

Top N:

``` text
pecaId
codigo
descricao
quantidade
tempoProdutivo
indiceDesempenho?
valorIndustrial?
```

Definir defaults/máximos seguros.

## 10. Máquinas

Retornar:

``` text
maquinaId
codigo
nome
setor
quantidade
tempoProdutivo
tempoParado
percentualParada
desempenho?
valorIndustrial?
```

## 11. Funcionários

Quando autorizado:

``` text
funcionarioId
codigo
nome
quantidade
tempoProdutivo
tempoParado
indiceDesempenho
coberturaTempoPadrao
```

Não criar ranking moral de melhor/pior funcionário.

### Drill-down

``` http
GET /api/v1/empresas/{empresaId}/dashboard/funcionarios/{funcionarioId}
```

Retornar:

``` text
identificacao
resumo
pecasProduzidas
serieTemporal
tempoProdutivo
tempoParado
paradas
desempenho
meta?
valorIndustrial? (somente autorizado)
qualidadeDados
```

Isso deve atender ao requisito de o proprietário clicar no funcionário e
analisar suas produções e tempos.

## 12. Drill-down de setor

``` http
GET /api/v1/empresas/{empresaId}/dashboard/setores/{setorId}
```

Retornar resumo, comparativo, série, funcionários, peças, máquinas,
paradas, metas, desempenho, financeiro autorizado e qualidade.

## 13. Drill-down de peça

``` http
GET /api/v1/empresas/{empresaId}/dashboard/pecas/{pecaId}
```

Retornar resumo, setores, funcionários, máquinas, série, tempos,
desempenho, financeiro autorizado e qualidade.

## 14. Drill-down de máquina

``` http
GET /api/v1/empresas/{empresaId}/dashboard/maquinas/{maquinaId}
```

Retornar resumo, série, peças, funcionários, paradas, desempenho,
financeiro autorizado e qualidade.

## 15. Paradas

Dashboard principal:

``` text
tempoParadoTotal
percentualTempoParado
quantidadeOcorrencias
topMotivos
```

Motivo:

``` text
motivoId
codigo
nome
categoria
ocorrencias
tempoMinutos
percentual
```

## 16. Metas e desempenho

Metas:

``` text
metaId
descricao
tipo
valorMeta
valorReal
atingimentoPercentual
periodo
```

Não criar meta quando não houver configuração.

Desempenho:

``` text
quantidadeReal
quantidadeEsperada
indiceDesempenho
registrosComPadrao
registrosSemPadrao
coberturaPadraoPercentual
```

## 17. Financeiro industrial

Somente com:

``` text
FINANCEIRO_INDUSTRIAL_VISUALIZAR
```

Retornar:

``` text
moeda
valorIndustrial
valorPorHoraBruta
valorPorHoraProdutiva
comparativo
cobertura
```

Nunca chamar automaticamente de faturamento, lucro ou receita real.

Não somar moedas diferentes. Agrupar por moeda ou exigir filtro.

## 18. Qualidade dos dados

Integrar TASK 13:

``` text
coberturaTempoPadrao
coberturaValor
quantidadeAlertas
quantidadeCriticos
possiveisDuplicidades
statusConfiabilidade?
```

Não esconder valores-base atrás de score.

## 19. Disponibilidade

Quando métrica não puder ser calculada, não retornar `0` como se fosse
valor real. Padronizar, quando útil:

``` text
DISPONIVEL
PARCIAL
INDISPONIVEL
```

com `null` para valor realmente indisponível e cobertura explícita.

## 20. Segurança

Todos os endpoints autenticados.

Respeitar:

``` text
ANALYTICS_VISUALIZAR
FUNCIONARIO_VISUALIZAR
FINANCEIRO_INDUSTRIAL_VISUALIZAR
QUALIDADE_DADOS_VISUALIZAR
```

e demais permissões reais.

Usuário Empresa A nunca acessa Empresa B.

Gestor Pintura/KTL recebe TODAS as agregações apenas desses setores,
inclusive resumo, séries, funcionários, peças, máquinas, paradas, metas,
financeiro e qualidade.

Drill-down de setor proibido deve retornar `403`.

Não permitir bypass por ID de funcionário/peça/máquina.

## 21. Contrato para Angular

DTOs previsíveis, enums estáveis, datas/BigDecimal/nullability
documentados.

Documentar OpenAPI com campos, filtros, segurança e enums.

Manter `/api/v1` e não quebrar endpoints anteriores.

## 22. Performance

O objetivo é reduzir round trips sem criar um endpoint que execute
centenas de queries.

Obrigatório revisar N+1.

Preferir:

``` text
projections
queries agregadas
DTO queries
```

Não carregar milhares de entidades completas para agregar em Java.

Definir Top N e máximos seguros.

`/dashboard` é resumo; detalhes ficam nos drill-downs.

Não adicionar Redis/cache distribuído nesta task. Não usar paralelização
indiscriminada para mascarar queries ruins.

## 23. Cenário determinístico principal

Atual:

``` text
10.000 peças
600 min brutos
60 min parada
540 min produtivos
Meta = 11.000
Valor BRL = 8.500
```

Anterior:

``` text
8.000 peças
80 min parada
Valor BRL = 7.000
```

Esperado:

``` text
produção = +25%
parada = -25%
atingimento meta ≈ 90,91%
valor industrial ≈ +21,43%
percentual parada atual = 10%
```

Valores devem coincidir com Analytics/Relatórios.

## 24. Cenário funcionário

Código `00147`:

``` text
Peça A = 300
Peça B = 200
bruto = 300 min
parada = 30 min
produtivo = 270 min
```

Drill-down:

``` text
500 peças
270 min produtivos
30 min parada
peças produzidas
série temporal
desempenho contextualizado
```

## 25. Cenário financeiro

Gestor com analytics sem financeiro:

``` text
produção → presente
paradas → presente
metas → presente
financeiroIndustrial → ausente
```

Nenhum valor financeiro pode vazar por outra seção.

## 26. Cenário qualidade

``` text
100 registros
80 com tempo padrão
90 com valor
```

Esperado:

``` text
coberturaTempoPadrao = 80%
coberturaValor = 90%
```

## 27. Cenário setor restrito

Empresa:

``` text
Pintura = 5.000
Laser = 7.000
```

Gestor somente Pintura:

``` text
Dashboard total = 5.000
```

Nunca 12.000 com Laser apenas escondido visualmente.

## 28. Testes obrigatórios

Testar: 1. Dashboard principal; 2. período/comparativo; 3.
série/granularidade; 4. cards; 5. setores; 6. peças; 7. máquinas; 8.
funcionários; 9. paradas; 10. metas; 11. desempenho; 12. financeiro; 13.
múltiplas moedas; 14. qualidade; 15. indisponibilidade; 16. drill-down
funcionário; 17. drill-down setor; 18. drill-down peça; 19. drill-down
máquina; 20. isolamento empresarial; 21. escopo setorial; 22. financeiro
proibido; 23. consistência com Analytics; 24. contrato JSON; 25.
N+1/performance básica; 26. volume sintético razoável; 27. regressão.

## 29. Integrações

Dashboard e Relatório Executivo devem compartilhar a mesma fonte de
verdade.

GETs do Dashboard não geram EventoAuditoria de negócio.

A API consolidada deve ficar preparada como fonte estruturada da futura
IA/RAG, mas sem implementar IA agora.

## 30. Fora do escopo

NÃO implementar:

``` text
Angular
HTML/CSS
gráficos visuais
WebSocket
tempo real
IA/RAG
Redis
notificações
PDF
novos conceitos financeiros
faturamento fiscal
lucro
```

## 31. Regressão e build

Todos os testes das TASKS 01--13 devem continuar passando.

Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar também a validação oficial OpenSpec disponível.

## 32. Proibições

É proibido: - duplicar fórmulas; - divergir do Analytics; - somar
moedas; - transformar indisponível em zero; - vazar
financeiro/setor/empresa; - retornar milhares de itens no resumo; -
criar N+1 evidente; - desabilitar segurança/testes; - editar migrations
antigas sem necessidade; - concluir com build falhando.

## 33. Critérios de aceite

Concluir somente quando: 1. `/dashboard` funcionar; 2. períodos forem
explícitos; 3. cards/comparativos funcionarem; 4. série/granularidade
funcionarem; 5. setores/peças/máquinas funcionarem; 6. funcionários
respeitarem permissão; 7. paradas/metas funcionarem; 8. desempenho
mostrar cobertura; 9. financeiro respeitar permissão/moeda; 10.
qualidade estiver integrada; 11. indisponível não virar zero; 12. quatro
drill-downs funcionarem; 13. isolamento empresarial funcionar; 14.
escopo setorial atingir TODAS as agregações; 15. OpenAPI estiver
documentada; 16. consistência com Analytics estiver testada; 17. N+1
evidente for evitado; 18. regressão passar; 19. `clean test` passar; 20.
`build` passar; 21. OpenSpec refletir a implementação.

## 34. Entrega esperada do Codex

Ao concluir, apresentar: -
change/proposal/design/tasks/specs/validação; - arquitetura
`Analytics → DashboardFacade → DTOs → Resources`; -
endpoints/filtros/permissões; - estrutura dos principais DTOs; -
segurança de empresa/setor/funcionário/financeiro/qualidade; -
estratégia de queries/projections/N+1/Top N; - testes matemáticos,
contrato, segurança, volume e regressão; - resultados de `clean test` e
`build`; - pendências reais.

Não iniciar a próxima task.

## 35. Próxima etapa prevista

Após aprovação:

``` text
TASK 15 — IA/RAG e Assistente Industrial Seguro
```

Ela deverá permitir perguntas em linguagem natural sobre os dados reais
do ERP:

``` text
Quanto a Pintura produziu esta semana?
Qual setor teve maior tempo de parada?
Como setembro ficou comparado a agosto?
Quais peças o funcionário 00147 produziu?
Qual foi o valor industrial da Pintura?
Os dados desse período são confiáveis?
```

A IA deverá respeitar usuário, empresa, perfil, permissões, setores,
financeiro e qualidade dos dados, sem inventar números.

**O frontend continuará fora do escopo nesta task.**
