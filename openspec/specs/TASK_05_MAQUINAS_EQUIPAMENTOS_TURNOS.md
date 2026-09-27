# TASK EXECUTÁVEL 05 --- Máquinas, Equipamentos, Turnos e Alocação Produtiva

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização de:

-   TASK 01 --- Fundação: Empresa e Setores Dinâmicos
-   TASK 02 --- Funcionários e Peças
-   TASK 03 --- Registro de Produção Industrial
-   TASK 04 --- Paradas, Motivos e Tempos Produtivos

Antes de alterar código:

1.  leia o projeto completo;
2.  leia todas as specs/changes OpenSpec;
3.  execute a suíte atual de testes;
4.  confirme que o build está estável;
5.  preserve as decisões anteriores;
6.  crie a nova change;
7.  implemente;
8.  teste;
9.  corrija;
10. execute regressão completa.

**Não implementar frontend.** **Não implementar importação Excel
ainda.** **Não iniciar a próxima task automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Implementar a estrutura industrial necessária para representar:

-   máquinas;
-   equipamentos/recursos produtivos;
-   vínculo com setores;
-   turnos de trabalho;
-   horários dos turnos;
-   associação opcional da produção à máquina/equipamento;
-   associação da produção ao turno;
-   associação opcional de paradas à máquina/equipamento;
-   regras temporais básicas.

Após esta task, o sistema deverá conseguir representar algo como:

``` text
EMPRESA
│
├── SETOR: Pintura a pó
│   │
│   ├── Máquina: Cabine 01
│   ├── Máquina: Cabine 02
│   └── Máquina: Forno 01
│
├── SETOR: Laser
│   │
│   ├── Máquina: Laser 01
│   └── Máquina: Laser 02
│
└── TURNOS
    ├── 1º Turno
    ├── 2º Turno
    └── 3º Turno
```

E um Registro de Produção poderá informar:

``` text
Setor: Pintura a pó
Máquina: Cabine 01
Funcionário: 00147
Peça: 54871
Turno: 1º Turno
Quantidade: 350
08:14 → 09:42
```

------------------------------------------------------------------------

# 2. OpenSpec

Criar nova change seguindo exatamente a convenção existente.

Nome sugerido:

``` text
maquinas-e-turnos
```

Criar artefatos equivalentes a:

``` text
proposal.md
design.md
tasks.md
specs/
```

Criar/atualizar capacidades equivalentes a:

``` text
maquinas
turnos
alocacao-produtiva
```

Não alterar specs anteriores silenciosamente.

------------------------------------------------------------------------

# 3. Proposal

Registrar:

## Problema

Até o momento o sistema consegue saber:

-   empresa;
-   setor;
-   funcionário;
-   peça;
-   quantidade;
-   período;
-   paradas.

Porém, para análises industriais reais, também precisamos saber em qual
recurso produtivo a atividade ocorreu e em qual turno.

Sem isso, futuramente seria difícil responder:

-   qual máquina produziu mais;
-   qual máquina ficou mais parada;
-   qual turno teve melhor produtividade;
-   onde estão os gargalos;
-   qual equipamento apresenta maior incidência de manutenção;
-   qual setor/máquina apresenta menor disponibilidade.

## Solução

Criar:

-   cadastro dinâmico de máquinas/equipamentos;
-   vínculo das máquinas com setores;
-   cadastro de turnos;
-   horários de turno;
-   integração opcional desses dados com Produção e Paradas.

------------------------------------------------------------------------

# 4. Terminologia

Utilizar um conceito de domínio consistente.

Pode ser adotado:

``` text
Maquina
```

ou, se tecnicamente mais adequado:

``` text
RecursoProdutivo
```

com tipo/categoria.

A escolha deverá ser registrada no `design.md`.

O sistema precisa representar tanto máquinas tradicionais quanto
recursos produtivos identificáveis.

Exemplos:

``` text
Prensa 01
Laser 02
Dobradeira 03
Cabine de Pintura 01
Forno 01
Tanque KTL
Linha de Zincagem
Robô de Solda 01
```

Evitar uma modelagem excessivamente restrita à palavra "máquina".

------------------------------------------------------------------------

# 5. Entidade de recurso produtivo

Criar entidade equivalente a:

``` text
Maquina
```

ou:

``` text
RecursoProdutivo
```

Campos mínimos:

``` text
id
empresa
setor
codigo
nome
descricao
tipo
ativo
createdAt
updatedAt
```

Campos adicionais simples podem ser incluídos somente se houver
justificativa real.

Não implementar manutenção preventiva/corretiva completa nesta task.

------------------------------------------------------------------------

# 6. Setor da máquina

Toda máquina/recurso produtivo MUST pertencer a uma Empresa.

Nesta fase, cada máquina deve possuir um Setor atual responsável.

Exemplo:

``` text
Cabine 01
→ Pintura a pó
```

``` text
Laser 01
→ Laser
```

O Setor deve pertencer à mesma Empresa.

------------------------------------------------------------------------

# 7. Movimentação futura de máquina

Não criar arquitetura que torne impossível mover uma máquina de setor
futuramente.

Nesta task, o campo `setor` representa a alocação atual.

Histórico de movimentação entre setores poderá ser implementado
futuramente se necessário.

Não criar histórico complexo agora.

------------------------------------------------------------------------

# 8. Código da máquina

`codigo`:

-   obrigatório;
-   String;
-   normalizado;
-   único dentro da empresa;
-   preservar códigos empresariais;
-   aceitar letras, números e separadores razoáveis.

Exemplos:

``` text
PINT-CAB-01
LASER-01
DB-003
FORNO-A
```

Constraint conceitual:

``` text
empresa_id + codigo
```

------------------------------------------------------------------------

# 9. Nome

`nome` obrigatório.

Exemplos:

``` text
Cabine de Pintura 01
Laser Trumpf 01
Dobradeira 03
Forno de Cura 01
```

Normalizar espaços externos.

------------------------------------------------------------------------

# 10. Tipo de recurso

Se útil, criar classificação de alto nível.

Exemplos:

``` text
MAQUINA
LINHA
FORNO
CABINE
TANQUE
CELULA
OUTRO
```

Se utilizar enum, ele deve representar uma classificação técnica
estável.

NÃO criar enum com nomes concretos das máquinas da empresa.

------------------------------------------------------------------------

# 11. Exclusão lógica da máquina

Não excluir fisicamente.

Utilizar:

``` text
ativo = true / false
```

Máquina inativa:

-   não pode ser utilizada em nova produção;
-   não pode receber nova parada operacional;
-   continua aparecendo corretamente em históricos antigos.

------------------------------------------------------------------------

# 12. Entidade Turno

Criar entidade:

``` text
Turno
```

Campos mínimos:

``` text
id
empresa
codigo
nome
horaInicio
horaFim
ativo
createdAt
updatedAt
```

Exemplos:

``` text
T1
1º Turno
06:00 → 14:00
```

``` text
T2
2º Turno
14:00 → 22:00
```

``` text
T3
3º Turno
22:00 → 06:00
```

------------------------------------------------------------------------

# 13. Turnos dinâmicos

Não criar:

``` java
enum Turno {
    PRIMEIRO,
    SEGUNDO,
    TERCEIRO
}
```

Turnos devem ser cadastráveis.

A empresa poderá ter:

``` text
Administrativo
Comercial
Turno A
Turno B
Noturno
Extra
```

sem alteração de código.

------------------------------------------------------------------------

# 14. Código do turno

`codigo`:

-   obrigatório;
-   String;
-   único por empresa;
-   normalizado.

Constraint:

``` text
empresa_id + codigo
```

------------------------------------------------------------------------

# 15. Horário do turno

Utilizar `LocalTime` ou tipo `java.time` apropriado.

Turnos podem atravessar meia-noite.

Exemplo válido:

``` text
22:00 → 06:00
```

Não rejeitar apenas porque:

``` text
horaFim < horaInicio
```

Para turno, isso significa atravessar meia-noite.

Registrar a estratégia no design.

------------------------------------------------------------------------

# 16. Duração do turno

Disponibilizar cálculo da duração nominal do turno.

Exemplo:

``` text
06:00 → 14:00
= 8 horas
```

``` text
22:00 → 06:00
= 8 horas
```

Não persistir duração redundante sem justificativa.

------------------------------------------------------------------------

# 17. Turno com início igual ao fim

Por padrão, rejeitar:

``` text
08:00 → 08:00
```

para evitar ambiguidade entre:

``` text
0 horas
```

e:

``` text
24 horas
```

Se no futuro existir turno de 24h, criar regra explícita em change
própria.

------------------------------------------------------------------------

# 18. Intervalos e almoço

NÃO implementar automaticamente:

-   almoço;
-   café;
-   descanso;
-   intervalo programado.

Esses elementos dependerão de regras reais da empresa.

Nesta task o Turno representa apenas sua janela nominal.

Calendário operacional e intervalos planejados poderão ser adicionados
futuramente.

------------------------------------------------------------------------

# 19. Associação da produção à máquina

Evoluir `RegistroProducao` para permitir:

``` text
maquina/recursoProdutivo
```

A associação deve ser opcional inicialmente.

Motivo:

as planilhas atuais podem não possuir informação de máquina em todas as
linhas.

Não tornar o campo obrigatório sem evidência dos dados reais.

------------------------------------------------------------------------

# 20. Validação máquina x empresa

Se uma máquina for informada no Registro de Produção:

-   deve existir;
-   deve estar ativa;
-   deve pertencer à mesma empresa.

Rejeitar máquina de outra empresa.

------------------------------------------------------------------------

# 21. Validação máquina x setor

Por padrão, a máquina utilizada deve estar atualmente associada ao mesmo
setor do Registro de Produção.

Exemplo válido:

``` text
Produção:
Setor = Pintura

Máquina:
Cabine 01
Setor atual = Pintura
```

Exemplo inválido:

``` text
Produção:
Setor = Pintura

Máquina:
Laser 01
Setor atual = Laser
```

Rejeitar por padrão.

Registrar no design.

Se futuramente recursos compartilhados entre setores forem necessários,
tratar em change própria.

------------------------------------------------------------------------

# 22. Associação da produção ao turno

Evoluir `RegistroProducao` para permitir:

``` text
turno
```

A associação poderá ser opcional inicialmente para preservar
compatibilidade com registros existentes.

O turno deve pertencer à mesma empresa.

Turno inativo não pode ser utilizado em nova produção.

------------------------------------------------------------------------

# 23. Determinação automática de turno

NÃO assumir automaticamente o turno apenas pelo horário sem considerar
casos de fronteira e mudanças futuras.

Pode ser implementado helper/serviço para sugerir ou resolver turno
quando houver correspondência inequívoca, mas:

-   não duplicar regra;
-   não sobrescrever turno explicitamente informado sem justificativa;
-   registrar comportamento no design;
-   testar turnos que atravessam meia-noite.

Se a determinação automática adicionar risco desnecessário, deixar para
task futura e manter seleção explícita.

------------------------------------------------------------------------

# 24. Produção atravessando troca de turno

Não rejeitar automaticamente uma produção porque ela atravessa o limite
entre dois turnos.

Exemplo:

``` text
Produção:
13:50 → 14:20

T1:
06:00 → 14:00

T2:
14:00 → 22:00
```

Nesta fase, o Registro de Produção possui no máximo um turno associado.

Registrar no design a limitação.

Divisão automática de produção entre turnos fica fora do escopo desta
task.

------------------------------------------------------------------------

# 25. Associação da parada à máquina

Evoluir `ParadaProducao` para permitir referência opcional à
máquina/recurso produtivo.

Se a Produção possuir máquina e a parada for uma parada daquela máquina,
o vínculo pode ser registrado.

Não tornar obrigatório.

------------------------------------------------------------------------

# 26. Consistência da máquina na parada

Se a Parada informar máquina:

-   deve pertencer à mesma empresa;
-   deve estar relacionada de forma consistente ao contexto produtivo;
-   deve estar ativa para novas paradas, conforme regra adotada.

Se o Registro de Produção já possuir uma máquina e a parada indicar
outra, por padrão rejeitar, salvo decisão explicitamente justificada no
design.

------------------------------------------------------------------------

# 27. Paradas de máquina sem produção

NÃO implementar nesta task paradas independentes de produção.

Exemplo futuro:

``` text
Máquina parada das 05:00 às 06:00
sem Registro de Produção ativo
```

Isso poderá ser necessário para disponibilidade/OEE, mas exige modelagem
própria.

Registrar como evolução futura.

------------------------------------------------------------------------

# 28. DTO --- Máquina

Criar request/response.

Exemplo:

``` json
{
  "setorId": 2,
  "codigo": "PINT-CAB-01",
  "nome": "Cabine de Pintura 01",
  "descricao": "Cabine principal da linha",
  "tipo": "CABINE"
}
```

------------------------------------------------------------------------

# 29. DTO --- Turno

Exemplo:

``` json
{
  "codigo": "T1",
  "nome": "1º Turno",
  "horaInicio": "06:00",
  "horaFim": "14:00"
}
```

------------------------------------------------------------------------

# 30. Evolução dos DTOs de produção

Adicionar campos opcionais adequados:

``` text
maquinaId
turnoId
```

Não quebrar contratos existentes desnecessariamente.

Responses devem retornar identificação amigável quando informado:

``` text
maquina:
  id
  codigo
  nome

turno:
  id
  codigo
  nome
```

Evitar payload excessivo.

------------------------------------------------------------------------

# 31. API --- Máquinas

Base sugerida:

``` http
/api/v1/empresas/{empresaId}/maquinas
```

Implementar:

``` http
POST
GET
GET /{maquinaId}
PUT /{maquinaId}
PATCH /{maquinaId}/ativar
PATCH /{maquinaId}/desativar
```

Listagem paginada.

Filtros:

``` text
setorId
codigo
nome
tipo
ativo
```

------------------------------------------------------------------------

# 32. API --- Turnos

Base:

``` http
/api/v1/empresas/{empresaId}/turnos
```

Implementar:

``` http
POST
GET
GET /{turnoId}
PUT /{turnoId}
PATCH /{turnoId}/ativar
PATCH /{turnoId}/desativar
```

Listagem paginada.

Filtros:

``` text
codigo
nome
ativo
```

------------------------------------------------------------------------

# 33. Repository

Criar repositories para:

``` text
Maquina/RecursoProdutivo
Turno
```

Consultas necessárias:

### Máquina

-   por empresa;
-   por setor;
-   por código;
-   por nome;
-   por tipo;
-   por status.

### Turno

-   por empresa;
-   por código;
-   por nome;
-   por status.

Manter consultas sustentáveis.

------------------------------------------------------------------------

# 34. Services

Criar Services responsáveis por:

-   normalização;
-   duplicidade;
-   empresa;
-   setor;
-   status;
-   horários;
-   ativação/desativação;
-   integração com Produção;
-   integração opcional com Parada.

Não colocar regra de negócio nos Controllers.

------------------------------------------------------------------------

# 35. Migrations

Criar novas migrations Flyway.

Não editar migrations antigas já aplicadas.

Criar tabelas equivalentes a:

``` text
maquina
turno
```

Adicionar colunas opcionais em:

``` text
registro_producao
parada_producao
```

quando necessário.

Adicionar:

``` text
maquina_id
turno_id
```

de forma compatível com dados existentes.

------------------------------------------------------------------------

# 36. Constraints

Máquina:

``` text
UNIQUE (empresa_id, codigo)
```

Turno:

``` text
UNIQUE (empresa_id, codigo)
```

Criar FKs apropriadas.

Não utilizar cascade remove em históricos industriais.

------------------------------------------------------------------------

# 37. Índices

Planejar índices úteis para futuras consultas:

``` text
produção por máquina + período
produção por turno + período
paradas por máquina
máquinas por setor
```

Evitar excesso.

------------------------------------------------------------------------

# 38. Testes --- Máquina

Testar:

1.  cadastrar máquina;
2.  normalizar código;
3.  rejeitar código vazio;
4.  rejeitar nome vazio;
5.  rejeitar empresa inexistente;
6.  rejeitar setor inexistente;
7.  rejeitar setor de outra empresa;
8.  rejeitar código duplicado na mesma empresa;
9.  permitir mesmo código em empresas diferentes;
10. listar;
11. filtrar por setor;
12. atualizar;
13. desativar;
14. reativar;
15. impedir acesso cruzado.

------------------------------------------------------------------------

# 39. Testes --- Turno

Testar:

1.  cadastrar turno diurno;
2.  cadastrar turno noturno;
3.  calcular duração de turno normal;
4.  calcular duração atravessando meia-noite;
5.  rejeitar início igual ao fim;
6.  rejeitar código duplicado na empresa;
7.  permitir mesmo código em empresas diferentes;
8.  atualizar;
9.  desativar;
10. reativar;
11. impedir acesso cruzado.

Cenário:

``` text
T1:
06:00 → 14:00
= 8h

T3:
22:00 → 06:00
= 8h
```

------------------------------------------------------------------------

# 40. Testes --- Produção com máquina

Testar:

1.  criar produção sem máquina;
2.  criar produção com máquina válida;
3.  rejeitar máquina inexistente;
4.  rejeitar máquina de outra empresa;
5.  rejeitar máquina inativa;
6.  rejeitar máquina de setor incompatível;
7.  atualizar produção adicionando máquina;
8.  alterar máquina;
9.  remover associação opcional quando permitido.

------------------------------------------------------------------------

# 41. Testes --- Produção com turno

Testar:

1.  produção sem turno;
2.  produção com turno válido;
3.  rejeitar turno inexistente;
4.  rejeitar turno de outra empresa;
5.  rejeitar turno inativo;
6.  turno noturno;
7.  produção atravessando troca de turno sem corromper dados.

------------------------------------------------------------------------

# 42. Testes --- Parada com máquina

Testar:

1.  parada sem máquina;
2.  parada com máquina válida;
3.  rejeitar máquina de outra empresa;
4.  rejeitar máquina inconsistente com a produção;
5.  preservar cálculos de tempo parado;
6.  preservar regras de sobreposição.

------------------------------------------------------------------------

# 43. Cenário de integração obrigatório

Criar:

``` text
Empresa:
Metalúrgica A

Setor:
PINT - Pintura a pó

Máquina:
PINT-CAB-01 - Cabine de Pintura 01

Turno:
T1 - 06:00 → 14:00

Funcionário:
00147

Peça:
54871
```

Registrar:

``` text
Produção:
08:00 → 12:00
Quantidade: 350
Máquina: PINT-CAB-01
Turno: T1
```

Adicionar:

``` text
Parada:
09:10 → 09:30
Máquina: PINT-CAB-01
```

Validar:

``` text
duração bruta = 240 min
tempo parado = 20 min
tempo produtivo = 220 min
```

------------------------------------------------------------------------

# 44. Preparação para importação Excel

A próxima grande etapa será a importação das planilhas.

A arquitetura deverá permitir que o importador resolva opcionalmente:

``` text
codigoFuncionario
codigoPeca
codigoSetor
codigoMaquina
codigoTurno
```

para entidades reais.

Não implementar Excel agora.

Não permitir que futuros importadores gravem diretamente no banco
ignorando Services e validações.

------------------------------------------------------------------------

# 45. Preparação para indicadores

Após esta task, teremos base para indicadores futuros como:

``` text
produção por máquina
produção por turno
tempo parado por máquina
tempo produtivo por máquina
quantidade por turno
gargalos por recurso
```

Não implementar Dashboard ou agregações avançadas agora.

------------------------------------------------------------------------

# 46. OEE

NÃO implementar OEE nesta task.

OEE exige definição confiável de:

-   disponibilidade;
-   performance;
-   qualidade;
-   tempo planejado;
-   velocidade ideal;
-   refugo/peças boas.

Ainda não temos todos esses conceitos.

A arquitetura não deve impedir OEE futuramente.

------------------------------------------------------------------------

# 47. Calendário industrial

NÃO implementar calendário de:

-   feriados;
-   dias úteis;
-   escalas;
-   horas extras;
-   intervalos;
-   folgas.

Turnos nesta task representam somente janelas nominais.

------------------------------------------------------------------------

# 48. Auditoria

Preservar `createdAt` e `updatedAt`.

Não apagar histórico quando:

-   máquina for desativada;
-   turno for desativado;
-   setor for alterado.

Dados históricos devem continuar consultáveis.

------------------------------------------------------------------------

# 49. Tratamento de exceções

Reutilizar o padrão global.

Mensagens claras:

``` text
A máquina informada não pertence à empresa.

A máquina informada não pertence ao setor da produção.

A máquina está inativa.

O turno informado não pertence à empresa.

O turno está inativo.

Já existe uma máquina com este código nesta empresa.

Já existe um turno com este código nesta empresa.
```

------------------------------------------------------------------------

# 50. Fora do escopo

NÃO implementar:

-   frontend;
-   Angular;
-   Excel;
-   upload;
-   Dashboard;
-   gráficos;
-   manutenção completa;
-   ordens de manutenção;
-   OEE;
-   calendário industrial;
-   intervalos;
-   metas;
-   qualidade/refugo;
-   valor/faturamento;
-   relatórios gerenciais;
-   autenticação;
-   autorização;
-   IA;
-   RAG.

------------------------------------------------------------------------

# 51. Regressão

Todos os testes das TASKS 01--04 devem continuar passando.

Executar suite completa antes e depois.

Não aceitar regressões.

------------------------------------------------------------------------

# 52. Comandos obrigatórios

Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar validação OpenSpec oficial, se disponível.

Não inventar comando.

------------------------------------------------------------------------

# 53. Não mascarar falhas

É proibido:

-   apagar/desabilitar testes;
-   remover validações;
-   ignorar exceções;
-   usar `try/catch` vazio;
-   editar migrations antigas aplicadas;
-   quebrar APIs anteriores sem necessidade;
-   considerar concluído com build falhando;
-   alterar specs silenciosamente para justificar bugs.

Corrigir a causa real.

------------------------------------------------------------------------

# 54. Critérios de aceite

A task somente estará concluída quando:

1.  máquinas/recursos puderem ser cadastrados;
2.  máquinas pertencerem a empresa e setor;
3.  códigos forem únicos por empresa;
4.  máquinas puderem ser ativadas/desativadas;
5.  turnos forem dinâmicos;
6.  turnos diurnos funcionarem;
7.  turnos atravessando meia-noite funcionarem;
8.  produção puder opcionalmente possuir máquina;
9.  produção puder opcionalmente possuir turno;
10. máquina da produção for validada contra empresa/setor;
11. turno for validado contra empresa;
12. parada puder opcionalmente referenciar máquina;
13. cálculos da TASK 04 continuarem corretos;
14. dados históricos forem preservados;
15. migrations funcionarem;
16. testes anteriores continuarem passando;
17. novos testes passarem;
18. `clean test` passar;
19. `build` passar;
20. OpenSpec refletir a implementação.

------------------------------------------------------------------------

# 55. Entrega esperada do Codex

Ao concluir, apresentar:

## OpenSpec

-   change;
-   proposal;
-   design;
-   tasks;
-   specs;
-   validação.

## Código

-   arquivos criados/alterados.

## Banco

-   migrations;
-   tabelas;
-   novas colunas;
-   FKs;
-   constraints;
-   índices.

## APIs

-   endpoints de máquinas;
-   endpoints de turnos;
-   alterações de Produção/Parada.

## Decisões

Explicar: - nome escolhido (`Maquina` ou `RecursoProdutivo`); - vínculo
com setor; - estratégia de turno noturno; - máquina opcional na
produção; - turno opcional; - parada com máquina; - limitações atuais.

## Testes

-   regressão;
-   testes novos;
-   falhas;
-   correções;
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

# 56. Próxima etapa prevista

Após aprovação:

``` text
TASK 06 — Importação e Validação de Planilhas Excel
```

Essa será uma das principais tasks do projeto, pois começará a
transformar os dados atuais da metalúrgica em registros reais do
sistema:

``` text
PLANILHA EXCEL
      ↓
VALIDAÇÃO
      ↓
RESOLUÇÃO DOS CÓDIGOS
      ↓
REGISTROS DE PRODUÇÃO
      ↓
PARADAS / MÁQUINAS / TURNOS
      ↓
BASE ANALÍTICA
```

Somente depois avançaremos para indicadores, comparativos, relatórios,
segurança e APIs analíticas.

**O frontend permanece fora do escopo até o backend estar completo,
testado e estabilizado.**
