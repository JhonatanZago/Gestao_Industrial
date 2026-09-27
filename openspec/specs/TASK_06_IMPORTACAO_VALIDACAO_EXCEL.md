# TASK EXECUTÁVEL 06 --- Importação e Validação de Planilhas Excel

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--05.

Antes de alterar código:

1.  leia o projeto completo;
2.  leia todas as specs e changes OpenSpec;
3.  execute todos os testes existentes;
4.  confirme build estável;
5.  preserve arquitetura e contratos;
6.  crie a nova change OpenSpec;
7.  implemente;
8.  teste com arquivos Excel reais/sintéticos controlados;
9.  corrija falhas;
10. execute regressão completa.

**Não implementar frontend.** **Não implementar Dashboard nesta task.**
**Não iniciar a próxima task automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Implementar uma camada robusta de importação de planilhas Excel
utilizadas pelos setores da metalúrgica.

As planilhas atuais podem conter dados semelhantes a:

``` text
Código Funcionário
Código Peça
Quantidade
Data
Horário Início
Horário Fim
Setor
Máquina
Turno
```

Nem todas as planilhas necessariamente possuem todas essas colunas.

O sistema deverá:

``` text
ARQUIVO EXCEL
     ↓
LEITURA
     ↓
IDENTIFICAÇÃO / MAPEAMENTO
     ↓
NORMALIZAÇÃO
     ↓
VALIDAÇÃO
     ↓
RESOLUÇÃO DOS CÓDIGOS
     ↓
PRÉ-VALIDAÇÃO COMPLETA
     ↓
IMPORTAÇÃO TRANSACIONAL
     ↓
REGISTROS DE PRODUÇÃO
     ↓
RELATÓRIO DE IMPORTAÇÃO
```

------------------------------------------------------------------------

# 2. OpenSpec

Criar nova change:

``` text
importacao-excel-producao
```

Respeitar a convenção OpenSpec instalada.

Criar/atualizar capacidades equivalentes a:

``` text
importacao-excel
validacao-importacao
historico-importacao
```

O `proposal.md` deverá explicar que o Excel é atualmente uma fonte
operacional da metalúrgica e que a importação precisa ser segura,
auditável, validada e independente do frontend.

------------------------------------------------------------------------

# 3. Princípio arquitetural fundamental

A importação NÃO deve gravar dados diretamente nos repositories
ignorando regras existentes.

O importador deve reutilizar, sempre que tecnicamente apropriado, as
regras de domínio/Services implementadas nas tasks anteriores.

Fluxo conceitual:

``` text
ExcelImportService
       ↓
normalização / resolução
       ↓
serviços de domínio
       ↓
regras existentes
       ↓
persistência
```

Evitar duplicar regras como:

-   quantidade \> 0;
-   validação temporal;
-   pertencimento à empresa;
-   recursos ativos;
-   sobreposição;
-   setor/máquina;
-   códigos empresariais.

------------------------------------------------------------------------

# 4. Biblioteca Excel

Adicionar biblioteca consolidada para leitura de `.xlsx`.

Preferir Apache POI, salvo se já houver solução equivalente no projeto.

Não adicionar dependências desnecessárias.

Suporte mínimo:

``` text
.xlsx
```

`.xls` legado somente se puder ser suportado com segurança e baixo custo
técnico. Registrar decisão no design.

------------------------------------------------------------------------

# 5. Não confiar no nome da planilha

Não assumir que todo arquivo possui exatamente:

``` text
Planilha1
```

ou:

``` text
Sheet1
```

Definir estratégia para selecionar a aba:

-   configuração explícita quando necessário;
-   primeira aba válida como fallback controlado;
-   rejeição clara se nenhuma aba válida for encontrada.

Registrar a decisão.

------------------------------------------------------------------------

# 6. Layout configurável

As planilhas dos setores podem possuir nomes/ordens de colunas
diferentes.

Não acoplar o importador apenas à posição fixa:

``` text
A = funcionário
B = peça
C = quantidade
```

Criar mecanismo de mapeamento por cabeçalho e/ou perfil de importação.

Exemplos de cabeçalhos equivalentes:

``` text
Funcionário
Cod. Funcionário
Código Funcionário
COD_FUNC
```

O sistema deverá possuir estratégia explícita e testável de aliases.

Não criar heurística excessivamente "mágica".

------------------------------------------------------------------------

# 7. Perfil de importação

Criar conceito equivalente a:

``` text
PerfilImportacao
```

ou configuração interna extensível, conforme arquitetura mais adequada.

Um perfil poderá representar o layout de um setor/origem.

Exemplo:

``` text
Perfil: Pintura padrão 2026

codigoFuncionario → "Código Funcionário"
codigoPeca        → "Código Peça"
quantidade        → "Quantidade"
data              → "Data"
horaInicio        → "Início"
horaFim           → "Fim"
codigoMaquina     → "Máquina"
codigoTurno       → "Turno"
```

Não é obrigatório expor CRUD completo do perfil nesta primeira versão se
isso gerar complexidade excessiva. Porém, a arquitetura deve evitar
hardcode espalhado.

Registrar a escolha no design.

------------------------------------------------------------------------

# 8. Setor da importação

A importação deve possuir contexto explícito de:

``` text
empresa
setor
```

Exemplo:

``` http
POST /api/v1/empresas/1/setores/2/importacoes/producao
```

Assim, uma planilha da Pintura poderá ser importada explicitamente para
o setor Pintura.

Se a planilha também trouxer setor, validar consistência.

Não confiar silenciosamente em setor divergente no arquivo.

------------------------------------------------------------------------

# 9. Entidade Importacao

Criar entidade de auditoria equivalente a:

``` text
ImportacaoProducao
```

Campos mínimos:

``` text
id
empresa
setor
nomeArquivo
hashArquivo
status
totalLinhas
linhasValidas
linhasInvalidas
linhasImportadas
iniciadoEm
finalizadoEm
createdAt
```

Adicionar outros campos somente quando justificados.

------------------------------------------------------------------------

# 10. Status da importação

Criar enum técnico estável equivalente a:

``` text
RECEBIDA
VALIDANDO
VALIDADA
IMPORTANDO
CONCLUIDA
FALHOU
REJEITADA
```

Esse enum representa estados internos do workflow, portanto é apropriado
como enum.

Definir transições válidas no design.

------------------------------------------------------------------------

# 11. Resultado por linha

Criar estrutura/entidade equivalente a:

``` text
ResultadoLinhaImportacao
```

ou mecanismo persistente apropriado para registrar problemas.

Campos conceituais:

``` text
numeroLinha
status
codigoFuncionario
codigoPeca
mensagem
codigoErro
```

O proprietário/usuário precisa saber exatamente quais linhas falharam.

------------------------------------------------------------------------

# 12. Princípio de atomicidade

Nesta primeira versão, utilizar estratégia **all-or-nothing** por
importação de produção.

Ou seja:

``` text
100 linhas
99 válidas
1 inválida
```

Resultado:

``` text
0 registros de produção persistidos
```

e relatório contendo o erro da linha inválida.

Motivo:

evitar importações parcialmente aplicadas sem percepção do usuário.

A validação deve ocorrer antes da persistência final.

Registrar a decisão no design.

Preparar arquitetura para possível modo parcial no futuro, mas NÃO
implementá-lo agora.

------------------------------------------------------------------------

# 13. Validação antes de persistir

O fluxo deve ser:

``` text
ler arquivo
↓
interpretar linhas
↓
normalizar
↓
resolver referências
↓
validar TODAS as linhas
↓
validar conflitos entre linhas do próprio arquivo
↓
validar conflitos com banco
↓
somente se tudo estiver válido:
persistir
```

Não persistir linha por linha antes de saber que o arquivo completo é
válido.

------------------------------------------------------------------------

# 14. Código do funcionário

Ler como String e preservar zeros à esquerda sempre que possível.

Exemplo:

``` text
00147
```

deve continuar:

``` text
00147
```

Cuidado: Excel pode armazenar visualmente `00147` como número com
formatação.

Criar estratégia usando formatter apropriado do Apache POI para obter o
valor apresentado ao usuário quando necessário.

Testar.

------------------------------------------------------------------------

# 15. Código da peça

Ler como String.

Suportar:

``` text
54871
00054871
ABC-102
P-01-A
```

Não converter automaticamente para inteiro.

------------------------------------------------------------------------

# 16. Quantidade

Aceitar somente valor compatível com regra do domínio.

Rejeitar:

``` text
vazio
texto inválido
0
negativo
```

Não arredondar silenciosamente valores inválidos.

------------------------------------------------------------------------

# 17. Datas

Excel pode representar datas como:

-   célula de data;
-   texto;
-   número serial.

Criar parser controlado.

Aceitar formatos explicitamente suportados, por exemplo:

``` text
dd/MM/yyyy
yyyy-MM-dd
```

e células Excel de data válidas.

Não interpretar ambiguidades silenciosamente.

------------------------------------------------------------------------

# 18. Horários

Suportar:

-   células de horário Excel;
-   texto em formatos definidos.

Exemplos:

``` text
08:14
08:14:30
```

Não aceitar horário inválido silenciosamente.

------------------------------------------------------------------------

# 19. Data + horário

Construir:

``` text
inicio
fim
```

como data/hora completas.

Se a planilha tiver:

``` text
Data = 26/09/2026
Início = 22:00
Fim = 02:00
```

a estratégia deve reconhecer, conforme regra documentada, que o fim pode
ocorrer no dia seguinte.

Não transformar automaticamente todo `fim < inicio` em dia seguinte sem
regra clara.

Definir uma estratégia controlada no design e testar.

------------------------------------------------------------------------

# 20. Linhas vazias

Ignorar linhas completamente vazias.

Não contar linhas vazias finais do Excel como erro.

Linhas parcialmente preenchidas devem ser validadas e podem gerar erro.

------------------------------------------------------------------------

# 21. Cabeçalhos

Validar cabeçalhos obrigatórios.

Se faltar coluna essencial, rejeitar o arquivo antes de processar as
linhas.

Exemplo:

``` text
Coluna obrigatória "Código Funcionário" não encontrada.
```

------------------------------------------------------------------------

# 22. Resolução do funcionário

Para cada linha:

``` text
codigoFuncionario
       ↓
buscar Funcionario por empresa + codigo
```

Se não existir:

rejeitar a linha.

NÃO criar funcionário automaticamente nesta task.

Mensagem:

``` text
Funcionário de código 00147 não encontrado na empresa.
```

------------------------------------------------------------------------

# 23. Resolução da peça

Fluxo:

``` text
codigoPeca
   ↓
empresa + codigo
```

Se não existir:

rejeitar.

NÃO criar peça automaticamente.

------------------------------------------------------------------------

# 24. Resolução do setor

O setor vem prioritariamente do contexto da importação.

Se existir coluna de setor:

-   normalizar;
-   resolver;
-   verificar se corresponde ao setor informado no endpoint/perfil;
-   rejeitar divergência.

------------------------------------------------------------------------

# 25. Resolução da máquina

Máquina é opcional quando a coluna/dado não existir.

Se informada:

``` text
codigoMaquina
↓
empresa + codigo
```

Validar:

-   existência;
-   ativa;
-   setor compatível.

------------------------------------------------------------------------

# 26. Resolução do turno

Turno é opcional quando não houver informação suficiente.

Se código estiver presente:

``` text
empresa + codigoTurno
```

Validar existência e status.

Não inventar turno.

------------------------------------------------------------------------

# 27. Sobreposição dentro do próprio arquivo

Antes de persistir, detectar conflito temporal entre linhas do MESMO
arquivo para o mesmo funcionário.

Exemplo:

``` text
Linha 10:
00147 | 08:00 → 10:00

Linha 11:
00147 | 09:30 → 11:00
```

Rejeitar importação e apontar ambas/relacionamento do conflito de forma
útil.

------------------------------------------------------------------------

# 28. Sobreposição com o banco

Também validar contra Registros de Produção já persistidos.

Reutilizar a regra implementada na TASK 03.

Não criar lógica divergente.

------------------------------------------------------------------------

# 29. Duplicidade exata

Detectar linhas duplicadas dentro do mesmo arquivo.

Exemplo:

``` text
00147 | 54871 | 350 | 08:00 | 10:00
00147 | 54871 | 350 | 08:00 | 10:00
```

Rejeitar ou sinalizar conforme decisão documentada.

Recomendação inicial: rejeitar para evitar dupla contabilização.

------------------------------------------------------------------------

# 30. Reimportação do mesmo arquivo

Calcular hash criptográfico do conteúdo do arquivo, por exemplo SHA-256.

Se o mesmo arquivo já tiver sido importado com sucesso para o mesmo
contexto, impedir reimportação acidental por padrão.

Não confiar apenas em `nomeArquivo`.

Exemplo:

``` text
producao_pintura_26-09.xlsx
```

pode ser renomeado e continuar sendo o mesmo conteúdo.

------------------------------------------------------------------------

# 31. Arquivo corrigido

Um arquivo corrigido terá hash diferente e poderá ser processado
novamente, desde que seus registros não entrem em conflito com dados já
persistidos.

Não implementar substituição automática de importação anterior.

Correção/reversão será tratada em change futura se necessária.

------------------------------------------------------------------------

# 32. Tamanho do arquivo

Definir limite configurável de upload.

Não permitir arquivo arbitrariamente grande.

Configurar valor razoável e documentar.

Evitar carregar arquivos enormes integralmente em memória quando
desnecessário.

------------------------------------------------------------------------

# 33. Segurança do arquivo

Validar:

-   extensão;
-   MIME quando confiável;
-   assinatura/formato real quando possível;
-   arquivo vazio;
-   workbook corrompido.

Não executar macros.

Não processar fórmulas de maneira que permitam comportamento inseguro.

Para células com fórmula, definir estratégia segura: preferir valor
calculado disponível ou rejeitar em campos críticos se não puder ser
interpretado com segurança.

Registrar decisão.

------------------------------------------------------------------------

# 34. API --- validação

Implementar endpoint de pré-validação, por exemplo:

``` http
POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/validar
```

Recebe arquivo multipart.

Retorna relatório sem persistir Registros de Produção.

Exemplo conceitual:

``` json
{
  "valido": false,
  "totalLinhas": 120,
  "linhasValidas": 118,
  "linhasInvalidas": 2,
  "erros": [
    {
      "linha": 37,
      "campo": "codigoFuncionario",
      "codigo": "FUNCIONARIO_NAO_ENCONTRADO",
      "mensagem": "Funcionário 00991 não encontrado."
    }
  ]
}
```

------------------------------------------------------------------------

# 35. API --- importar

Implementar endpoint:

``` http
POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao
```

O endpoint deve:

1.  receber arquivo;
2.  criar rastreabilidade da importação;
3.  validar;
4.  rejeitar integralmente se houver erro;
5.  persistir somente quando tudo estiver válido;
6.  retornar resumo.

------------------------------------------------------------------------

# 36. API --- histórico

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/importacoes/producao
```

Paginado.

Filtros úteis:

``` text
setorId
status
dataInicio
dataFim
nomeArquivo
```

------------------------------------------------------------------------

# 37. API --- detalhe

Implementar:

``` http
GET /api/v1/empresas/{empresaId}/importacoes/producao/{importacaoId}
```

Retornar:

-   resumo;
-   status;
-   arquivo;
-   setor;
-   contagens;
-   timestamps;
-   erros/resultado conforme estratégia de payload.

Evitar resposta gigantesca; paginar erros/linhas se necessário.

------------------------------------------------------------------------

# 38. Códigos de erro de importação

Criar códigos técnicos estáveis.

Exemplos:

``` text
CABECALHO_AUSENTE
FUNCIONARIO_NAO_ENCONTRADO
PECA_NAO_ENCONTRADA
SETOR_DIVERGENTE
MAQUINA_NAO_ENCONTRADA
MAQUINA_SETOR_INVALIDO
TURNO_NAO_ENCONTRADO
QUANTIDADE_INVALIDA
DATA_INVALIDA
HORARIO_INVALIDO
PERIODO_INVALIDO
SOBREPOSICAO
LINHA_DUPLICADA
ARQUIVO_DUPLICADO
ARQUIVO_CORROMPIDO
```

Mensagens podem evoluir; códigos devem permanecer úteis para frontend e
relatórios.

------------------------------------------------------------------------

# 39. Não expor stack trace

Erros técnicos internos não devem retornar stack trace.

Registrar log técnico no servidor e resposta segura para o cliente.

------------------------------------------------------------------------

# 40. Logs

Adicionar logs úteis:

``` text
importacaoId
empresaId
setorId
nomeArquivo
status
quantidade de linhas
tempo de processamento
```

Não logar conteúdo sensível desnecessariamente.

------------------------------------------------------------------------

# 41. Transação

A etapa final de persistência deve ser transacional.

Se ocorrer erro inesperado durante a gravação:

``` text
rollback
```

Não deixar metade dos Registros de Produção persistidos.

O histórico da importação deve conseguir representar falha de maneira
consistente.

Registrar estratégia transacional no design.

------------------------------------------------------------------------

# 42. Performance

Evitar padrão:

``` text
para cada linha:
    query funcionário
    query peça
    query máquina
    query turno
```

para milhares de linhas, se isso gerar N+1 massivo.

Implementar resolução eficiente:

-   coletar códigos distintos;
-   buscar em lote por empresa;
-   construir mapas em memória;
-   validar as linhas usando esses mapas.

Manter as regras de domínio consistentes.

------------------------------------------------------------------------

# 43. Persistência em lote

Avaliar batch insert/JPA batching para Registros de Produção após
validação.

Não sacrificar regras e consistência por performance.

Registrar a decisão.

------------------------------------------------------------------------

# 44. Modelo intermediário

Criar modelo não persistente equivalente a:

``` text
LinhaProducaoImportada
```

contendo os valores interpretados da planilha antes de virarem
entidades.

Isso ajuda a separar:

``` text
Excel
```

de:

``` text
Domínio
```

Não acoplar entidades JPA diretamente às células POI.

------------------------------------------------------------------------

# 45. Separação de responsabilidades

Evitar um único `ExcelImportService` gigante.

Separar responsabilidades de forma coerente, por exemplo:

``` text
ExcelReader
HeaderMapper
CellValueParser
ImportValidationService
ImportReferenceResolver
ProductionImportService
ImportReportService
```

Não criar abstrações vazias. Dividir somente onde houver
responsabilidade real.

------------------------------------------------------------------------

# 46. Testes unitários de parsing

Testar:

-   String;
-   números;
-   códigos com zero;
-   código alfanumérico;
-   datas Excel;
-   datas texto;
-   horários Excel;
-   horários texto;
-   células vazias;
-   fórmulas conforme estratégia;
-   tipos inesperados.

------------------------------------------------------------------------

# 47. Testes de cabeçalho

Testar:

-   layout correto;
-   ordem diferente;
-   aliases permitidos;
-   coluna obrigatória ausente;
-   coluna extra;
-   cabeçalho duplicado;
-   espaços;
-   variação de caixa conforme estratégia.

------------------------------------------------------------------------

# 48. Testes de validação de linhas

Testar:

-   funcionário inexistente;
-   peça inexistente;
-   máquina inexistente;
-   turno inexistente;
-   quantidade inválida;
-   data inválida;
-   horário inválido;
-   setor divergente;
-   recursos inativos;
-   período inválido.

------------------------------------------------------------------------

# 49. Testes de atomicidade

Arquivo:

``` text
Linha 2 válida
Linha 3 válida
Linha 4 inválida
```

Resultado:

``` text
nenhum RegistroProducao persistido
```

Validar explicitamente no banco.

------------------------------------------------------------------------

# 50. Testes de sucesso

Criar workbook de teste com, por exemplo:

``` text
00147 | 54871 | 350 | 26/09/2026 | 08:00 | 10:00
00231 | 98421 | 200 | 26/09/2026 | 10:00 | 12:00
```

Validar:

``` text
2 linhas importadas
2 RegistrosProducao persistidos
status CONCLUIDA
```

------------------------------------------------------------------------

# 51. Teste de zeros à esquerda

Criar Excel onde o código seja exibido como:

``` text
00147
```

Validar que o sistema resolve exatamente o funcionário:

``` text
00147
```

e não:

``` text
147
```

------------------------------------------------------------------------

# 52. Teste de duplicidade do arquivo

Importar arquivo válido.

Depois tentar importar exatamente os mesmos bytes novamente.

Esperado:

``` text
ARQUIVO_DUPLICADO
```

e nenhum novo Registro de Produção.

------------------------------------------------------------------------

# 53. Teste de conflito interno

Arquivo:

``` text
00147 | 08:00 → 10:00
00147 | 09:00 → 11:00
```

Rejeitar integralmente.

------------------------------------------------------------------------

# 54. Teste de conflito com banco

Banco já possui:

``` text
00147 | 08:00 → 10:00
```

Excel contém:

``` text
00147 | 09:30 → 11:00
```

Rejeitar.

------------------------------------------------------------------------

# 55. Teste de turno noturno

Validar importação de registro associado a turno:

``` text
T3 = 22:00 → 06:00
```

e produção atravessando meia-noite.

------------------------------------------------------------------------

# 56. Testes de arquivos problemáticos

Testar:

-   arquivo vazio;
-   workbook sem abas válidas;
-   arquivo corrompido;
-   extensão inválida;
-   cabeçalho sem dados;
-   somente linhas vazias;
-   arquivo acima do limite configurado quando possível.

------------------------------------------------------------------------

# 57. Fixtures de teste

Criar arquivos `.xlsx` pequenos e determinísticos em:

``` text
src/test/resources/
```

quando apropriado.

Não adicionar planilhas grandes ou dados reais/confidenciais da empresa
ao repositório.

Usar dados sintéticos.

------------------------------------------------------------------------

# 58. Dados reais

NÃO versionar planilhas reais da metalúrgica no Git sem autorização
explícita.

O sistema deve ser testável com fixtures sintéticas.

------------------------------------------------------------------------

# 59. Relatório da importação

O resultado deverá ser claro o suficiente para futuramente o frontend
mostrar:

``` text
Arquivo: producao_pintura_26-09.xlsx
Setor: Pintura a pó

120 linhas analisadas
118 válidas
2 inválidas
0 importadas

Status:
REJEITADA

Problemas:
Linha 37 — Funcionário 00991 não encontrado
Linha 84 — Quantidade deve ser maior que zero
```

------------------------------------------------------------------------

# 60. Preparação para importações futuras

A arquitetura deve permitir futuramente:

``` text
Importação de Produção
Importação de Paradas
Importação de Peças
Importação de Funcionários
Importação de Máquinas
```

Não implementar todas agora.

Evitar arquitetura exclusiva demais para uma única planilha.

------------------------------------------------------------------------

# 61. Preparação para processamento assíncrono

Nesta primeira versão, arquivos pequenos/médios podem ser processados
sincronicamente.

Entretanto, evitar decisões que impossibilitem futuramente:

``` text
upload
↓
fila/job
↓
processamento
↓
status
```

Não implementar mensageria agora.

------------------------------------------------------------------------

# 62. Preparação para analytics

Após a importação, os dados devem entrar nas mesmas tabelas/estruturas
de produção usadas pelo cadastro manual.

NÃO criar uma tabela paralela de "produção importada" usada apenas pelo
Excel.

A origem pode ser auditada, mas o domínio final deve ser único.

------------------------------------------------------------------------

# 63. Origem do registro

Avaliar adicionar ao Registro de Produção um campo técnico de origem, se
necessário:

``` text
MANUAL
IMPORTACAO
```

e referência opcional à importação.

Se adotado, utilizar enum técnico estável.

Isso ajudará auditoria futura.

Registrar a decisão no design.

Não quebrar registros anteriores.

------------------------------------------------------------------------

# 64. Rastreabilidade

Deve ser possível futuramente saber:

``` text
Registro de Produção X
veio da
Importação Y
linha Z
```

Implementar referência de rastreabilidade se tecnicamente apropriado.

Isso será importante para correção e auditoria.

------------------------------------------------------------------------

# 65. Fora do escopo

NÃO implementar:

-   frontend;
-   Angular;
-   drag-and-drop visual;
-   Dashboard;
-   gráficos;
-   KPIs;
-   ranking;
-   relatórios gerenciais finais;
-   correção visual de planilha;
-   edição de Excel;
-   exportação Excel;
-   importação parcial;
-   rollback manual de importação concluída;
-   fila/mensageria;
-   autenticação;
-   autorização;
-   IA;
-   RAG.

------------------------------------------------------------------------

# 66. Regressão

Todos os testes das TASKS 01--05 devem continuar passando.

Executar antes e depois.

Não aceitar regressões.

------------------------------------------------------------------------

# 67. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar validação OpenSpec oficial se disponível.

------------------------------------------------------------------------

# 68. Não mascarar falhas

É proibido:

-   ignorar linhas inválidas;
-   importar parcialmente sem especificação;
-   converter código silenciosamente;
-   criar funcionário/peça automaticamente;
-   apagar/desabilitar testes;
-   ignorar exceptions;
-   usar try/catch vazio;
-   persistir antes da validação completa;
-   alterar migration antiga aplicada;
-   considerar task concluída com build quebrado.

------------------------------------------------------------------------

# 69. Critérios de aceite

A task somente estará concluída quando:

1.  `.xlsx` puder ser lido;
2.  cabeçalhos forem validados;
3.  códigos forem preservados;
4.  funcionário for resolvido por empresa + código;
5.  peça for resolvida por empresa + código;
6.  setor for validado;
7.  máquina opcional for validada;
8.  turno opcional for validado;
9.  datas/horários forem interpretados;
10. quantidade for validada;
11. linhas vazias forem tratadas corretamente;
12. conflitos internos forem detectados;
13. conflitos com banco forem detectados;
14. arquivo duplicado for detectado por hash;
15. pré-validação não persistir produção;
16. importação inválida persistir zero produções;
17. importação válida persistir todos os registros;
18. histórico da importação existir;
19. erros por linha forem reportáveis;
20. rastreabilidade for preservada;
21. performance evitar N+1 massivo;
22. transação garantir atomicidade;
23. testes antigos passarem;
24. novos testes passarem;
25. `clean test` passar;
26. `build` passar;
27. OpenSpec refletir a implementação.

------------------------------------------------------------------------

# 70. Entrega esperada do Codex

Ao concluir, apresentar:

## OpenSpec

-   change;
-   proposal;
-   design;
-   tasks;
-   specs;
-   validação.

## Arquitetura

Explicar: - reader; - parser; - mapper de cabeçalhos; - resolver; -
validator; - importer; - relatório.

## Banco

-   migrations;
-   entidades de importação;
-   hash;
-   rastreabilidade;
-   índices/constraints.

## API

-   validar;
-   importar;
-   histórico;
-   detalhe.

## Excel

Informar: - formatos suportados; - aliases; - datas; - horários; -
códigos com zeros; - fórmulas; - limite de arquivo.

## Testes

-   fixtures;
-   parsing;
-   atomicidade;
-   conflitos;
-   duplicidade;
-   regressão;
-   resultado.

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

# 71. Próxima etapa prevista

Após aprovação:

``` text
TASK 07 — Indicadores, KPIs e Agregações de Produção
```

Ela começará a transformar os dados estruturados em inteligência:

``` text
Produção importada/manual
        ↓
Quantidade
Tempo bruto
Tempo parado
Tempo produtivo
        ↓
Agregações
        ↓
KPIs
        ↓
Comparativos
```

Depois virão desempenho, valorização/faturamento, segurança, relatórios,
APIs analíticas, auditoria avançada e preparação para IA/RAG.

**O frontend continuará bloqueado até o backend estar completo, testado
e estabilizado.**
