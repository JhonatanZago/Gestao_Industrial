# TASK EXECUTÁVEL 11 --- Relatórios Gerenciais, Resumos Executivos e Exportações

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--10.

Antes de alterar código:

1.  leia integralmente o projeto;
2.  leia todas as specs/changes OpenSpec;
3.  execute todos os testes existentes;
4.  confirme build estável;
5.  analise as APIs analíticas já implementadas;
6.  preserve regras, segurança, isolamento empresarial e permissões;
7.  crie a nova change OpenSpec;
8.  implemente os relatórios sem duplicar regras analíticas;
9.  valide cálculos com cenários determinísticos;
10. teste autorização e exportações;
11. execute regressão completa.

**Não implementar frontend.** **Não implementar IA/RAG nesta task.**
**Não inventar recomendações gerenciais sem dados.** **Não iniciar a
próxima task automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Criar a camada de relatórios gerenciais do sistema.

O objetivo original do projeto inclui transformar as planilhas
operacionais da metalúrgica em informação útil para o proprietário e
gestores.

O backend já deverá possuir:

``` text
produção
paradas
funcionários
peças
setores
máquinas
turnos
importação Excel
KPIs
metas
tempos padrão
desempenho
valor industrial
usuários/permissões
```

Agora deverá produzir um resumo executivo estruturado capaz de
responder:

``` text
Quanto foi produzido?
Como ficou em relação ao período anterior?
Quais setores mais produziram?
Quais peças tiveram maior volume?
Quanto tempo ficou parado?
Quais foram os principais motivos de parada?
Como estão as metas?
Como está o desempenho contextualizado?
Qual foi o valor industrial produzido?
Quais áreas merecem atenção?
```

------------------------------------------------------------------------

# 2. OpenSpec

Criar change:

``` text
relatorios-gerenciais
```

Capacidades sugeridas:

``` text
relatorio-executivo
relatorios-producao
exportacao-relatorios
```

O `design.md` deve documentar:

-   fontes dos dados;
-   composição dos relatórios;
-   regras de período;
-   comparativos;
-   cobertura;
-   autorização;
-   exportações;
-   limites;
-   performance;
-   consistência numérica.

------------------------------------------------------------------------

# 3. Princípio arquitetural

O relatório NÃO deve reimplementar todas as fórmulas das TASKS 07--09.

Fluxo:

``` text
Analytics Services existentes
          ↓
Report Service / Facade
          ↓
modelo do relatório
          ↓
JSON / CSV / XLSX
```

Reutilizar as mesmas regras de:

``` text
quantidade
tempos
paradas
desempenho
metas
valor industrial
comparativos
```

Um número exibido no relatório deve coincidir com o mesmo número
retornado pelo analytics correspondente.

------------------------------------------------------------------------

# 4. Relatório Executivo

Criar relatório principal equivalente a:

``` text
RelatorioExecutivo
```

Não é obrigatório persistir o relatório se ele puder ser gerado sob
demanda.

Preferir geração sob demanda nesta primeira versão.

------------------------------------------------------------------------

# 5. Estrutura do resumo executivo

O relatório deve possuir seções estruturadas, por exemplo:

``` text
Identificação
Período
Resumo de produção
Comparativo
Setores
Funcionários
Peças
Máquinas
Turnos
Paradas
Metas
Desempenho
Valor industrial
Cobertura dos dados
Pontos de atenção objetivos
```

Cada seção deve respeitar permissões.

------------------------------------------------------------------------

# 6. Identificação

Retornar:

``` text
empresaId
empresaNome
periodoInicio
periodoFim
geradoEm
geradoPor
```

`geradoPor` deve utilizar o usuário autenticado quando aplicável.

------------------------------------------------------------------------

# 7. Resumo de produção

Incluir métricas já validadas:

``` text
quantidadeProduzida
quantidadeRegistros
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
taxaProducaoBruta
taxaProducaoProdutiva
percentualTempoParado
```

------------------------------------------------------------------------

# 8. Comparativo

Comparar com período anterior explicitamente definido.

Retornar:

``` text
periodoAtual
periodoAnterior
quantidadeAtual
quantidadeAnterior
variacaoQuantidade
tempoProdutivoAtual
tempoProdutivoAnterior
variacaoTempoProdutivo
tempoParadoAtual
tempoParadoAnterior
variacaoTempoParado
```

Quando permitido:

``` text
valorIndustrialAtual
valorIndustrialAnterior
variacaoValorIndustrial
```

------------------------------------------------------------------------

# 9. Período comparativo

Não escolher período anterior de forma obscura.

Permitir:

``` text
inicioAnterior
fimAnterior
```

ou estratégia automática claramente documentada.

Se automático:

``` text
período imediatamente anterior de mesma duração
```

Documentar os intervalos efetivamente utilizados no response.

------------------------------------------------------------------------

# 10. Setores

Incluir agregação por setor:

``` text
setorId
codigo
nome
quantidadeProduzida
tempoProdutivo
tempoParado
percentualParada
taxaProducao
desempenho quando disponível
atingimentoMeta quando disponível
valorIndustrial quando autorizado
```

------------------------------------------------------------------------

# 11. Funcionários

O relatório executivo pode incluir resumo por funcionário somente quando
o usuário autenticado possuir permissão adequada.

Retornar métricas contextualizadas.

Não criar ranking simplista de "melhor/pior funcionário".

Exemplo:

``` text
codigo
nome
quantidade
tempoProdutivo
tempoParado
indiceDesempenho
coberturaTempoPadrao
```

Valor industrial somente com permissão financeira.

------------------------------------------------------------------------

# 12. Detalhe sob demanda

O relatório executivo não deve retornar todo histórico de cada
funcionário.

O frontend futuro poderá:

``` text
clicar funcionário
↓
endpoint analítico detalhado
```

Manter relatório principal compacto.

------------------------------------------------------------------------

# 13. Peças

Incluir:

``` text
codigo
descricao
quantidadeProduzida
tempoProdutivo
desempenho quando disponível
valorIndustrial quando autorizado
```

Permitir limitar Top N para o relatório executivo.

Exemplo configurável:

``` text
top=10
```

------------------------------------------------------------------------

# 14. Máquinas

Incluir resumo:

``` text
codigo
nome
setor
quantidade
tempoProdutivo
tempoParado
percentualParada
desempenho quando disponível
valorIndustrial quando autorizado
```

------------------------------------------------------------------------

# 15. Turnos

Incluir:

``` text
turno
quantidade
tempoProdutivo
tempoParado
taxas
```

------------------------------------------------------------------------

# 16. Paradas

Incluir:

``` text
tempoParadoTotal
percentualTempoParado
quantidadeOcorrencias
principaisMotivos
```

Por motivo:

``` text
codigo
nome
categoria
ocorrencias
tempoMinutos
percentual
```

------------------------------------------------------------------------

# 17. Metas

Incluir metas aplicáveis ao período quando possível:

``` text
nome
contexto
valorMeta
valorReal
atingimentoPercentual
statusCalculado
```

Não prorratear meta de forma não especificada.

------------------------------------------------------------------------

# 18. Desempenho

Incluir:

``` text
quantidadeReal
quantidadeEsperada
indiceDesempenho
registrosComPadrao
registrosSemPadrao
coberturaPadraoPercentual
```

Não ocultar cobertura parcial.

------------------------------------------------------------------------

# 19. Valor industrial

Somente incluir quando o usuário possuir:

``` text
FINANCEIRO_INDUSTRIAL_VISUALIZAR
```

Incluir:

``` text
moeda
valorIndustrialProduzido
valorPorHoraBruta
valorPorHoraProdutiva
registrosComValor
registrosSemValor
coberturaValorPercentual
```

Não chamar de faturamento real.

------------------------------------------------------------------------

# 20. Relatório sem permissão financeira

Se o usuário pode gerar relatório, mas não pode visualizar financeiro:

-   não retornar valores;
-   não retornar campos com dados mascarados que permitam inferência;
-   preferir omitir seção/campos financeiros conforme contrato
    documentado.

Testar.

------------------------------------------------------------------------

# 21. Escopo setorial

Se o gestor possui acesso somente a:

``` text
Pintura
KTL
```

o relatório dele deve conter somente esses setores e dados derivados
deles.

Nunca gerar relatório global e depois apenas esconder os nomes dos
setores.

A filtragem deve ocorrer na origem da consulta.

------------------------------------------------------------------------

# 22. Pontos de atenção objetivos

Criar seção opcional baseada em regras determinísticas, NÃO IA.

Exemplos válidos:

``` text
Setor X apresentou aumento de 25% no tempo parado.
Meta Y ficou em 82% de atingimento.
Motivo Z representou 38% do tempo total de parada.
Cobertura de tempo padrão está em apenas 60%.
20% dos registros não possuem valor industrial configurado.
```

Esses textos devem ser derivados de dados verificáveis.

------------------------------------------------------------------------

# 23. Não gerar julgamento subjetivo

Evitar:

``` text
“Funcionário João teve desempenho ruim.”
“Setor Pintura está péssimo.”
“A empresa faturou mal.”
```

Preferir:

``` text
“O índice de desempenho calculado do setor foi 87%, com cobertura de padrão de 100%.”
```

------------------------------------------------------------------------

# 24. Regras de alertas do relatório

Criar mecanismo simples e extensível, por exemplo:

``` text
ReportInsightRule
```

ou equivalente.

Não criar engine genérica exagerada.

Regras iniciais podem considerar:

-   variação relevante de produção;
-   variação de parada;
-   atingimento de meta;
-   cobertura de padrão;
-   cobertura financeira.

Limiares devem ser configuráveis ou centralizados, não espalhados em
magic numbers.

------------------------------------------------------------------------

# 25. API --- relatório executivo

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/relatorios/executivo
```

Parâmetros:

``` text
inicio
fim
inicioAnterior?
fimAnterior?
setorId?
top?
```

Aplicar contexto autenticado e permissões.

------------------------------------------------------------------------

# 26. API --- relatório de produção

Implementar relatório mais focado:

``` http
GET /api/v1/empresas/{empresaId}/relatorios/producao
```

Com filtros:

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

# 27. API --- relatório de paradas

``` http
GET /api/v1/empresas/{empresaId}/relatorios/paradas
```

------------------------------------------------------------------------

# 28. API --- relatório de desempenho

``` http
GET /api/v1/empresas/{empresaId}/relatorios/desempenho
```

------------------------------------------------------------------------

# 29. API --- relatório financeiro industrial

``` http
GET /api/v1/empresas/{empresaId}/relatorios/financeiro-industrial
```

Exigir permissão financeira.

------------------------------------------------------------------------

# 30. Exportação

Implementar exportação dos relatórios em formatos estruturados.

Mínimo:

``` text
CSV
XLSX
```

PDF pode ser preparado para task posterior caso exija biblioteca/layout
adicional.

Não gerar PDF ruim apenas para marcar requisito.

------------------------------------------------------------------------

# 31. Exportação CSV

Gerar CSV compatível com dados tabulares.

Definir:

``` text
UTF-8
cabeçalhos claros
separador documentado
escaping correto
```

Cuidado com fórmulas em planilhas.

Valores iniciados por:

``` text
=
+
-
@
```

em campos textuais controlados pelo usuário podem causar CSV Injection
ao abrir em Excel.

Sanitizar campos exportados quando necessário.

------------------------------------------------------------------------

# 32. Exportação XLSX

Utilizar Apache POI já presente pela TASK 06, quando disponível.

Criar workbook com abas úteis, por exemplo:

``` text
Resumo
Setores
Funcionários
Peças
Máquinas
Paradas
Metas
```

Aba financeira somente se autorizada.

------------------------------------------------------------------------

# 33. XLSX --- qualidade

Aplicar:

``` text
cabeçalhos
tipos corretos de células
datas como datas
números como números
valores monetários como números
percentuais como números
```

Não exportar tudo como String.

------------------------------------------------------------------------

# 34. XLSX --- tamanho

Não utilizar auto-size de forma indiscriminada em milhares de linhas se
causar custo excessivo.

Definir limites razoáveis.

------------------------------------------------------------------------

# 35. Endpoint de exportação

Estratégia sugerida:

``` http
GET /api/v1/empresas/{empresaId}/relatorios/executivo/exportar?formato=XLSX
```

ou endpoints específicos coerentes com a arquitetura.

Tipos suportados:

``` text
CSV
XLSX
```

Utilizar enum técnico.

------------------------------------------------------------------------

# 36. Content-Type

Retornar headers corretos.

Exemplos:

``` text
text/csv
application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

Definir `Content-Disposition` seguro.

------------------------------------------------------------------------

# 37. Nome do arquivo

Gerar nome previsível e sanitizado:

``` text
relatorio-executivo-2026-09-01-a-2026-09-30.xlsx
```

Não usar entrada do usuário diretamente como caminho.

------------------------------------------------------------------------

# 38. Limites de exportação

Definir limite de período/volume quando necessário.

Não permitir exportação ilimitada capaz de consumir memória excessiva.

Registrar decisão.

------------------------------------------------------------------------

# 39. Consistência

Cenário obrigatório:

``` text
GET /analytics/resumo
```

e:

``` text
GET /relatorios/executivo
```

para o mesmo contexto devem retornar os mesmos valores-base.

Testar.

------------------------------------------------------------------------

# 40. Segurança

Endpoints de relatório devem exigir:

``` text
RELATORIO_VISUALIZAR
```

além das permissões específicas para seções sensíveis.

Exportar um relatório não pode contornar permissões existentes.

------------------------------------------------------------------------

# 41. Exportação e escopo

O XLSX/CSV deve possuir exatamente o mesmo escopo autorizado do JSON.

Teste obrigatório:

``` text
Gestor Pintura
```

não pode receber dados de Laser no arquivo exportado.

------------------------------------------------------------------------

# 42. Dados financeiros no arquivo

Usuário sem permissão financeira não deve receber:

``` text
colunas financeiras
abas financeiras
totais financeiros
```

Nem valores ocultos em células.

------------------------------------------------------------------------

# 43. Auditoria de geração

Registrar log técnico:

``` text
usuarioId
empresaId
tipoRelatorio
periodo
formato
geradoEm
```

Não registrar conteúdo completo do relatório.

Persistência formal de auditoria poderá ser aprofundada em task futura.

------------------------------------------------------------------------

# 44. DTOs

Criar DTOs específicos, por exemplo:

``` text
RelatorioExecutivoResponse
ResumoExecutivoResponse
ComparativoExecutivoResponse
ResumoSetorRelatorioResponse
ResumoFuncionarioRelatorioResponse
ResumoPecaRelatorioResponse
ResumoMaquinaRelatorioResponse
ResumoParadaRelatorioResponse
ResumoMetaRelatorioResponse
ResumoDesempenhoRelatorioResponse
ResumoFinanceiroRelatorioResponse
PontoAtencaoResponse
```

Evitar DTO gigantesco sem composição.

------------------------------------------------------------------------

# 45. Facade

Considerar:

``` text
RelatorioExecutivoFacade
```

para orquestrar múltiplos serviços analíticos.

Não mover fórmulas para a facade.

Ela deve coordenar, não recalcular tudo.

------------------------------------------------------------------------

# 46. Performance

Relatório executivo pode chamar várias agregações.

Evitar dezenas de queries redundantes.

Avaliar:

-   reutilização de filtros;
-   projections;
-   consultas agregadas;
-   paralelização somente se realmente segura/necessária;
-   limites Top N.

Não adicionar complexidade prematura.

------------------------------------------------------------------------

# 47. Transação

Relatórios são predominantemente leitura.

Utilizar transação read-only quando apropriado.

------------------------------------------------------------------------

# 48. Cenário determinístico obrigatório

Criar período com:

``` text
Produção total: 10.000 peças
Tempo bruto: 600 min
Paradas: 60 min
Tempo produtivo: 540 min
Meta: 11.000 peças
Valor industrial: R$ 8.500,00
```

Esperado:

``` text
Atingimento da meta = 90,91%
Percentual de parada = 10%
```

Validar valores herdados das tasks anteriores.

------------------------------------------------------------------------

# 49. Comparativo obrigatório

Período anterior:

``` text
8.000 peças
R$ 7.000
80 min de parada
```

Atual:

``` text
10.000 peças
R$ 8.500
60 min de parada
```

Validar:

``` text
produção = +25%
valor industrial ≈ +21,43%
tempo parado = -25%
```

------------------------------------------------------------------------

# 50. Teste de ponto de atenção

Exemplo:

``` text
cobertura padrão = 60%
```

A regra pode produzir:

``` text
“A cobertura de tempo padrão no período foi de 60%.”
```

Não inventar causa.

------------------------------------------------------------------------

# 51. Testes de permissão

Testar:

``` text
ADMINISTRADOR
GESTOR
OPERACIONAL
```

com diferentes permissões.

Validar:

-   acesso ao relatório;
-   escopo setorial;
-   seção financeira;
-   exportação;
-   funcionários.

------------------------------------------------------------------------

# 52. Testes CSV

Testar:

-   encoding;
-   cabeçalho;
-   separador;
-   aspas;
-   quebra de linha;
-   caracteres acentuados;
-   sanitização de formula injection.

------------------------------------------------------------------------

# 53. Testes XLSX

Abrir workbook em teste e validar:

-   abas;
-   células;
-   tipos;
-   números;
-   percentuais;
-   financeiro condicionado à permissão;
-   ausência de dados proibidos.

------------------------------------------------------------------------

# 54. Testes de volume

Gerar volume sintético razoável e validar que exportação não possui
comportamento evidentemente inadequado.

Não exigir benchmark complexo.

------------------------------------------------------------------------

# 55. Não persistir arquivo no servidor sem necessidade

Preferir gerar stream/arquivo temporário controlado e retornar.

Não acumular exportações indefinidamente no filesystem.

------------------------------------------------------------------------

# 56. Preparação para PDF

Projetar o modelo de relatório separado do formato de exportação para
permitir futuramente:

``` text
JSON
CSV
XLSX
PDF
```

sem reimplementar regras.

------------------------------------------------------------------------

# 57. Preparação para IA/RAG

Os DTOs e serviços de relatório devem fornecer dados estruturados e
semanticamente claros.

Futuramente o RAG/IA poderá utilizar:

``` text
resumos
indicadores
metas
comparativos
paradas
```

Mas NÃO integrar modelo de IA nesta task.

------------------------------------------------------------------------

# 58. Fora do escopo

NÃO implementar:

-   frontend;
-   gráficos visuais;
-   PDF obrigatório;
-   envio por e-mail;
-   agendamento automático;
-   WhatsApp;
-   IA;
-   RAG;
-   recomendações generativas;
-   faturamento fiscal;
-   NF-e;
-   custos/lucro não modelados.

------------------------------------------------------------------------

# 59. Regressão

Todos os testes das TASKS 01--10 devem continuar passando.

Não desabilitar segurança nos testes.

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

-   duplicar fórmulas e gerar números divergentes;
-   inventar insights;
-   chamar valor industrial de faturamento real;
-   vazar financeiro;
-   vazar setor;
-   exportar dados proibidos;
-   gerar tudo como String no XLSX;
-   ignorar CSV Injection;
-   desabilitar testes;
-   editar migrations antigas sem necessidade;
-   considerar task concluída com build falhando.

------------------------------------------------------------------------

# 62. Critérios de aceite

A task somente estará concluída quando:

1.  relatório executivo funcionar;
2.  período funcionar;
3.  comparativo funcionar;
4.  resumo de produção coincidir com analytics;
5.  setores forem resumidos;
6.  funcionários forem resumidos conforme permissão;
7.  peças forem resumidas;
8.  máquinas forem resumidas;
9.  turnos forem resumidos;
10. paradas forem resumidas;
11. metas forem apresentadas;
12. desempenho e cobertura forem apresentados;
13. financeiro aparecer somente quando autorizado;
14. pontos de atenção forem determinísticos;
15. escopo setorial for respeitado;
16. CSV funcionar;
17. CSV Injection for tratada;
18. XLSX funcionar;
19. XLSX possuir tipos corretos;
20. exportação respeitar permissões;
21. volume possuir limites razoáveis;
22. testes de consistência passarem;
23. testes de segurança passarem;
24. testes anteriores passarem;
25. `clean test` passar;
26. `build` passar;
27. OpenSpec refletir a implementação.

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

## Arquitetura

Explicar: - Report Service/Facade; - reutilização dos Analytics; -
modelo de relatório; - exporters.

## Relatórios

Listar seções e regras.

## Segurança

Explicar: - permissão de relatório; - escopo setorial; - financeiro; -
exportações.

## Exportações

Informar: - CSV; - XLSX; - headers; - tipos; - limites; - sanitização.

## Testes

Informar: - matemática; - consistência; - comparativos; - permissões; -
CSV; - XLSX; - regressão.

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
TASK 12 — Auditoria, Rastreabilidade e Histórico de Alterações
```

Essa task deverá responder:

``` text
Quem alterou?
O que alterou?
Quando alterou?
Qual era o valor anterior?
Qual ficou sendo o valor novo?
Qual empresa?
Qual recurso?
```

Isso será importante principalmente para:

``` text
produção
metas
tempos padrão
valores financeiros
usuários
permissões
importações
```

Depois poderemos avançar para qualidade/confiabilidade, API final do
Dashboard e preparação para IA/RAG.

**O frontend continuará fora do escopo até o backend estar completo,
testado e estabilizado.**
