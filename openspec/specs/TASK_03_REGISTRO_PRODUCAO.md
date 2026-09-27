# TASK EXECUTÁVEL 03 --- Registro de Produção Industrial

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a estabilização das TASKS 01 e 02. Antes de
alterar código, leia o projeto e todas as specs/changes OpenSpec,
execute os testes atuais e confirme que o build está estável.

**Não implementar frontend. Não implementar Excel ainda. Não iniciar a
próxima task automaticamente.**

## 1. Objetivo

Implementar o coração transacional inicial: **Registro de Produção**.

Cada registro deverá conectar: - Empresa - Setor - Funcionário - Peça -
Quantidade - Início - Fim - Observação - Status - Auditoria temporal

Exemplo:

``` text
Empresa: Metalúrgica A
Setor: Pintura a pó
Funcionário: 00147
Peça: 54871
Quantidade: 350
Início: 26/09/2026 08:14
Fim: 26/09/2026 09:42
```

## 2. OpenSpec

Criar change `registro-producao`, respeitando a versão/convenção
instalada. Criar proposal, design, tasks e spec/capability equivalente a
`producao`.

O proposal deve explicar que as planilhas atuais registram código do
funcionário, código da peça, quantidade, data/horário e setor, e que
esses dados precisam virar registros estruturados antes da importação
Excel e dos indicadores.

## 3. Entidade RegistroProducao

Campos mínimos:

``` text
id
empresa
setor
funcionario
peca
quantidade
inicio
fim
observacao
ativo
createdAt
updatedAt
```

Todo registro MUST pertencer a uma Empresa e MUST possuir Setor,
Funcionário e Peça da mesma empresa.

## 4. Setor e funcionário

O setor deve representar onde a atividade ocorreu. Uma peça pode passar
por vários setores.

Um funcionário pode produzir em setor diferente de seu `setorPrincipal`,
caso esse campo exista. `setorPrincipal` é somente lotação/padrão e não
limita produções futuras.

## 5. Quantidade

`quantidade` é obrigatória e deve ser maior que zero. Rejeitar zero e
negativos. Escolher tipo numérico apropriado e registrar a decisão no
design.

## 6. Datas e horários

Utilizar tipos `java.time`. Preferir data/hora completas para `inicio` e
`fim`.

Obrigatório:

``` text
fim > inicio
```

Rejeitar duração zero e fim anterior ao início.

Deve suportar produção atravessando meia-noite:

``` text
26/09/2026 22:00 → 27/09/2026 02:00
```

## 7. Duração bruta

Disponibilizar cálculo:

``` text
duracaoBruta = fim - inicio
```

Não persistir redundantemente sem justificativa.

Não chamar duração bruta de tempo produtivo, pois ainda não existem
Paradas.

## 8. Sobreposição

Detectar registros temporalmente sobrepostos do MESMO funcionário.

Rejeitar:

``` text
Existente: 08:00 → 10:00
Novo:      09:30 → 11:00
```

Permitir adjacentes:

``` text
08:00 → 10:00
10:00 → 11:30
```

Permitir horários iguais para funcionários diferentes.

A detecção deve ocorrer eficientemente no banco, não carregando toda a
produção em memória.

## 9. Recursos inativos

Não permitir nova produção com: - empresa inativa; - setor inativo; -
funcionário inativo; - peça inativa.

Históricos existentes continuam consultáveis após futura desativação
desses cadastros.

## 10. Exclusão lógica

Não implementar DELETE físico. Utilizar ativação/desativação consistente
com o projeto.

## 11. Atualização

Permitir atualização controlada. Revalidar quantidade, horários,
empresa, setor, funcionário, peça e sobreposição. Na detecção de
sobreposição durante update, ignorar o próprio registro.

## 12. DTOs

Criar request/response sem expor JPA diretamente.

Request conceitual:

``` json
{
  "setorId": 1,
  "funcionarioId": 10,
  "pecaId": 25,
  "quantidade": 350,
  "inicio": "2026-09-26T08:14:00",
  "fim": "2026-09-26T09:42:00",
  "observacao": "Produção normal"
}
```

Response deve retornar dados suficientes do setor, funcionário e peça,
além da duração bruta.

## 13. Repository e Service

Criar repository para RegistroProducao com consultas por: - empresa; -
setor; - funcionário; - peça; - período; - status; - sobreposição.

Service deve concentrar regras de negócio. Resource/Controller deve
permanecer fino.

## 14. API

Base:

``` http
/api/v1/empresas/{empresaId}/producoes
```

Implementar:

``` http
POST  /api/v1/empresas/{empresaId}/producoes
GET   /api/v1/empresas/{empresaId}/producoes
GET   /api/v1/empresas/{empresaId}/producoes/{producaoId}
PUT   /api/v1/empresas/{empresaId}/producoes/{producaoId}
PATCH /api/v1/empresas/{empresaId}/producoes/{producaoId}/desativar
PATCH /api/v1/empresas/{empresaId}/producoes/{producaoId}/ativar
```

## 15. Filtros e paginação

A listagem MUST ser paginada e suportar filtros opcionais:

``` text
setorId
funcionarioId
pecaId
inicio
fim
ativo
```

Ordenação padrão recomendada: `inicio DESC`.

Evitar explosão de métodos combinatórios no repository; utilizar
abordagem sustentável compatível com a arquitetura existente.

## 16. Migration

Criar nova migration Flyway sem editar migrations antigas.

Tabela conceitual `registro_producao`:

``` text
id
empresa_id
setor_id
funcionario_id
peca_id
quantidade
inicio
fim
observacao
ativo
created_at
updated_at
```

Criar PK, FKs, NOT NULL e índices úteis.

Pensar nas futuras consultas:

``` text
produção por empresa + período
produção por setor + período
produção por funcionário + período
produção por peça + período
```

## 17. Testes obrigatórios

Testar: - criação válida; - duração bruta; - produção atravessando
meia-noite; - empresa/setor/funcionário/peça inexistentes; -
relacionamentos pertencentes a outra empresa; - quantidade
zero/negativa; - início igual ao fim; - fim anterior ao início; -
recursos inativos; - sobreposição parcial; - registro contido; -
registro envolvendo outro; - mesmo horário; - registros adjacentes; -
funcionários diferentes simultâneos; - atualização; - sobreposição no
update ignorando o próprio registro; - filtros; - paginação; -
isolamento entre empresas; - ativação/desativação.

Cenário obrigatório:

``` text
Empresa: Metalúrgica A
Setor: PINT
Funcionário: 00147
Peça: 54871
Quantidade: 350
Início: 26/09/2026 08:14
Fim: 26/09/2026 09:42
```

Validar duração bruta = **88 minutos**.

Depois, para o mesmo funcionário:

``` text
09:30 → 10:30
```

deve ser rejeitado.

Já:

``` text
09:42 → 10:30
```

deve ser permitido.

## 18. Preparação futura

Não implementar agora, mas preparar a arquitetura para:

``` text
Registro de Produção
       ↓
Paradas
       ↓
Tempo produtivo real
       ↓
Indicadores
       ↓
Dashboard
       ↓
Relatório gerencial
```

Também preparar para futura importação de linhas como:

``` text
00147 | 54871 | 350 | 08:14 | 09:42 | PINT
```

Códigos empresariais continuam estáveis, mas relacionamentos no banco
devem usar entidades/FKs reais.

## 19. Auditoria e timezone

`inicio/fim` representam o evento industrial. `createdAt/updatedAt`
representam o evento do sistema.

Não confundir os conceitos.

Utilizar `java.time` e registrar no design a estratégia de data/hora
escolhida.

## 20. Tratamento de erros

Reutilizar o `@RestControllerAdvice` existente.

Mensagens claras, por exemplo:

``` text
O funcionário informado não pertence à empresa.
A peça informada não pertence à empresa.
O setor informado está inativo.
A quantidade deve ser maior que zero.
O horário final deve ser posterior ao horário inicial.
O funcionário já possui um registro de produção sobreposto neste período.
```

## 21. Fora do escopo

NÃO implementar: - frontend/Angular; - Excel/upload; - Paradas; -
Máquinas; - Turnos; - Metas; - Desempenho; - Ranking; -
Valor/Faturamento; - Dashboard; - Relatórios; - Autenticação; -
Permissões; - IA; - RAG.

## 22. Regressão e build

Todos os testes das TASKS 01 e 02 devem continuar passando.

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Se houver comando oficial de validação OpenSpec na instalação atual,
executá-lo. Não inventar comandos.

É proibido apagar/desabilitar testes, ignorar exceções, remover
validações para passar o build ou alterar specs para justificar bugs.

## 23. Critérios de aceite

A task somente está concluída quando: 1. produção puder ser registrada;
2. empresa, setor, funcionário e peça forem obrigatórios e consistentes;
3. quantidade e período forem validados; 4. produção atravessando
meia-noite funcionar; 5. duração bruta for calculável; 6. recursos
inativos forem rejeitados em novos registros; 7. sobreposição do mesmo
funcionário for rejeitada; 8. registros adjacentes forem permitidos; 9.
funcionários diferentes puderem produzir simultaneamente; 10. filtros e
paginação funcionarem; 11. exclusão lógica funcionar; 12. migrations
funcionarem; 13. testes antigos e novos passarem; 14. `clean test`
passar; 15. `build` passar; 16. OpenSpec refletir a implementação.

## 24. Entrega esperada do Codex

Ao concluir, informar: - change/specs OpenSpec criadas/alteradas; -
arquivos criados/alterados; - entidade e relacionamentos; - migration,
FKs e índices; - endpoints; - regras temporais e de sobreposição; -
testes novos e regressão; - falhas encontradas e correções; - resultado
de `clean test`; - resultado de `build`; - decisões técnicas
relevantes; - pendências reais.

Não iniciar a próxima task.

## 25. Próxima etapa prevista

Após aprovação:

``` text
TASK 04 — Paradas e Tempos Produtivos
```

Ela deverá permitir:

``` text
Tempo bruto
-
Paradas
=
Tempo produtivo real
```

O **frontend continuará fora do escopo até o backend completo e
estabilizado**.
