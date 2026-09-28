# TASK EXECUTÁVEL 08 --- Metas, Tempos Padrão, Desempenho e Comparativos Industriais

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--07.

Antes de alterar código:

1.  leia todo o projeto;
2.  leia todas as specs/changes OpenSpec;
3.  execute todos os testes existentes;
4.  confirme build estável;
5.  analise o modelo efetivamente implementado;
6.  preserve regras e contratos existentes;
7.  crie a nova change OpenSpec;
8.  implemente;
9.  valide matematicamente os indicadores;
10. execute regressão completa.

**Não implementar frontend.** **Não implementar faturamento nesta
task.** **Não implementar OEE completo.** **Não iniciar a próxima task
automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Adicionar contexto industrial aos indicadores da TASK 07.

Até agora o sistema sabe quanto foi produzido e quanto tempo foi
utilizado, mas não sabe se uma determinada quantidade representa
desempenho acima ou abaixo do esperado.

Exemplo:

``` text
Funcionário A:
500 peças

Funcionário B:
350 peças
```

Isso NÃO permite concluir que A teve melhor desempenho.

A peça de A pode possuir:

``` text
tempo padrão = 20 segundos
```

e a peça de B:

``` text
tempo padrão = 60 segundos
```

Portanto, esta task deverá introduzir:

-   metas de produção;
-   tempos padrão;
-   histórico de vigência;
-   produção esperada;
-   desempenho contextualizado;
-   atingimento de meta;
-   comparativos coerentes;
-   APIs analíticas correspondentes.

------------------------------------------------------------------------

# 2. OpenSpec

Criar change:

``` text
metas-tempos-padrao-desempenho
```

Criar artefatos conforme a versão instalada.

Capacidades sugeridas:

``` text
metas-producao
tempos-padrao
desempenho-industrial
comparativos-desempenho
```

Documentar fórmulas e decisões no `design.md`.

------------------------------------------------------------------------

# 3. Princípio fundamental

Separar:

``` text
DADO REAL
```

de:

``` text
REFERÊNCIA / META
```

e de:

``` text
INDICADOR DERIVADO
```

Exemplo:

``` text
Real:
480 peças

Meta:
500 peças

Atingimento:
96%
```

Não persistir percentuais derivados sem necessidade.

------------------------------------------------------------------------

# 4. Tempo padrão

Criar conceito equivalente a:

``` text
TempoPadraoProducao
```

O tempo padrão representa o tempo de referência esperado para produzir
uma unidade ou lote em determinado contexto.

Não adicionar um único `tempoPadrao` diretamente em Peça se isso impedir
diferenças por setor/máquina.

------------------------------------------------------------------------

# 5. Contexto do tempo padrão

O tempo padrão MUST pertencer a:

``` text
empresa
peça
setor
```

Máquina pode ser opcional.

Exemplo:

``` text
Peça: 54871
Setor: Pintura
Máquina: Cabine 01
Tempo padrão/unidade: 18 segundos
```

Outro contexto:

``` text
Peça: 54871
Setor: KTL
Tempo padrão/unidade: 42 segundos
```

------------------------------------------------------------------------

# 6. Unidade interna

Evitar armazenar texto como:

``` text
"1 minuto e 20 segundos"
```

Utilizar unidade numérica consistente.

Preferência:

``` text
tempoPadraoSegundos
```

com precisão apropriada.

Se o processo exigir tempo por lote, modelar explicitamente.

------------------------------------------------------------------------

# 7. Base do tempo padrão

Criar enum técnico estável, se necessário:

``` text
POR_UNIDADE
POR_LOTE
```

Se `POR_LOTE`, armazenar também:

``` text
quantidadeLoteReferencia
```

Documentar fórmula.

Não converter silenciosamente sem conhecer a base.

------------------------------------------------------------------------

# 8. Vigência do tempo padrão

Tempos padrão mudam ao longo do tempo.

Implementar:

``` text
vigenciaInicio
vigenciaFim
```

`vigenciaFim` pode ser nulo para padrão atual.

Exemplo:

``` text
01/01/2026 → 30/06/2026 = 22 s/unidade
01/07/2026 → atual      = 18 s/unidade
```

Produção histórica deve utilizar o padrão válido na data da produção.

------------------------------------------------------------------------

# 9. Sobreposição de vigências

Não permitir dois tempos padrão ativos/vigentes sobrepostos para o mesmo
contexto:

``` text
empresa + peça + setor + máquina
```

Tratar `máquina = null` de forma consistente.

------------------------------------------------------------------------

# 10. Máquina específica versus padrão do setor

Permitir:

``` text
Peça X + Setor Pintura + sem máquina
```

como padrão geral.

E:

``` text
Peça X + Setor Pintura + Máquina 02
```

como padrão específico.

Na resolução:

``` text
1. procurar padrão específico da máquina
2. se não existir, procurar padrão geral do setor
3. se não existir, indicador dependente do padrão fica indisponível
```

Não inventar valor.

------------------------------------------------------------------------

# 11. Recursos inativos

Tempo padrão histórico continua válido mesmo se:

-   peça;
-   setor;
-   máquina

forem posteriormente desativados.

Para novos padrões, recursos devem ser válidos conforme regras do
domínio.

------------------------------------------------------------------------

# 12. Meta de produção

Criar entidade equivalente a:

``` text
MetaProducao
```

Uma meta deve possuir contexto explícito.

Campos conceituais:

``` text
id
empresa
nome
descricao
tipo
setor?
funcionario?
peca?
maquina?
turno?
quantidadeMeta
periodicidade
vigenciaInicio
vigenciaFim
ativo
createdAt
updatedAt
```

------------------------------------------------------------------------

# 13. Não criar metas ambíguas

A meta deve declarar claramente a que se aplica.

Exemplos:

``` text
Meta do setor Pintura:
20.000 peças/dia
```

``` text
Meta da Peça 54871 na Pintura:
4.000 peças/dia
```

``` text
Meta da Máquina Laser 01:
1.200 peças/turno
```

Evitar aceitar combinação contraditória de campos sem regra.

------------------------------------------------------------------------

# 14. Tipo da meta

Criar enum técnico estável, por exemplo:

``` text
EMPRESA
SETOR
FUNCIONARIO
PECA
MAQUINA
TURNO
COMPOSTA
```

Ou abordagem equivalente mais limpa.

A validação deve garantir que os campos obrigatórios para o tipo estejam
presentes.

------------------------------------------------------------------------

# 15. Periodicidade

Criar enum técnico:

``` text
TURNO
DIA
SEMANA
MES
```

Não assumir que todas as metas são diárias.

------------------------------------------------------------------------

# 16. Vigência da meta

Implementar:

``` text
vigenciaInicio
vigenciaFim
```

Meta histórica não pode mudar retroativamente apenas porque uma nova
meta foi cadastrada.

------------------------------------------------------------------------

# 17. Sobreposição de metas

Definir unicidade lógica de contexto + periodicidade + vigência.

Impedir metas ativas conflitantes para o mesmo contexto e intervalo,
salvo se o design justificar explicitamente composição.

Nesta primeira versão, preferir rejeitar ambiguidade.

------------------------------------------------------------------------

# 18. Desativação

Não excluir metas/tempos padrão fisicamente.

Utilizar histórico e ativação/desativação.

------------------------------------------------------------------------

# 19. Produção esperada por tempo padrão

Para padrão `POR_UNIDADE`:

``` text
tempoProdutivoDisponivelSegundos
/
tempoPadraoSegundosPorUnidade
=
quantidadeEsperada
```

Exemplo:

``` text
tempo produtivo = 3.600 s
tempo padrão = 18 s/unidade

esperado = 200 unidades
```

------------------------------------------------------------------------

# 20. Desempenho por tempo padrão

Fórmula:

``` text
indiceDesempenho
=
quantidadeReal
/
quantidadeEsperada
* 100
```

Exemplo:

``` text
real = 180
esperado = 200

desempenho = 90%
```

Não chamar esse indicador de OEE.

------------------------------------------------------------------------

# 21. Atingimento de meta

Fórmula:

``` text
atingimentoMeta
=
quantidadeReal
/
quantidadeMetaAplicavel
* 100
```

Exemplo:

``` text
real = 18.500
meta = 20.000

atingimento = 92.5%
```

------------------------------------------------------------------------

# 22. Meta acima de 100%

Não limitar artificialmente o indicador a 100%.

Exemplo:

``` text
real = 22.000
meta = 20.000

atingimento = 110%
```

------------------------------------------------------------------------

# 23. Divisão por zero

Nunca dividir por zero.

Quando:

``` text
meta = 0
```

ou:

``` text
tempo padrão inválido
```

a configuração deve ser rejeitada ou o indicador marcado como não
calculável, conforme contexto.

`quantidadeMeta` deve ser maior que zero.

`tempoPadrao` deve ser maior que zero.

------------------------------------------------------------------------

# 24. Desempenho de funcionário

Permitir calcular desempenho contextualizado de um funcionário
considerando as produções realizadas e os tempos padrão aplicáveis.

Se ele produziu peças diferentes, calcular o esperado por Registro de
Produção e depois agregar.

NÃO calcular simplesmente:

``` text
quantidade total / um único tempo padrão
```

------------------------------------------------------------------------

# 25. Exemplo com múltiplas peças

Produção A:

``` text
Peça X
Real: 100
Tempo produtivo: 1.800 s
Padrão: 20 s
Esperado: 90
```

Produção B:

``` text
Peça Y
Real: 50
Tempo produtivo: 3.000 s
Padrão: 60 s
Esperado: 50
```

Agregado:

``` text
Real = 150
Esperado = 140
Índice = 107.14%
```

Testar.

------------------------------------------------------------------------

# 26. Registros sem tempo padrão

Não excluir silenciosamente esses registros do resumo sem informar.

Retornar cobertura do indicador:

``` text
registrosTotais
registrosComPadrao
registrosSemPadrao
quantidadeComPadrao
```

O usuário precisa saber quando o desempenho é parcial.

------------------------------------------------------------------------

# 27. Cobertura de tempo padrão

Criar indicador de cobertura, por exemplo:

``` text
coberturaPadraoPercentual
=
registrosComPadrao / registrosTotais * 100
```

ou outra base explicitamente documentada.

Não apresentar índice de desempenho como plenamente representativo se a
cobertura for parcial sem indicar isso.

------------------------------------------------------------------------

# 28. Comparação entre funcionários

Criar endpoint analítico que permita comparar métricas contextualizadas.

Exemplo:

``` text
Código | Real | Esperado | Índice | Tempo parado | Cobertura
00147  | 950  | 900      | 105.56%| 35 min       | 100%
00192  | 870  | 910      | 95.60% | 42 min       | 100%
```

Não rotular automaticamente "melhor" ou "pior".

------------------------------------------------------------------------

# 29. Comparação por setor

Permitir:

``` text
real
esperado
índice de desempenho
meta
atingimento
tempo parado
tempo produtivo
```

por setor.

------------------------------------------------------------------------

# 30. Comparação por máquina

Quando houver padrão/meta aplicável:

``` text
produção real
produção esperada
índice
paradas
```

por máquina.

------------------------------------------------------------------------

# 31. Comparação por peça

Permitir verificar:

``` text
quantidade
tempo produtivo
tempo padrão
esperado
índice
```

por peça/contexto.

------------------------------------------------------------------------

# 32. Meta versus tempo padrão

Meta e tempo padrão são conceitos diferentes.

``` text
TEMPO PADRÃO
= referência técnica do processo
```

``` text
META
= objetivo gerencial/operacional
```

Não misturar os dois modelos.

------------------------------------------------------------------------

# 33. Paradas

Tempo esperado baseado em produtividade deve usar o `tempoProdutivo`,
não duração bruta, salvo indicador explicitamente definido de outra
forma.

Assim:

``` text
duração bruta
-
paradas
=
tempo produtivo
↓
produção esperada
```

------------------------------------------------------------------------

# 34. Meta por turno

Se periodicidade for `TURNO`, a meta deve ser associável ao turno
apropriado.

Não aplicar automaticamente uma meta de T1 ao T2.

------------------------------------------------------------------------

# 35. Meta mensal e filtros parciais

Não prorratear meta mensal automaticamente para 10 dias sem regra de
calendário industrial.

Se o período consultado não coincidir com a periodicidade da meta,
retornar comportamento explícito/documentado.

Prorrateamento por dias úteis será tratado quando houver calendário
industrial.

------------------------------------------------------------------------

# 36. API --- Tempos padrão

Base:

``` http
/api/v1/empresas/{empresaId}/tempos-padrao
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

Filtros:

``` text
setorId
pecaId
maquinaId
ativo
vigenteEm
```

Listagem paginada.

------------------------------------------------------------------------

# 37. API --- Metas

Base:

``` http
/api/v1/empresas/{empresaId}/metas-producao
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

Filtros apropriados.

------------------------------------------------------------------------

# 38. API --- Desempenho geral

Criar:

``` http
GET /api/v1/empresas/{empresaId}/analytics/desempenho
```

Com:

``` text
inicio
fim
setorId?
funcionarioId?
pecaId?
maquinaId?
turnoId?
```

------------------------------------------------------------------------

# 39. API --- Desempenho por funcionário

``` http
GET /api/v1/empresas/{empresaId}/analytics/desempenho/funcionarios
```

Paginado/ordenável.

------------------------------------------------------------------------

# 40. API --- Desempenho por setor

``` http
GET /api/v1/empresas/{empresaId}/analytics/desempenho/setores
```

------------------------------------------------------------------------

# 41. API --- Desempenho por máquina

``` http
GET /api/v1/empresas/{empresaId}/analytics/desempenho/maquinas
```

------------------------------------------------------------------------

# 42. API --- Desempenho por peça

``` http
GET /api/v1/empresas/{empresaId}/analytics/desempenho/pecas
```

------------------------------------------------------------------------

# 43. API --- Atingimento de metas

Criar endpoint conceitual:

``` http
GET /api/v1/empresas/{empresaId}/analytics/metas
```

Retornar:

``` text
meta
contexto
valorMeta
valorReal
atingimentoPercentual
vigencia
status calculado
```

Evitar classificação subjetiva.

------------------------------------------------------------------------

# 44. Status calculado da meta

Se for útil, pode retornar estado técnico como:

``` text
SEM_DADOS
ABAIXO_DA_META
ATINGIDA
ACIMA_DA_META
```

Esse status é derivado, não precisa ser persistido.

Definir:

``` text
ATINGIDA = exatamente 100%
ACIMA = >100%
ABAIXO = <100%
```

ou regra equivalente documentada.

------------------------------------------------------------------------

# 45. DTOs

Criar DTOs específicos, por exemplo:

``` text
TempoPadraoRequest
TempoPadraoResponse
MetaProducaoRequest
MetaProducaoResponse
DesempenhoResumoResponse
DesempenhoFuncionarioResponse
DesempenhoSetorResponse
DesempenhoMaquinaResponse
DesempenhoPecaResponse
AtingimentoMetaResponse
```

Não expor entidades JPA.

------------------------------------------------------------------------

# 46. Migrations

Criar novas migrations Flyway para:

``` text
tempo_padrao_producao
meta_producao
```

Adicionar:

-   PK;
-   FK;
-   NOT NULL;
-   checks quando suportados;
-   índices;
-   constraints adequadas.

Não editar migrations antigas.

------------------------------------------------------------------------

# 47. Índices

Planejar consultas por:

``` text
empresa + peça + setor + vigência
empresa + peça + setor + máquina + vigência
empresa + tipo de meta + vigência
empresa + setor + vigência
empresa + funcionário + vigência
empresa + máquina + vigência
```

Evitar excesso.

------------------------------------------------------------------------

# 48. Resolução histórica do padrão

Para cada produção, utilizar o padrão cuja vigência corresponde ao
evento produtivo.

Não utilizar simplesmente o padrão atualmente ativo.

Exemplo:

``` text
Produção: 15/05/2026
Padrão antigo: válido até 30/06/2026
Padrão novo: começa 01/07/2026
```

A produção de maio deve usar o padrão antigo.

------------------------------------------------------------------------

# 49. Data de referência

Definir qual instante do Registro de Produção determina a vigência.

Preferência inicial:

``` text
inicio da produção
```

Registrar no design e utilizar consistentemente.

------------------------------------------------------------------------

# 50. Alteração histórica

Evitar permitir edição de um tempo padrão histórico de forma que mude
silenciosamente indicadores antigos.

Preferir encerramento de vigência + criação de novo padrão.

Definir regras de atualização.

------------------------------------------------------------------------

# 51. Testes --- Tempo padrão

Testar:

1.  criar padrão geral do setor;
2.  criar padrão específico de máquina;
3.  rejeitar tempo zero;
4.  rejeitar tempo negativo;
5.  rejeitar recursos de outra empresa;
6.  vigência aberta;
7.  vigência fechada;
8.  rejeitar período invertido;
9.  rejeitar sobreposição;
10. resolver padrão específico antes do geral;
11. fallback para geral;
12. ausência de padrão;
13. histórico de vigência;
14. desativação.

------------------------------------------------------------------------

# 52. Testes --- Meta

Testar:

1.  meta de setor;
2.  meta de peça;
3.  meta de máquina;
4.  meta de turno;
5.  meta de funcionário, se suportada;
6.  quantidade \> 0;
7.  periodicidade;
8.  vigência;
9.  sobreposição;
10. empresa;
11. recursos inativos;
12. ativação/desativação.

------------------------------------------------------------------------

# 53. Cenário matemático obrigatório

Criar:

``` text
Peça: 54871
Setor: Pintura
Tempo padrão: 20 s/unidade
```

Produção:

``` text
Quantidade real: 180
Tempo produtivo: 3.600 s
```

Esperado:

``` text
Quantidade esperada = 180
Índice desempenho = 100%
```

Depois:

``` text
Quantidade real = 198
```

Esperado:

``` text
Índice = 110%
```

------------------------------------------------------------------------

# 54. Cenário de meta obrigatório

Meta:

``` text
Setor Pintura
20.000 peças/dia
```

Real:

``` text
18.500
```

Esperado:

``` text
92.50%
```

Real:

``` text
22.000
```

Esperado:

``` text
110%
```

------------------------------------------------------------------------

# 55. Cenário de vigência obrigatório

Criar:

``` text
01/01 → 30/06 = 20 s
01/07 → aberto = 18 s
```

Produção em maio:

``` text
usa 20 s
```

Produção em setembro:

``` text
usa 18 s
```

------------------------------------------------------------------------

# 56. Cenário de máquina específica

Criar:

``` text
Padrão geral:
Peça X + Pintura = 20 s
```

``` text
Padrão específico:
Peça X + Pintura + Cabine 02 = 17 s
```

Produção na Cabine 02:

``` text
usa 17 s
```

Produção na Cabine 01:

``` text
usa 20 s
```

------------------------------------------------------------------------

# 57. Cenário com cobertura parcial

Criar 10 registros:

``` text
8 possuem tempo padrão aplicável
2 não possuem
```

Validar:

``` text
registrosTotais = 10
registrosComPadrao = 8
registrosSemPadrao = 2
cobertura = 80%
```

O response deve deixar claro que o desempenho calculado possui cobertura
parcial.

------------------------------------------------------------------------

# 58. Testes de comparação

Criar múltiplos funcionários processando peças com tempos padrão
diferentes.

Validar que o comparativo usa:

``` text
real / esperado
```

e não somente quantidade absoluta.

------------------------------------------------------------------------

# 59. Não criar ranking simplista

Não implementar:

``` text
Funcionário 1 = melhor
Funcionário 2 = pior
```

apenas pelo índice.

Fatores futuros podem incluir:

-   qualidade;
-   retrabalho;
-   refugo;
-   complexidade;
-   disponibilidade;
-   metas;
-   paradas externas.

Esta task fornece métricas contextualizadas, não julgamento de pessoas.

------------------------------------------------------------------------

# 60. Performance

A resolução de tempos padrão para grande volume não deve gerar N+1.

Buscar padrões aplicáveis eficientemente.

Utilizar consultas/projections/estratégias em lote compatíveis com o
projeto.

Documentar a solução.

------------------------------------------------------------------------

# 61. Integração com TASK 07

Não duplicar toda a camada analítica.

Reutilizar:

``` text
quantidade
tempo bruto
tempo parado
tempo produtivo
filtros
períodos
agregações
```

e acrescentar:

``` text
esperado
meta
desempenho
atingimento
cobertura
```

------------------------------------------------------------------------

# 62. Tratamento de erros

Reutilizar `@RestControllerAdvice`.

Mensagens claras:

``` text
O tempo padrão deve ser maior que zero.

Já existe um tempo padrão com vigência sobreposta para este contexto.

A meta deve ser maior que zero.

Já existe uma meta conflitante neste período.

A máquina informada não pertence à empresa.

A peça informada não pertence à empresa.
```

------------------------------------------------------------------------

# 63. Fora do escopo

NÃO implementar:

-   frontend;
-   Dashboard visual;
-   gráficos;
-   faturamento;
-   preço de venda;
-   custo;
-   margem;
-   qualidade/refugo;
-   OEE completo;
-   calendário industrial;
-   prorrateamento avançado;
-   manutenção;
-   relatório PDF;
-   autenticação;
-   autorização;
-   IA;
-   RAG.

------------------------------------------------------------------------

# 64. Regressão

Todos os testes das TASKS 01--07 devem continuar passando.

Executar antes e depois.

------------------------------------------------------------------------

# 65. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar validação OpenSpec oficial se disponível.

------------------------------------------------------------------------

# 66. Não mascarar falhas

É proibido:

-   inventar tempo padrão;
-   aplicar padrão atual a todo histórico;
-   considerar ausência de padrão como zero;
-   dividir por zero;
-   arredondar cedo demais;
-   esconder cobertura parcial;
-   classificar funcionário subjetivamente;
-   apagar/desabilitar testes;
-   editar migrations antigas;
-   quebrar isolamento por empresa;
-   considerar task concluída com build falhando.

------------------------------------------------------------------------

# 67. Critérios de aceite

A task somente estará concluída quando:

1.  tempos padrão puderem ser cadastrados;
2.  contexto empresa + peça + setor funcionar;
3.  máquina específica opcional funcionar;
4.  fallback para padrão geral funcionar;
5.  vigência histórica funcionar;
6.  sobreposição de vigência for rejeitada;
7.  metas puderem ser cadastradas;
8.  periodicidades funcionarem;
9.  metas conflitantes forem rejeitadas;
10. produção esperada estiver correta;
11. índice de desempenho estiver correto;
12. atingimento de meta estiver correto;
13. valores acima de 100% forem permitidos;
14. múltiplas peças forem agregadas corretamente;
15. cobertura parcial for informada;
16. comparativo por funcionário funcionar;
17. comparativo por setor funcionar;
18. comparativo por máquina funcionar;
19. comparativo por peça funcionar;
20. dados históricos utilizarem padrão histórico;
21. N+1 evidente for evitado;
22. testes matemáticos passarem;
23. testes anteriores passarem;
24. `clean test` passar;
25. `build` passar;
26. OpenSpec refletir fielmente a implementação.

------------------------------------------------------------------------

# 68. Entrega esperada do Codex

Ao concluir, apresentar:

## OpenSpec

-   change;
-   proposal;
-   design;
-   tasks;
-   specs;
-   validação.

## Domínio

Explicar: - TempoPadrao; - MetaProducao; - vigência; - contexto; -
fallback; - desativação.

## Fórmulas

Documentar: - quantidade esperada; - índice de desempenho; -
atingimento; - cobertura.

## APIs

Listar endpoints e filtros.

## Banco

-   migrations;
-   constraints;
-   índices.

## Performance

Explicar como evitou N+1 na resolução histórica.

## Testes

Informar cenários matemáticos, vigência, metas, cobertura, comparativos
e regressão.

## Build

Informar:

``` text
clean test
build
```

## Pendências

Somente pendências reais.

Não iniciar a próxima task.

------------------------------------------------------------------------

# 69. Próxima etapa prevista

Após aprovação:

``` text
TASK 09 — Valorização da Produção, Receita Estimada e Análise Financeira Industrial
```

Essa etapa permitirá responder à parte do objetivo original relacionada
a:

``` text
“quanto foi produzido”
        +
“qual o valor dessas peças”
        ↓
VALOR DA PRODUÇÃO
        ↓
comparativos financeiros
        ↓
estimativa de faturamento produtivo
```

Ela deverá separar cuidadosamente:

``` text
produção física
valor industrial produzido
receita estimada
faturamento contábil real
```

para não apresentar números financeiros enganosos.

Depois seguiremos para segurança/permissões, relatórios gerenciais, APIs
finais para Dashboard, auditoria/qualidade e preparação para IA/RAG.

**O frontend continuará fora do escopo até o backend estar completo,
testado e estabilizado.**
