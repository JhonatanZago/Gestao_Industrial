# TASK EXECUTÁVEL 04 --- Paradas, Motivos e Tempos Produtivos

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização de:

-   TASK 01 --- Fundação: Empresa e Setores Dinâmicos
-   TASK 02 --- Funcionários e Peças
-   TASK 03 --- Registro de Produção Industrial

Antes de alterar código:

1.  leia o projeto completo;
2.  leia todas as specs/changes OpenSpec existentes;
3.  execute a suíte atual de testes;
4.  confirme que o build está estável;
5.  preserve os contratos e decisões anteriores;
6.  crie a nova change OpenSpec;
7.  implemente;
8.  teste;
9.  corrija;
10. execute regressão completa.

**Não implementar frontend.** **Não implementar Excel ainda.** **Não
iniciar a próxima task automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Implementar o controle de **Paradas de Produção** e permitir calcular
corretamente:

``` text
DURAÇÃO BRUTA
      -
TEMPO DE PARADAS
      =
TEMPO PRODUTIVO
```

Exemplo:

``` text
Produção:
08:00 → 12:00

Duração bruta:
4h00

Parada 1:
09:10 → 09:30 = 20 min

Parada 2:
10:45 → 11:00 = 15 min

Total parado:
35 min

Tempo produtivo:
3h25
```

Essa estrutura será fundamental para os futuros:

-   indicadores;
-   produtividade;
-   eficiência;
-   desempenho;
-   comparativos;
-   dashboard;
-   relatório gerencial;
-   análise de gargalos;
-   análise de motivos de parada.

------------------------------------------------------------------------

# 2. OpenSpec

Criar uma nova change seguindo exatamente a convenção OpenSpec
existente.

Nome sugerido:

``` text
paradas-tempos-produtivos
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
paradas
motivos-parada
tempos-produtivos
```

Não alterar specs anteriores silenciosamente.

------------------------------------------------------------------------

# 3. Proposal

Registrar:

## Problema

A duração bruta de um registro de produção não representa
necessariamente o tempo efetivamente produtivo.

Durante uma produção podem ocorrer interrupções como:

-   manutenção;
-   setup;
-   falta de material;
-   falta de operador;
-   limpeza;
-   regulagem;
-   troca de ferramenta;
-   quebra;
-   espera;
-   qualidade;
-   falta de energia;
-   outros motivos.

Sem registrar essas interrupções, os indicadores futuros de
produtividade seriam incorretos.

## Solução

Criar:

-   Motivo de Parada;
-   Registro de Parada;
-   associação da parada ao Registro de Produção;
-   cálculo de tempo parado;
-   cálculo de tempo produtivo;
-   validações temporais;
-   consultas e filtros;
-   testes.

------------------------------------------------------------------------

# 4. Motivos de parada devem ser dinâmicos

NÃO criar enum fixo como:

``` java
enum MotivoParada {
    MANUTENCAO,
    FALTA_MATERIAL,
    SETUP
}
```

Os motivos deverão ser cadastráveis.

Criar entidade equivalente a:

``` text
MotivoParada
```

Isso permitirá que a empresa crie novos motivos futuramente sem alterar
código.

------------------------------------------------------------------------

# 5. Entidade MotivoParada

Campos mínimos:

``` text
id
empresa
codigo
nome
descricao
categoria
ativo
createdAt
updatedAt
```

Exemplo:

``` text
codigo: MAN-001
nome: Manutenção corretiva
descricao: Parada causada por falha ou manutenção não programada
ativo: true
```

------------------------------------------------------------------------

# 6. Categoria do motivo

Evitar amarrar regras críticas a categorias rígidas.

Se for útil para análises futuras, permitir uma classificação de alto
nível.

Exemplos conceituais:

``` text
MANUTENCAO
MATERIAL
PROCESSO
QUALIDADE
OPERACIONAL
PLANEJAMENTO
EXTERNA
OUTROS
```

Se utilizar enum para `categoria`, ele deve representar uma
classificação técnica estável e NÃO os motivos específicos cadastrados
pela empresa.

Registrar a decisão no design.

------------------------------------------------------------------------

# 7. Código do motivo

`codigo`:

-   obrigatório;
-   String;
-   normalizado;
-   único por empresa;
-   preservar códigos empresariais;
-   não assumir formato exclusivamente numérico.

Permitido:

``` text
Empresa A → MAN-001
Empresa B → MAN-001
```

Não permitido:

``` text
Empresa A → MAN-001
Empresa A → MAN-001
```

Criar constraint:

``` text
empresa_id + codigo
```

------------------------------------------------------------------------

# 8. Exclusão lógica do motivo

Motivos de parada não deverão ser fisicamente apagados.

Utilizar:

``` text
ativo = true / false
```

Um motivo desativado:

-   não deve ser utilizado em novas paradas;
-   deve continuar aparecendo corretamente nos registros históricos
    antigos.

------------------------------------------------------------------------

# 9. Entidade ParadaProducao

Criar entidade equivalente a:

``` text
ParadaProducao
```

Campos mínimos:

``` text
id
empresa
registroProducao
motivoParada
inicio
fim
observacao
ativo
createdAt
updatedAt
```

------------------------------------------------------------------------

# 10. Associação obrigatória

Toda Parada MUST estar associada a um Registro de Produção.

Conceito:

``` text
Registro de Produção
08:00 → 12:00
       │
       ├── Parada 09:10 → 09:30
       └── Parada 10:45 → 11:00
```

Não criar parada solta nesta task.

Paradas gerais de máquina/linha sem Registro de Produção poderão ser
avaliadas em change futura, se necessárias.

------------------------------------------------------------------------

# 11. Empresa

A Parada, o Registro de Produção e o Motivo de Parada devem pertencer à
mesma empresa.

Rejeitar inconsistências.

Exemplo inválido:

``` text
Produção → Empresa A
Motivo → Empresa B
```

------------------------------------------------------------------------

# 12. Período da parada

Utilizar tipos `java.time`.

A parada deverá possuir:

``` text
inicio
fim
```

Obrigatório:

``` text
fim > inicio
```

Rejeitar duração zero e período invertido.

------------------------------------------------------------------------

# 13. Parada deve estar dentro da produção

Uma parada associada a um Registro de Produção MUST estar contida dentro
do período da produção.

Produção:

``` text
08:00 → 12:00
```

Permitido:

``` text
09:00 → 09:20
```

Não permitido:

``` text
07:50 → 08:20
```

Não permitido:

``` text
11:50 → 12:10
```

Não permitido:

``` text
07:00 → 13:00
```

------------------------------------------------------------------------

# 14. Limites exatos

Definir comportamento consistente.

Uma parada pode começar exatamente no início da produção:

``` text
Produção: 08:00 → 12:00
Parada:   08:00 → 08:15
```

Permitido.

Uma parada pode terminar exatamente no fim:

``` text
11:45 → 12:00
```

Permitido.

------------------------------------------------------------------------

# 15. Sobreposição de paradas

Duas paradas ativas do mesmo Registro de Produção não podem possuir
períodos sobrepostos.

Exemplo inválido:

``` text
Parada A:
09:00 → 09:30

Parada B:
09:20 → 09:45
```

Rejeitar.

Paradas adjacentes podem ser permitidas:

``` text
09:00 → 09:30
09:30 → 09:45
```

Não somar duas vezes períodos sobrepostos.

A melhor solução é impedir a sobreposição na entrada.

------------------------------------------------------------------------

# 16. Atualização de parada

Ao atualizar uma parada:

-   validar início/fim;
-   validar limites da produção;
-   validar empresa;
-   validar motivo;
-   validar status;
-   validar sobreposição;
-   ignorar a própria parada na consulta de conflito.

------------------------------------------------------------------------

# 17. Duração da parada

Calcular:

``` text
duracaoParada = fim - inicio
```

Preferir cálculo derivado.

Não persistir valor redundante sem justificativa.

Disponibilizar duração em formato útil na resposta, por exemplo:

``` text
duracaoMinutos
```

------------------------------------------------------------------------

# 18. Tempo total parado

Para cada Registro de Produção:

``` text
tempoTotalParado
=
soma das durações das paradas ativas
```

Paradas desativadas/canceladas não entram no cálculo atual.

------------------------------------------------------------------------

# 19. Tempo produtivo

Calcular:

``` text
tempoProdutivo
=
duracaoBruta
-
tempoTotalParado
```

Exemplo:

``` text
Produção:
08:00 → 12:00
= 240 minutos

Paradas:
20 + 15
= 35 minutos

Tempo produtivo:
205 minutos
```

------------------------------------------------------------------------

# 20. Proteção contra tempo negativo

O sistema nunca deve produzir:

``` text
tempoProdutivo < 0
```

As validações temporais e de sobreposição devem tornar esse cenário
impossível.

Adicionar teste defensivo.

------------------------------------------------------------------------

# 21. Alteração do Registro de Produção

A TASK 03 permite atualizar o período da produção.

Agora existe uma nova regra.

Se uma produção possuir paradas, uma alteração de:

``` text
inicio
fim
```

não poderá deixar uma parada existente fora do novo período.

Exemplo:

Produção atual:

``` text
08:00 → 12:00
```

Parada:

``` text
11:30 → 11:45
```

Tentativa de alterar produção para:

``` text
08:00 → 11:00
```

deve ser rejeitada.

Implementar integração entre as regras sem criar dependência circular
inadequada.

Criar testes.

------------------------------------------------------------------------

# 22. Desativação da produção

Definir comportamento consistente.

Se um Registro de Produção for desativado:

-   suas paradas históricas devem permanecer persistidas;
-   consultas operacionais devem respeitar status;
-   não apagar paradas em cascata.

Não utilizar cascade remove.

------------------------------------------------------------------------

# 23. Motivo inativo

Não permitir criar nova parada com motivo inativo.

Parada histórica existente continua válida e consultável caso o motivo
seja posteriormente desativado.

------------------------------------------------------------------------

# 24. Produção inativa

Não permitir adicionar nova parada a um Registro de Produção inativo.

------------------------------------------------------------------------

# 25. DTO --- Motivo de Parada

Criar DTOs de request/response.

Exemplo conceitual:

``` json
{
  "codigo": "MAN-001",
  "nome": "Manutenção corretiva",
  "descricao": "Falha ou manutenção não programada",
  "categoria": "MANUTENCAO"
}
```

------------------------------------------------------------------------

# 26. DTO --- Parada

Request conceitual:

``` json
{
  "motivoParadaId": 5,
  "inicio": "2026-09-26T09:10:00",
  "fim": "2026-09-26T09:30:00",
  "observacao": "Ajuste no equipamento"
}
```

`empresaId` e `producaoId` devem vir preferencialmente do contexto da
URL.

------------------------------------------------------------------------

# 27. Response da produção

Evoluir o response de Registro de Produção para disponibilizar, quando
adequado:

``` text
duracaoBrutaMinutos
tempoParadoMinutos
tempoProdutivoMinutos
```

Exemplo:

``` json
{
  "id": 100,
  "quantidade": 350,
  "inicio": "2026-09-26T08:00:00",
  "fim": "2026-09-26T12:00:00",
  "duracaoBrutaMinutos": 240,
  "tempoParadoMinutos": 35,
  "tempoProdutivoMinutos": 205
}
```

Evitar retornar todas as paradas dentro de todas as listagens se isso
causar payload excessivo/N+1.

Criar endpoint específico para paradas.

------------------------------------------------------------------------

# 28. API --- Motivos

Base:

``` http
/api/v1/empresas/{empresaId}/motivos-parada
```

Implementar:

``` http
POST
GET
GET /{motivoId}
PUT /{motivoId}
PATCH /{motivoId}/ativar
PATCH /{motivoId}/desativar
```

Listagem paginada.

Filtros simples por:

``` text
codigo
nome
categoria
ativo
```

------------------------------------------------------------------------

# 29. API --- Paradas

Utilizar:

``` http
/api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas
```

Implementar:

``` http
POST   /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas

GET    /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas

GET    /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}

PUT    /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}

PATCH  /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}/desativar

PATCH  /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}/ativar
```

Não implementar DELETE físico.

------------------------------------------------------------------------

# 30. Reativação de parada

Ao reativar uma parada, executar novamente todas as validações
relevantes:

-   produção ativa;
-   motivo válido conforme regra definida;
-   período dentro da produção;
-   ausência de sobreposição com outras paradas ativas.

Não simplesmente trocar:

``` text
ativo = true
```

sem validação.

------------------------------------------------------------------------

# 31. Repository

Criar repositories necessários para:

``` text
MotivoParada
ParadaProducao
```

Consultas devem suportar:

-   empresa;
-   produção;
-   motivo;
-   período;
-   status;
-   detecção de sobreposição;
-   soma eficiente do tempo parado quando adequado.

Não carregar todas as paradas do sistema em memória.

------------------------------------------------------------------------

# 32. Service

Criar Services seguindo o padrão do projeto.

As regras de:

-   pertencimento;
-   status;
-   período;
-   sobreposição;
-   cálculo;
-   reativação;
-   alteração;

devem permanecer na camada apropriada de negócio.

Controllers/Resources devem permanecer finos.

------------------------------------------------------------------------

# 33. Migrations

Criar novas migrations Flyway.

Não alterar migrations antigas já aplicadas.

Criar tabelas equivalentes a:

``` text
motivo_parada
parada_producao
```

Adicionar:

-   PKs;
-   FKs;
-   NOT NULL;
-   constraints;
-   unicidade;
-   índices.

Motivo:

``` text
UNIQUE (empresa_id, codigo)
```

Parada deve possuir FK para:

``` text
empresa
registro_producao
motivo_parada
```

------------------------------------------------------------------------

# 34. Índices

Planejar índices para consultas futuras como:

``` text
paradas por empresa + período
paradas por produção
paradas por motivo
paradas por setor via produção
tempo parado por motivo
```

Evitar excesso de índices.

Registrar decisões importantes no design.

------------------------------------------------------------------------

# 35. Testes --- Motivo de Parada

Testar:

1.  criar motivo;
2.  normalizar código;
3.  rejeitar código vazio;
4.  rejeitar nome vazio;
5.  rejeitar empresa inexistente;
6.  rejeitar código duplicado na mesma empresa;
7.  permitir mesmo código em empresas diferentes;
8.  atualizar;
9.  desativar;
10. reativar;
11. impedir acesso cruzado entre empresas.

------------------------------------------------------------------------

# 36. Testes --- Parada

Testar:

1.  criar parada válida;
2.  calcular duração;
3.  parada iniciando junto com produção;
4.  parada terminando junto com produção;
5.  rejeitar início anterior à produção;
6.  rejeitar fim posterior à produção;
7.  rejeitar período invertido;
8.  rejeitar duração zero;
9.  rejeitar produção inexistente;
10. rejeitar motivo inexistente;
11. rejeitar motivo de outra empresa;
12. rejeitar motivo inativo;
13. rejeitar produção inativa;
14. rejeitar sobreposição;
15. permitir paradas adjacentes;
16. atualizar;
17. impedir sobreposição no update;
18. ignorar a própria parada no update;
19. desativar;
20. reativar com validação.

------------------------------------------------------------------------

# 37. Testes --- Cálculos

Criar cenário:

``` text
Produção:
08:00 → 12:00

Duração bruta:
240 minutos

Parada 1:
09:10 → 09:30
20 minutos

Parada 2:
10:45 → 11:00
15 minutos
```

Validar:

``` text
tempoParado = 35 minutos
tempoProdutivo = 205 minutos
```

Depois desativar a Parada 2.

Validar:

``` text
tempoParado = 20 minutos
tempoProdutivo = 220 minutos
```

Reativar e validar novamente.

------------------------------------------------------------------------

# 38. Teste --- alteração da produção

Cenário:

``` text
Produção:
08:00 → 12:00

Parada:
11:30 → 11:45
```

Tentar alterar produção para:

``` text
08:00 → 11:00
```

Rejeitar.

Alterar para:

``` text
08:00 → 11:45
```

Definir no design se `fim` igual ao fim da parada é válido.
Recomendação: permitir.

Criar testes conforme decisão.

------------------------------------------------------------------------

# 39. Consultas futuras

A modelagem deverá permitir futuramente responder:

``` text
Quanto tempo a Pintura ficou parada este mês?

Qual motivo gerou mais tempo parado?

Qual setor possui maior índice de parada?

Qual peça sofre mais interrupções?

Quanto tempo produtivo o funcionário 00147 teve?

Qual foi o impacto das paradas na produção?
```

NÃO implementar esses indicadores agora.

Apenas garantir que os dados necessários existam.

------------------------------------------------------------------------

# 40. Preparação para máquinas

No futuro, paradas poderão estar associadas a máquinas/equipamentos.

Não adicionar `maquinaId` improvisadamente nesta task.

A próxima change poderá introduzir:

``` text
Máquinas + Turnos
```

e então evoluir os relacionamentos de forma controlada.

------------------------------------------------------------------------

# 41. Preparação para Excel

Futuramente as planilhas poderão trazer informações de parada ou
arquivos separados.

Não implementar importação nesta task.

Preservar APIs e domínio de forma que importadores futuros utilizem os
Services/regras existentes, em vez de gravar diretamente nos
repositories.

------------------------------------------------------------------------

# 42. Preparação para indicadores

Depois desta task teremos:

``` text
Quantidade
Duração bruta
Tempo parado
Tempo produtivo
```

Esses dados serão fundamentais para calcular futuramente:

``` text
peças/hora produtiva
percentual de parada
eficiência
disponibilidade
desempenho
comparativos
```

Não implementar fórmulas avançadas ainda.

------------------------------------------------------------------------

# 43. Não confundir parada com intervalo planejado

Nesta fase, `ParadaProducao` representa uma interrupção registrada
dentro de uma produção.

Turnos, almoço, intervalo padrão e calendário operacional serão tratados
adequadamente em tasks futuras.

Não inserir automaticamente almoço ou intervalo sem dados/regras
definidas.

------------------------------------------------------------------------

# 44. Auditoria

Preservar:

``` text
createdAt
updatedAt
```

Os horários:

``` text
inicio
fim
```

representam o evento industrial.

Não confundir com timestamps do sistema.

------------------------------------------------------------------------

# 45. Concorrência e consistência

Avaliar transações nas operações de criação/atualização de parada e
cálculos relacionados.

Evitar condições em que duas requisições simultâneas consigam criar
paradas conflitantes sem detecção.

Não adicionar complexidade excessiva, mas registrar no design a
estratégia utilizada.

------------------------------------------------------------------------

# 46. Tratamento de exceções

Reutilizar o padrão global existente.

Mensagens claras, como:

``` text
O motivo de parada informado não pertence à empresa.

O motivo de parada está inativo.

A parada deve estar contida no período da produção.

Já existe uma parada sobreposta neste período.

Não é possível alterar o período da produção porque existem paradas fora do novo intervalo.
```

------------------------------------------------------------------------

# 47. Fora do escopo

NÃO implementar:

-   frontend;
-   Angular;
-   gráficos;
-   Dashboard;
-   Excel;
-   upload;
-   máquinas;
-   equipamentos;
-   turnos;
-   calendário industrial;
-   metas;
-   valor de peça;
-   faturamento;
-   ranking;
-   indicadores avançados;
-   relatório gerencial;
-   autenticação;
-   autorização;
-   IA;
-   RAG.

------------------------------------------------------------------------

# 48. Regressão

Todos os testes das TASKS 01, 02 e 03 devem continuar passando.

Executar antes da implementação e depois da implementação.

Não aceitar regressões.

------------------------------------------------------------------------

# 49. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
```

Depois:

``` powershell
.\gradlew.bat build
```

Executar também a validação oficial do OpenSpec, caso disponível na
versão instalada.

Não inventar comandos.

------------------------------------------------------------------------

# 50. Não mascarar falhas

É proibido:

-   apagar testes;
-   desabilitar testes;
-   ignorar exceções;
-   usar `try/catch` vazio;
-   remover validações;
-   permitir sobreposição para fazer teste passar;
-   editar migration antiga já aplicada;
-   alterar spec silenciosamente para justificar bug;
-   considerar task concluída com build falhando.

Corrigir a causa real.

------------------------------------------------------------------------

# 51. Critérios de aceite

A task somente estará concluída quando:

1.  motivos de parada forem dinâmicos;
2.  motivos forem isolados por empresa;
3.  paradas puderem ser registradas em uma produção;
4.  parada estiver obrigatoriamente dentro do período produtivo;
5.  sobreposição de paradas for rejeitada;
6.  paradas adjacentes forem permitidas;
7.  duração de cada parada for calculável;
8.  tempo total parado for calculável;
9.  tempo produtivo for calculável;
10. paradas desativadas não entrarem no cálculo atual;
11. reativação revalidar conflitos;
12. motivo inativo não puder ser usado em nova parada;
13. produção inativa não puder receber nova parada;
14. alteração da produção não puder invalidar paradas existentes;
15. exclusão física não ocorrer;
16. migrations funcionarem;
17. APIs forem paginadas quando aplicável;
18. isolamento por empresa funcionar;
19. testes anteriores continuarem passando;
20. novos testes passarem;
21. `clean test` passar;
22. `build` passar;
23. OpenSpec refletir fielmente a implementação.

------------------------------------------------------------------------

# 52. Entrega esperada do Codex

Ao concluir, apresentar:

## OpenSpec

-   change criada;
-   proposal;
-   design;
-   tasks;
-   specs;
-   resultado da validação.

## Código

-   arquivos criados;
-   arquivos alterados.

## Banco

-   migrations;
-   constraints;
-   índices.

## APIs

-   endpoints de motivos;
-   endpoints de paradas;
-   alterações nos responses de produção.

## Regras

Explicar: - período; - sobreposição; - ativação/desativação; - cálculo
de tempo parado; - cálculo de tempo produtivo; - integração com
atualização da produção.

## Testes

Informar: - testes anteriores; - novos testes; - falhas encontradas; -
correções; - resultado final.

## Build

Informar resultado de:

``` text
clean test
build
```

## Pendências

Somente pendências reais.

Não iniciar a próxima task.

------------------------------------------------------------------------

# 53. Próxima etapa prevista

Após aprovação desta task:

``` text
TASK 05 — Máquinas, Equipamentos e Turnos
```

Depois:

``` text
TASK 06 — Importação Excel
TASK 07 — Indicadores e KPIs
TASK 08 — Desempenho e Comparativos
TASK 09 — Valorização da Produção
TASK 10 — Usuários, Segurança e Permissões
TASK 11 — Relatórios Gerenciais
TASK 12 — APIs Analíticas para Dashboard
TASK 13 — Auditoria e Qualidade
TASK 14 — Preparação IA/RAG
```

A numeração poderá evoluir conforme as regras reais do domínio forem
descobertas.

**O frontend continuará bloqueado até o backend estar completo, testado
e estabilizado.**
