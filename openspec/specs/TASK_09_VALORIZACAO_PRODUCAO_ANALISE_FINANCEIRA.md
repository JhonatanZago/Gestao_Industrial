# TASK EXECUTÁVEL 09 --- Valorização da Produção, Receita Estimada e Análise Financeira Industrial

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--08.
Antes de alterar código, leia projeto/specs, execute todos os testes,
confirme build estável, preserve contratos, crie a change OpenSpec,
implemente, valide matematicamente e execute regressão completa.

**Não implementar frontend. Não implementar contabilidade/fiscal. Não
chamar valor produzido de faturamento contábil real. Não iniciar a
próxima task automaticamente.**

## 1. Objetivo

Adicionar uma camada financeira industrial sobre a produção física para
responder: - Qual foi o valor estimado da produção? - Qual
peça/setor/máquina/turno gerou maior valor industrial? - Qual valor
industrial está associado às produções de cada funcionário? - Como
quantidade e valor produzido evoluíram e se comparam entre períodos?

## 2. Separação obrigatória de conceitos

Separar rigorosamente:

``` text
VALOR INDUSTRIAL PRODUZIDO
≠ RECEITA ESTIMADA
≠ FATURAMENTO CONTÁBIL/FISCAL REAL
```

O sistema ainda não possui necessariamente NF-e, pedidos faturados,
impostos, descontos, devoluções, recebimentos ou contas a receber.
Portanto, nunca afirmar que produção física foi faturada.

## 3. OpenSpec

Criar change `valorizacao-producao`, com proposal, design, tasks e
specs/capabilities equivalentes a:

``` text
valor-producao
historico-valores
analytics-financeiro-industrial
```

Documentar conceitos, fórmulas, moeda, vigência, fallback, cobertura e
limitações.

## 4. Valor de referência

Criar entidade equivalente a `ValorProducao`:

``` text
id
empresa
peca
setor
maquina?
tipoValor
valorUnitario
moeda
vigenciaInicio
vigenciaFim
ativo
createdAt
updatedAt
```

O contexto mínimo é empresa + peça + setor. Máquina é opcional.

Exemplo:

``` text
Peça 54871
Estamparia = R$ 1,20/unidade
Pintura    = R$ 0,85/unidade
KTL        = R$ 0,45/unidade
```

## 5. Tipo de valor

Se necessário, utilizar enum técnico estável:

``` text
VALOR_PROCESSAMENTO
VALOR_PRODUCAO
RECEITA_ESTIMADA
```

Não utilizar nomenclatura que implique faturamento fiscal real.
Simplificar se apenas um conceito for realmente necessário.

## 6. Precisão monetária

MUST utilizar `BigDecimal`. É proibido usar `double`/`float` para
dinheiro.

No banco, utilizar precisão adequada, por exemplo `DECIMAL(19,6)` ou
equivalente coerente. Valores unitários podem ter mais de duas casas,
como `0,0085`.

Não arredondar cálculos intermediários cedo demais.

## 7. Moeda

Usar código ISO (`BRL`, `USD`, `EUR`). BRL pode ser padrão inicial da
empresa, mas não deve ser hardcoded de modo irreversível.

Não implementar câmbio. Nunca somar moedas diferentes sem conversão.
Agrupar por moeda ou exigir filtro.

## 8. Vigência histórica

Implementar `vigenciaInicio` e `vigenciaFim` (fim opcional).

Exemplo:

``` text
01/01/2026 → 30/06/2026 = R$ 0,80
01/07/2026 → atual      = R$ 0,85
```

Produção histórica usa o valor vigente na data de referência da
produção, preferencialmente o `inicio` do RegistroProducao.

Não permitir vigências ativas sobrepostas para o mesmo contexto:

``` text
empresa + peça + setor + máquina + tipoValor + moeda
```

## 9. Fallback de máquina

Resolver:

``` text
1. valor específico peça + setor + máquina
2. fallback peça + setor sem máquina
3. sem valor => indicador financeiro indisponível para o registro
```

Nunca inventar zero.

## 10. Fórmula do valor industrial

``` text
valorProduzido = quantidade * valorUnitarioAplicavel
```

Exemplo:

``` text
350 × R$ 0,85 = R$ 297,50
```

## 11. Processos múltiplos e dupla contagem

Uma peça pode passar por Estamparia, Solda, Pintura etc. Somar valores
de processamento pode representar valor industrial processado, mas NÃO
deve ser apresentado automaticamente como receita da peça final.

DTOs/endpoints devem deixar explícita a semântica.

## 12. Cobertura financeira

Retornar:

``` text
registrosTotais
registrosComValor
registrosSemValor
quantidadeComValor
valorTotalCalculado
coberturaValorPercentual
```

Ausência de valor não é R\$ 0,00. Totais parciais devem ser
identificados como parciais.

## 13. Agregações

Implementar valor por:

``` text
setor
funcionario
peca
maquina
turno
```

O valor associado ao funcionário representa somente o valor industrial
das produções registradas sob seu código; não representa lucro, salário
ou "valor do trabalhador".

## 14. Valor por hora

Calcular separadamente:

``` text
valorPorHoraBruta =
valorProduzido / (duracaoBrutaMinutos / 60)

valorPorHoraProdutiva =
valorProduzido / (tempoProdutivoMinutos / 60)
```

Tratar divisão por zero e manter moeda explícita.

## 15. Não calcular perda financeira por parada

Não implementar automaticamente "dinheiro perdido por parada". Isso
exige modelo econômico validado e pode gerar interpretações incorretas.

## 16. Receita, custo e lucro

Somente retornar `receitaEstimada` se houver valor explicitamente
cadastrado com essa semântica.

Não assumir:

``` text
valor produzido = custo
valor produzido = receita real
valor produzido = lucro
```

Custos de matéria-prima, energia, mão de obra, manutenção, consumíveis e
depreciação ainda não estão completamente modelados. Não calcular
margem/lucro nesta task.

## 17. API de valores

Base:

``` http
/api/v1/empresas/{empresaId}/valores-producao
```

Implementar:

``` http
POST
GET
GET /{id}
PUT /{id}
PATCH /{id}/ativar
PATCH /{id}/desativar
```

Filtros paginados:

``` text
pecaId
setorId
maquinaId
tipoValor
moeda
ativo
vigenteEm
```

## 18. APIs analíticas financeiras

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/analytics/financeiro/resumo
GET /api/v1/empresas/{empresaId}/analytics/financeiro/setores
GET /api/v1/empresas/{empresaId}/analytics/financeiro/funcionarios
GET /api/v1/empresas/{empresaId}/analytics/financeiro/pecas
GET /api/v1/empresas/{empresaId}/analytics/financeiro/maquinas
GET /api/v1/empresas/{empresaId}/analytics/financeiro/turnos
GET /api/v1/empresas/{empresaId}/analytics/financeiro/serie-temporal
GET /api/v1/empresas/{empresaId}/analytics/financeiro/comparativo
```

Filtros comuns:

``` text
inicio
fim
setorId?
funcionarioId?
pecaId?
maquinaId?
turnoId?
moeda?
```

Série temporal: `DIA`, `SEMANA`, `MES`.

Comparativo deve retornar valor atual/anterior, variação absoluta e
percentual, tratando base anterior zero.

## 19. Quantidade versus valor

Preparar respostas para comparações como:

``` text
Quantidade: +12%
Valor produzido: +4%
```

Produzir mais unidades não implica necessariamente maior valor
industrial.

## 20. DTOs

Criar DTOs específicos, sem expor JPA:

``` text
ValorProducaoRequest
ValorProducaoResponse
ResumoFinanceiroIndustrialResponse
ValorPorSetorResponse
ValorPorFuncionarioResponse
ValorPorPecaResponse
ValorPorMaquinaResponse
ValorPorTurnoResponse
SerieFinanceiraResponse
ComparativoFinanceiroResponse
```

## 21. Migration

Criar nova migration Flyway, sem editar migrations antigas.

Tabela equivalente a `valor_producao` com PK, FKs, BigDecimal/decimal,
moeda, vigência, status, auditoria, constraints e índices.

## 22. Integração com tasks anteriores

Reutilizar períodos, filtros e agregações da TASK 07 e contexto
histórico da TASK 08.

Produção importada pela TASK 06 e produção manual devem seguir
exatamente a mesma valorização.

Não exigir valor financeiro dentro da planilha de produção.

## 23. Performance

Evitar N+1 na resolução histórica dos valores. Não executar uma query de
valor para cada RegistroProducao.

Utilizar estratégia em lote/query adequada e documentá-la no design.

## 24. Cenários matemáticos obrigatórios

### Básico

``` text
Peça: 54871
Setor: Pintura
Valor: R$ 0,85
Quantidade: 350
Esperado: R$ 297,50
```

### Múltiplos registros

``` text
300 × 0,85 = 255,00
200 × 0,85 = 170,00
Total = 500 unidades / R$ 425,00
```

### Vigência

``` text
até 30/06: R$ 0,80
a partir de 01/07: R$ 0,85

100 unidades em maio = R$ 80,00
100 unidades em setembro = R$ 85,00
Total histórico = R$ 165,00
```

### Máquina específica

``` text
Peça X + Pintura = R$ 0,80
Peça X + Pintura + Cabine 02 = R$ 0,90
```

Cabine 02 usa 0,90; Cabine 01 usa fallback 0,80.

### Cobertura parcial

``` text
10 registros
8 com valor
2 sem valor
cobertura = 80%
```

### Valor/hora

``` text
Valor produzido = R$ 500
Tempo produtivo = 120 min
Esperado = R$ 250/h produtiva
```

## 25. Testes obrigatórios

Testar: 1. cadastro; 2. BigDecimal; 3. valores com várias casas
decimais; 4. zero/negativo; 5. moeda; 6. vigência; 7. sobreposição; 8.
isolamento por empresa; 9. peça/setor/máquina; 10. fallback; 11.
histórico; 12. desativação; 13. cálculo por registro; 14. agregações;
15. cobertura parcial; 16. valor/hora; 17. filtros; 18. comparativo; 19.
série temporal; 20. moedas diferentes; 21. produção manual/importada;
22. recursos posteriormente inativados; 23. prevenção de N+1.

## 26. Tratamento de erros

Reutilizar `@RestControllerAdvice`.

Mensagens claras:

``` text
O valor unitário deve ser maior que zero.
A moeda informada é inválida.
Já existe um valor de produção com vigência sobreposta para este contexto.
A peça informada não pertence à empresa.
O setor informado não pertence à empresa.
A máquina informada não pertence à empresa.
```

## 27. Fora do escopo

NÃO implementar: - frontend/Angular; - Dashboard visual; -
NF-e/fiscal; - contas a receber; - fluxo de caixa; - cobrança; - pedidos
de venda; - impostos; - descontos; - custo completo; - lucro/margem; -
câmbio; - dinheiro perdido por parada; - contabilidade; - integração ERP
externo; - autenticação/autorização; - IA/RAG.

## 28. Regressão e build

Todos os testes das TASKS 01--08 devem continuar passando.

Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar também a validação oficial OpenSpec disponível na instalação
atual.

É proibido mascarar falhas, desabilitar testes, usar `double` para
dinheiro, inventar valor ausente, somar moedas diferentes, aplicar valor
atual ao histórico, editar migrations antigas ou concluir com build
falhando.

## 29. Critérios de aceite

Concluir somente quando: 1. valores puderem ser cadastrados; 2.
BigDecimal for utilizado; 3. contexto peça + setor funcionar; 4. máquina
específica/fallback funcionarem; 5. vigência histórica funcionar; 6.
sobreposição for rejeitada; 7. valor por registro/agregado estiver
correto; 8. agregações por setor/funcionário/peça/máquina/turno
funcionarem; 9. valor/hora funcionar; 10. série temporal/comparativo
funcionarem; 11. cobertura parcial for explícita; 12. moedas não forem
somadas incorretamente; 13. produção manual/importada usar mesma regra;
14. N+1 evidente for evitado; 15. testes financeiros e regressão
passarem; 16. `clean test` e `build` passarem; 17. OpenSpec refletir
fielmente a implementação.

## 30. Entrega esperada do Codex

Ao concluir, informar: - change/proposal/design/tasks/specs; - arquivos
alterados; - domínio e regras de vigência/fallback/moeda; - migrations,
precisão, FKs, constraints e índices; - endpoints/filtros; - fórmulas; -
estratégia contra N+1; - testes e regressão; - resultados de
`clean test` e `build`; - pendências reais.

Não iniciar a próxima task.

## 31. Próxima etapa prevista

Após aprovação:

``` text
TASK 10 — Usuários, Autenticação, Perfis e Permissões
```

Ela deverá separar acessos como:

``` text
DONO / ADMINISTRADOR
├── visão completa
├── financeiro industrial
├── comparativos
├── funcionários/setores
└── relatórios

GESTOR
├── setores autorizados
├── produção
└── indicadores permitidos

USUÁRIO OPERACIONAL
└── resumo e informações autorizadas
```

Depois seguiremos para relatórios gerenciais, APIs finais para
Dashboard, auditoria/qualidade e preparação para IA/RAG.

**O frontend continuará fora do escopo até o backend estar completo,
testado e estabilizado.**
