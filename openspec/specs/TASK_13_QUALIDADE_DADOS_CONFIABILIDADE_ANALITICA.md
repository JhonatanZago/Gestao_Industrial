# TASK EXECUTÁVEL 13 --- Qualidade dos Dados, Validações Avançadas e Confiabilidade Analítica

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--12.

Antes de alterar código:

1.  leia integralmente o projeto;
2.  leia todas as specs/changes OpenSpec;
3.  execute todos os testes;
4.  confirme `clean test` e `build` estáveis;
5.  identifique todas as fontes de dados que alimentam analytics e
    relatórios;
6.  preserve regras de negócio, auditoria, autenticação e isolamento
    empresarial;
7.  crie a nova change OpenSpec;
8.  implemente validações e diagnósticos sem alterar silenciosamente
    dados históricos;
9.  teste cenários determinísticos;
10. execute regressão completa.

**Não implementar frontend.** **Não implementar IA/RAG nesta task.**
**Não corrigir automaticamente dados de negócio sem regra explícita.**
**Não iniciar a próxima task automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Criar uma camada de **qualidade e confiabilidade dos dados
industriais**.

O sistema já consegue transformar produção em:

``` text
KPIs
desempenho
metas
valor industrial
relatórios
```

Porém, indicadores corretos dependem de dados corretos.

Esta task deverá identificar situações como:

``` text
produção sem tempo padrão
produção sem valor industrial
registros com horários inválidos/suspeitos
possíveis duplicidades
quantidades suspeitas
paradas inconsistentes
cadastros incompletos
baixa cobertura de tempos padrão
baixa cobertura financeira
dados importados com problemas
configurações históricas ausentes
```

O objetivo é permitir que o sistema diga:

``` text
“Os números foram calculados, mas apenas 72% dos registros possuem tempo padrão.”
```

em vez de apresentar um indicador aparentemente preciso baseado em dados
incompletos.

------------------------------------------------------------------------

# 2. OpenSpec

Criar change:

``` text
qualidade-confiabilidade-dados
```

Capabilities sugeridas:

``` text
qualidade-dados
diagnosticos-dados
confiabilidade-analytics
```

O `design.md` deve documentar:

-   regras de qualidade;
-   severidades;
-   escopo;
-   cálculo de cobertura;
-   possíveis duplicidades;
-   dados suspeitos;
-   execução sob demanda;
-   integração com importação;
-   integração com relatórios;
-   performance;
-   segurança.

------------------------------------------------------------------------

# 3. Princípio fundamental

Separar:

``` text
ERRO DE DOMÍNIO
```

de:

``` text
ALERTA DE QUALIDADE
```

Exemplo:

``` text
fim < início
```

deve continuar sendo rejeitado pela regra de domínio.

Já:

``` text
produção de 50.000 peças em 2 minutos
```

pode ser tecnicamente válida, mas suspeita.

Nesse caso, gerar alerta de qualidade e não apagar/corrigir
automaticamente.

------------------------------------------------------------------------

# 4. Diagnóstico

Criar conceito equivalente a:

``` text
ProblemaQualidadeDados
```

Pode ser calculado sob demanda e/ou persistido quando necessário.

Campos conceituais:

``` text
codigo
tipo
severidade
mensagem
empresaId
tipoRecurso
recursoId?
setorId?
dataReferencia?
detalhes
```

Não persistir alertas derivados sem necessidade.

------------------------------------------------------------------------

# 5. Severidade

Criar enum técnico estável:

``` text
INFO
ALERTA
CRITICO
```

Exemplo:

``` text
INFO
Cobertura financeira de 95%.

ALERTA
20% das produções não possuem tempo padrão.

CRITICO
Registro com duração negativa encontrado em dado legado.
```

Não usar severidade subjetiva sem regra.

------------------------------------------------------------------------

# 6. Categoria

Criar categorias técnicas, por exemplo:

``` text
CADASTRO
PRODUCAO
PARADA
TEMPO_PADRAO
VALOR_PRODUCAO
IMPORTACAO
COBERTURA
DUPLICIDADE
CONSISTENCIA_TEMPORAL
```

------------------------------------------------------------------------

# 7. Código estável

Cada regra deve possuir código estável, por exemplo:

``` text
QD-PROD-001
QD-PROD-002
QD-PADRAO-001
QD-VALOR-001
QD-DUP-001
```

Isso permitirá ao frontend futuro, relatórios e RAG referenciarem o
mesmo problema.

------------------------------------------------------------------------

# 8. Produção sem tempo padrão

Detectar registros de produção para os quais não existe TempoPadrao
aplicável na data/contexto.

Retornar:

``` text
quantidadeRegistros
quantidadeProduzida
percentualRegistros
setoresAfetados
pecasAfetadas
```

------------------------------------------------------------------------

# 9. Produção sem valor industrial

Detectar registros sem ValorProducao aplicável.

Não considerar ausência como R\$ 0.

Retornar cobertura financeira.

------------------------------------------------------------------------

# 10. Cobertura de tempo padrão

Calcular:

``` text
registrosComPadrao
registrosSemPadrao
registrosTotais
coberturaPercentual
```

Opcionalmente também:

``` text
quantidadeProduzidaComPadrao
quantidadeProduzidaSemPadrao
```

------------------------------------------------------------------------

# 11. Cobertura financeira

Calcular:

``` text
registrosComValor
registrosSemValor
registrosTotais
coberturaPercentual
```

Separar por moeda quando necessário.

------------------------------------------------------------------------

# 12. Horários inconsistentes

Validar dados persistidos/legados para detectar:

``` text
fim < início
duração zero quando não permitida
datas absurdamente futuras
```

Não alterar registro automaticamente.

------------------------------------------------------------------------

# 13. Produções muito longas

Criar regra configurável para identificar registros com duração
excepcionalmente longa.

Exemplo inicial configurável:

``` text
> 24 horas
```

Não hardcodar magic number espalhado.

------------------------------------------------------------------------

# 14. Produções muito curtas

Pode sinalizar registros com duração muito curta quando combinados com
quantidade elevada.

Não assumir que toda produção curta é erro.

------------------------------------------------------------------------

# 15. Quantidade suspeita

Criar regras configuráveis para detectar:

``` text
quantidade <= 0 em dado legado
quantidade extremamente alta
```

Registros novos já devem respeitar validações normais.

Não escolher um limite universal sem configuração/contexto.

------------------------------------------------------------------------

# 16. Taxa de produção suspeita

Quando houver duração válida:

``` text
taxa = quantidade / tempo
```

Permitir detectar outliers operacionais simples.

Nesta task, NÃO usar Machine Learning.

Utilizar regras determinísticas/configuráveis.

------------------------------------------------------------------------

# 17. Possível duplicidade de produção

Detectar registros potencialmente duplicados com combinação semelhante,
por exemplo:

``` text
empresa
funcionario
peca
setor
inicio
fim
quantidade
maquina?
turno?
```

Não excluir automaticamente.

Retornar:

``` text
grupoDuplicidade
registrosRelacionados
```

------------------------------------------------------------------------

# 18. Tolerância temporal

Se necessário, permitir regra de duplicidade aproximada configurável.

Exemplo:

``` text
mesmo funcionário
mesma peça
mesmo setor
horários com diferença <= N minutos
mesma quantidade
```

Preferir começar com duplicidade exata para reduzir falso positivo.

------------------------------------------------------------------------

# 19. Paradas inconsistentes

Detectar dados legados que violem regras esperadas:

``` text
parada fora do intervalo da produção
fim < início
sobreposição indevida
parada maior que duração bruta
```

------------------------------------------------------------------------

# 20. Tempo produtivo impossível

Detectar:

``` text
tempoParado > duracaoBruta
tempoProdutivo < 0
```

Esses casos devem ser críticos.

------------------------------------------------------------------------

# 21. Cadastro incompleto

Detectar recursos necessários para analytics com configuração
incompleta.

Exemplos:

``` text
peça sem descrição quando obrigatória
máquina sem setor
funcionário sem setor/contexto necessário
```

Somente regras compatíveis com o modelo real.

------------------------------------------------------------------------

# 22. Vigências problemáticas

Verificar dados legados/configurações para:

``` text
tempo padrão com vigência sobreposta
valor industrial com vigência sobreposta
meta conflitante
lacunas de vigência relevantes
```

As APIs novas já devem impedir novas sobreposições; esta análise serve
para dados existentes/importados.

------------------------------------------------------------------------

# 23. Lacunas históricas

Detectar períodos produtivos sem configuração aplicável.

Exemplo:

``` text
Peça 54871 produzida de janeiro a março
Tempo padrão começa apenas em abril
```

Isso reduz confiabilidade histórica.

------------------------------------------------------------------------

# 24. Importações

Integrar com a TASK 06.

Após importação, produzir resumo de qualidade do lote:

``` text
linhasImportadas
linhasComAlerta
possiveisDuplicidades
semTempoPadrao
semValor
```

Não transformar ausência de configuração financeira em erro que bloqueie
toda importação, salvo regra explícita.

------------------------------------------------------------------------

# 25. Qualidade por setor

Permitir análise:

``` text
Pintura
Cobertura padrão: 98%
Cobertura financeira: 92%
Alertas: 3

Laser
Cobertura padrão: 65%
Cobertura financeira: 40%
Alertas: 17
```

------------------------------------------------------------------------

# 26. Qualidade por período

Toda análise deve aceitar:

``` text
inicio
fim
```

Evitar scan ilimitado de todo histórico por padrão.

------------------------------------------------------------------------

# 27. Escopo de empresa

Toda consulta deve respeitar a empresa autenticada.

Nenhuma análise pode misturar empresas.

------------------------------------------------------------------------

# 28. Escopo setorial

Gestores limitados a determinados setores devem enxergar somente
qualidade desses setores.

Não permitir que contagens revelem dados de setores proibidos.

------------------------------------------------------------------------

# 29. Score de confiabilidade

Pode criar um indicador de confiabilidade SOMENTE se a fórmula for
transparente.

Não inventar um score "IA".

Preferência: apresentar primeiro métricas separadas:

``` text
coberturaPadrao
coberturaValor
registrosComAlertas
registrosCriticos
```

Se criar score composto, documentar fórmula integralmente.

------------------------------------------------------------------------

# 30. Status de confiabilidade

Pode retornar estado técnico:

``` text
ALTA
PARCIAL
BAIXA
```

somente se os limites forem centralizados/configuráveis e documentados.

Não esconder os valores-base.

------------------------------------------------------------------------

# 31. Analytics e qualidade

Adicionar metadados de qualidade aos responses analíticos principais
quando apropriado.

Exemplo:

``` json
"qualidadeDados": {
  "coberturaTempoPadrao": 82.5,
  "coberturaValor": 91.0,
  "alertas": 7
}
```

Não quebrar contratos existentes sem versionamento/compatibilidade.

------------------------------------------------------------------------

# 32. Relatórios

Integrar com TASK 11.

O relatório executivo deve poder incluir seção:

``` text
QUALIDADE DOS DADOS

Cobertura tempo padrão: 82,5%
Cobertura financeira: 91%
Possíveis duplicidades: 4
Alertas críticos: 0
```

------------------------------------------------------------------------

# 33. Pontos de atenção

Permitir que a TASK 11 gere mensagens objetivas como:

``` text
“17,5% dos registros do período não possuem tempo padrão aplicável.”
```

Não inventar causa ou recomendação operacional.

------------------------------------------------------------------------

# 34. Auditoria

Não gerar EventoAuditoria para cada diagnóstico calculado.

Auditoria é mudança de negócio.

Qualidade é diagnóstico.

Se futuramente houver ação de correção, essa alteração deve ser auditada
normalmente.

------------------------------------------------------------------------

# 35. API --- resumo de qualidade

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/qualidade-dados/resumo
```

Parâmetros:

``` text
inicio
fim
setorId?
```

------------------------------------------------------------------------

# 36. API --- problemas

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/qualidade-dados/problemas
```

Filtros:

``` text
inicio
fim
categoria?
severidade?
setorId?
tipoRecurso?
codigo?
```

Paginação obrigatória.

------------------------------------------------------------------------

# 37. API --- cobertura

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/qualidade-dados/cobertura
```

Retornar cobertura por:

``` text
geral
setor
```

quando solicitado.

------------------------------------------------------------------------

# 38. API --- duplicidades

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/qualidade-dados/duplicidades
```

Paginado.

------------------------------------------------------------------------

# 39. API --- recurso específico

Quando útil:

``` http
GET /api/v1/empresas/{empresaId}/qualidade-dados/recursos/{tipoRecurso}/{recursoId}
```

Retornar problemas detectados para o recurso.

------------------------------------------------------------------------

# 40. Permissões

Adicionar permissão equivalente:

``` text
QUALIDADE_DADOS_VISUALIZAR
```

Administradores podem receber por padrão.

Gestores respeitam escopo setorial.

Não conceder automaticamente acesso financeiro através de diagnósticos.

------------------------------------------------------------------------

# 41. Financeiro

Um alerta pode informar:

``` text
“Existem 20 registros sem valor configurado.”
```

para usuário autorizado à qualidade operacional.

Mas não revelar:

``` text
valores unitários
totais financeiros
```

sem permissão financeira.

Documentar.

------------------------------------------------------------------------

# 42. DTOs

Criar DTOs específicos:

``` text
QualidadeDadosResumoResponse
CoberturaDadosResponse
ProblemaQualidadeResponse
DuplicidadeProducaoResponse
QualidadeSetorResponse
```

Não expor entidades JPA.

------------------------------------------------------------------------

# 43. Configuração

Centralizar limites configuráveis.

Exemplo:

``` text
duracaoMaximaProducaoHoras
limiteTaxaSuspeita
limiteCoberturaAlerta
```

Utilizar `@ConfigurationProperties` ou padrão equivalente.

Não espalhar magic numbers.

------------------------------------------------------------------------

# 44. Não bloquear produção por alerta

Alertas de qualidade não devem necessariamente impedir operação.

Separar:

``` text
VALIDAÇÃO BLOQUEANTE
```

de:

``` text
DIAGNÓSTICO NÃO BLOQUEANTE
```

------------------------------------------------------------------------

# 45. Performance

Não carregar milhões de registros em memória para procurar problemas.

Utilizar:

``` text
queries agregadas
projections
consultas específicas
paginação
```

para regras compatíveis.

Duplicidade deve ser detectada preferencialmente no banco.

------------------------------------------------------------------------

# 46. Execução sob demanda

Nesta primeira versão, diagnósticos podem ser calculados sob demanda.

Não criar scheduler pesado automaticamente.

Preparar arquitetura para execução periódica futura.

------------------------------------------------------------------------

# 47. Cache

Não adicionar cache complexo nesta task.

------------------------------------------------------------------------

# 48. Cenário obrigatório --- cobertura

Criar:

``` text
100 registros
80 com tempo padrão
90 com valor industrial
```

Esperado:

``` text
coberturaPadrao = 80%
coberturaValor = 90%
```

------------------------------------------------------------------------

# 49. Cenário obrigatório --- duplicidade

Criar dois registros idênticos:

``` text
funcionario = 00147
peca = 54871
setor = Pintura
inicio = 08:00
fim = 10:00
quantidade = 300
```

Esperado:

``` text
1 grupo de possível duplicidade
2 registros relacionados
```

Não excluir nenhum.

------------------------------------------------------------------------

# 50. Cenário obrigatório --- tempo impossível

Dado legado controlado:

``` text
duracaoBruta = 60 min
tempoParado = 80 min
```

Esperado:

``` text
CRITICO
tempoProdutivo impossível
```

------------------------------------------------------------------------

# 51. Cenário obrigatório --- ausência histórica

Produção:

``` text
15/05/2026
```

Tempo padrão:

``` text
vigente apenas a partir de 01/07/2026
```

Esperado:

``` text
registro sem tempo padrão aplicável
```

Não utilizar padrão atual retroativamente.

------------------------------------------------------------------------

# 52. Cenário obrigatório --- setor

Criar:

``` text
Pintura = 100% cobertura
Laser = 50% cobertura
```

Validar agregações independentes.

------------------------------------------------------------------------

# 53. Cenário obrigatório --- segurança

Gestor autorizado somente para Pintura.

Mesmo existindo problemas em Laser:

``` text
resumo
problemas
duplicidades
cobertura
```

não podem revelar dados de Laser.

------------------------------------------------------------------------

# 54. Testes obrigatórios

Testar:

1.  cobertura padrão;
2.  cobertura financeira;
3.  ausência histórica;
4.  horários inválidos;
5.  duração longa;
6.  quantidade suspeita;
7.  taxa suspeita;
8.  duplicidade;
9.  paradas inconsistentes;
10. tempo produtivo impossível;
11. vigências;
12. importação;
13. empresa;
14. setor;
15. permissões;
16. paginação;
17. filtros;
18. relatório;
19. analytics;
20. performance básica.

------------------------------------------------------------------------

# 55. Testes de falso positivo

Criar casos válidos incomuns e garantir que regras excessivamente
agressivas não os marquem incorretamente quando configuração permitir.

------------------------------------------------------------------------

# 56. Compatibilidade

Não quebrar APIs anteriores.

Se adicionar `qualidadeDados` a responses existentes, fazer de maneira
compatível ou criar versão/endpoint separado.

------------------------------------------------------------------------

# 57. Observabilidade

Logar falhas técnicas de execução dos diagnósticos.

Não logar datasets inteiros.

------------------------------------------------------------------------

# 58. Fora do escopo

NÃO implementar:

-   frontend;
-   dashboard visual;
-   correção automática;
-   Machine Learning;
-   detecção estatística avançada;
-   IA;
-   RAG;
-   scheduler pesado;
-   notificações;
-   e-mail;
-   WhatsApp;
-   alteração automática de tempo padrão;
-   alteração automática de valor;
-   exclusão automática de duplicidades.

------------------------------------------------------------------------

# 59. Regressão

Todos os testes das TASKS 01--12 devem continuar passando.

------------------------------------------------------------------------

# 60. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar validação OpenSpec oficial disponível.

------------------------------------------------------------------------

# 61. Não mascarar falhas

É proibido:

-   alterar dados para fazer diagnóstico desaparecer;
-   excluir duplicidade automaticamente;
-   usar padrão atual retroativamente;
-   tratar ausência de valor como zero;
-   revelar financeiro sem permissão;
-   revelar outro setor/empresa;
-   criar score opaco;
-   carregar toda base sem necessidade;
-   desabilitar testes;
-   editar migration antiga;
-   concluir com build falhando.

------------------------------------------------------------------------

# 62. Critérios de aceite

A task somente estará concluída quando:

1.  resumo de qualidade funcionar;
2.  cobertura de tempo padrão estiver correta;
3.  cobertura financeira estiver correta;
4.  registros sem padrão forem detectados;
5.  registros sem valor forem detectados;
6.  possíveis duplicidades forem detectadas;
7.  inconsistências temporais forem detectadas;
8.  paradas impossíveis forem detectadas;
9.  lacunas históricas forem detectadas;
10. diagnósticos por setor funcionarem;
11. diagnósticos por período funcionarem;
12. importações integrarem qualidade;
13. relatórios puderem mostrar qualidade;
14. analytics puderem expor metadados de confiabilidade de forma
    compatível;
15. empresa for isolada;
16. escopo setorial for respeitado;
17. financeiro não vazar;
18. problemas forem paginados;
19. limites forem configuráveis;
20. nenhum dado for corrigido automaticamente;
21. testes passarem;
22. regressão passar;
23. `clean test` passar;
24. `build` passar;
25. OpenSpec refletir fielmente a implementação.

------------------------------------------------------------------------

# 63. Entrega esperada do Codex

Ao concluir, apresentar:

## OpenSpec

-   change;
-   proposal;
-   design;
-   tasks;
-   specs;
-   validação.

## Regras

Listar cada regra:

``` text
código
categoria
severidade
descrição
condição
```

## Arquitetura

Explicar: - Services; - queries; - configuração; - integração com
analytics/importação/relatórios.

## Segurança

Explicar: - empresa; - setor; - financeiro; - permissões.

## Performance

Explicar como evitou scan/N+1 desnecessário.

## Testes

Informar cenários de: - cobertura; - duplicidade; - temporal; -
histórico; - setor; - segurança; - regressão.

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

# 64. Próxima etapa prevista

Após aprovação:

``` text
TASK 14 — API Consolidada do Dashboard e Visão Executiva
```

Essa será a etapa que consolidará o backend para o futuro frontend.

Em vez de o Dashboard precisar fazer dezenas de chamadas independentes:

``` text
Dashboard
   ↓
API consolidada
   ├── resumo
   ├── produção
   ├── comparativos
   ├── setores
   ├── paradas
   ├── metas
   ├── desempenho
   ├── valor industrial
   └── qualidade dos dados
```

A API deverá respeitar automaticamente:

``` text
usuário
empresa
perfil
permissões
setores autorizados
período
```

Depois da consolidação das APIs, seguiremos para a preparação da camada
de IA/RAG e testes finais do backend.

**O frontend continuará fora do escopo nesta etapa.**
