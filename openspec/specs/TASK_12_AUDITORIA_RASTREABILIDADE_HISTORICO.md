# TASK EXECUTÁVEL 12 --- Auditoria, Rastreabilidade e Histórico de Alterações

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**, somente após concluir e estabilizar as TASKS 01--11.

Antes de alterar código: leia projeto e OpenSpec completos, rode todos
os testes, confirme build estável, identifique operações que alteram
dados, reutilize autenticação/permissões da TASK 10, crie a change
OpenSpec, implemente, teste segurança/integridade e execute regressão.

**Não implementar frontend. Não implementar IA/RAG. Não usar logs comuns
como substituto de auditoria persistente. Nunca registrar senhas,
hashes, tokens, secrets ou credenciais. Não iniciar a próxima task
automaticamente.**

## 1. Objetivo

Criar trilha de auditoria persistente capaz de responder:

``` text
Quem alterou?
Quando?
Em qual empresa?
Qual recurso/registro?
Qual operação?
Qual era o valor anterior?
Qual ficou sendo o novo?
A origem foi manual, importação ou sistema?
```

Auditar principalmente:

``` text
produção
paradas
funcionários
peças
máquinas
turnos
metas
tempos padrão
valores industriais
usuários
perfis/permissões
importações
```

## 2. OpenSpec

Criar change:

``` text
auditoria-rastreabilidade
```

Capabilities sugeridas:

``` text
auditoria
historico-alteracoes
rastreabilidade
```

Documentar eventos, persistência, ator, empresa, before/after,
sanitização, origem, transações, consultas, retenção, performance e
segurança.

## 3. Auditoria ≠ log técnico

Logs podem rotacionar/desaparecer. Eventos de negócio auditáveis devem
ser persistidos.

## 4. Modelo

Criar entidade equivalente a `EventoAuditoria`:

``` text
id
empresaId
usuarioId?
usuarioNomeSnapshot?
tipoRecurso
recursoId
acao
origem
dataHora
dadosAnteriores?
dadosNovos?
metadados?
correlationId?
```

Guardar ator autenticado e snapshot mínimo quando necessário. Para
operações sem humano, representar origem `SISTEMA`; nunca inventar
usuário.

## 5. Ações e origem

Enums técnicos estáveis, por exemplo:

``` text
CRIACAO
ALTERACAO
ATIVACAO
DESATIVACAO
IMPORTACAO
ALTERACAO_SENHA
ALTERACAO_PERMISSAO
```

Origem:

``` text
MANUAL
IMPORTACAO_EXCEL
SISTEMA
API
```

Simplificar se houver redundância real.

## 6. Tipos de recurso

Usar identificadores de domínio estáveis:

``` text
EMPRESA
SETOR
FUNCIONARIO
PECA
MAQUINA
TURNO
REGISTRO_PRODUCAO
PARADA
META_PRODUCAO
TEMPO_PADRAO
VALOR_PRODUCAO
USUARIO
PERFIL
PERMISSAO
IMPORTACAO
```

Não usar nome de classe Java como contrato persistido.

## 7. Before/After

Para alterações relevantes, armazenar snapshots controlados:

``` json
{"quantidade":500}
```

→

``` json
{"quantidade":550}
```

Diferenças por campo podem ser derivadas no response:

``` text
campo
valorAnterior
valorNovo
```

## 8. Snapshots seguros

JSON é aceitável se: - serialização for controlada; - DTO/snapshot
específico for usado; - entidades JPA inteiras não forem serializadas; -
formato for versionável; - tamanho for limitado; - campos sensíveis
forem removidos.

NUNCA armazenar:

``` text
senha
senhaHash
accessToken
refreshToken
JWT secret
API key
Authorization
cookie de autenticação
credenciais
```

Alteração de senha registra somente ator, alvo, ação e data. Nunca
senha/hash antigo ou novo.

## 9. Recursos auditáveis

Auditar criação/alteração/ativação/desativação conforme aplicável em: -
RegistroProducao; - Parada; - Funcionário; - Peça; - Máquina; - Turno; -
MetaProducao; - TempoPadrao; - ValorProducao; - Usuario; -
Perfil/permissões.

Alterações de permissões e valores financeiros são eventos sensíveis.

## 10. Importação Excel

Integrar com a TASK 06.

Evento de lote deve conter:

``` text
importacaoId
arquivoNomeSanitizado
setor/contexto
quantidadeLinhas
quantidadeSucesso
quantidadeErro
usuario
dataHora
```

Registros criados pelo lote devem ser rastreáveis via:

``` text
origem = IMPORTACAO_EXCEL
correlationId = importacaoId
```

Não persistir caminho local inseguro.

## 11. Correlation ID

Operações compostas podem compartilhar correlationId:

``` text
1 importação
→ 500 registros
→ mesmo correlationId
```

Sem criar infraestrutura distribuída desnecessária.

## 12. Transação

Auditoria de mudança efetivada deve ser consistente com a transação:

``` text
alteração falhou
→ não pode existir evento dizendo que foi concluída
```

Para operações críticas, se a gravação da auditoria falhar, a alteração
de negócio deve falhar junto.

Tentativas rejeitadas de login/acesso permanecem nos logs de segurança
da TASK 10; não transformar cada HTTP em auditoria de negócio.

## 13. Estratégia arquitetural

Evitar chamadas duplicadas/inconsistentes em Controllers.

Avaliar e documentar: - Service/facade central; - eventos de
aplicação/domínio; - decorator; - annotation/AOP somente onde não
prejudicar captura de before/after.

Auditoria deve ficar próxima da operação de negócio, pois ela também
pode vir de importação/processamento interno.

## 14. Contexto autenticado

Obter ator pela segurança da TASK 10:

``` text
usuarioId
empresaId
perfil
```

Nunca aceitar `usuarioId` do body como autor da alteração.

## 15. Isolamento e imutabilidade

Todo evento deve possuir empresa quando aplicável.

Empresa A não acessa eventos da Empresa B.

Eventos são append-only no uso normal. Não criar PUT/PATCH/DELETE comum
para auditoria.

## 16. Banco

Criar migration Flyway nova, sem editar migrations antigas, para tabela
equivalente:

``` text
evento_auditoria
```

Campos:

``` text
id
empresa_id
usuario_id
usuario_nome_snapshot
tipo_recurso
recurso_id
acao
origem
data_hora
dados_anteriores
dados_novos
metadados
correlation_id
```

Usar JSON nativo somente se coerente/portável para a stack real; caso
contrário TEXT/CLOB controlado.

Índices úteis:

``` text
empresa + data_hora
empresa + tipo_recurso + recurso_id
empresa + usuario_id + data_hora
empresa + acao + data_hora
correlation_id
```

## 17. APIs

Consulta paginada:

``` http
GET /api/v1/empresas/{empresaId}/auditoria
```

Filtros:

``` text
inicio
fim
usuarioId
tipoRecurso
recursoId
acao
origem
correlationId
```

Histórico de recurso:

``` http
GET /api/v1/empresas/{empresaId}/auditoria/{tipoRecurso}/{recursoId}
```

Evento individual, se útil:

``` http
GET /api/v1/empresas/{empresaId}/auditoria/eventos/{eventoId}
```

Ordenação padrão:

``` text
dataHora DESC
```

Paginação obrigatória e tamanho máximo controlado.

## 18. Permissões

Adicionar permissões equivalentes:

``` text
AUDITORIA_VISUALIZAR
AUDITORIA_FINANCEIRA_VISUALIZAR
AUDITORIA_USUARIOS_VISUALIZAR
```

ou modelo mais enxuto compatível com o sistema.

Administrador: amplo acesso apenas dentro da própria empresa.

Gestor: auditoria limitada aos setores autorizados quando o recurso
possuir contexto setorial.

Financeiro: snapshots com valores somente para usuário autorizado.

Usuários/permissões: histórico exige autorização administrativa.

A auditoria nunca pode virar caminho alternativo para vazar dados.

## 19. DTOs

Criar:

``` text
EventoAuditoriaResumoResponse
EventoAuditoriaDetalheResponse
AlteracaoCampoResponse
AuditoriaFiltro
```

Não expor entidade JPA.

## 20. Retenção e performance

Não apagar eventos automaticamente nesta task.

Documentar futura política de retenção/arquivamento.

Priorizar consistência. Não usar processamento assíncrono que possa
perder eventos críticos sem mecanismo confiável.

Snapshots devem conter somente campos relevantes.

## 21. Cenários obrigatórios

### Produção

``` text
Registro 100
quantidade: 500 → 550
```

Esperado:

``` text
tipoRecurso = REGISTRO_PRODUCAO
recursoId = 100
acao = ALTERACAO
before.quantidade = 500
after.quantidade = 550
ator = usuário autenticado
```

### Meta

``` text
20.000 peças/dia → 22.000 peças/dia
```

Auditar mudança e vigência conforme regra real.

### Valor industrial

``` text
R$ 0,85 → nova vigência R$ 0,90
```

Auditar encerramento/criação conforme domínio. Usuário sem permissão
financeira não pode obter os valores pelo histórico.

### Senha

Registrar `ALTERACAO_SENHA`, mas confirmar por teste que senha/hash
antigo e novo nunca aparecem.

### Permissão

Registrar concessão/remoção de, por exemplo:

``` text
FINANCEIRO_INDUSTRIAL_VISUALIZAR
```

com ator, alvo e data.

### Importação

``` text
producao-pintura.xlsx
100 linhas
98 sucesso
2 erros
```

Deve ser possível localizar lote e registros relacionados.

## 22. Teste de rollback

Simular: 1. alteração iniciada; 2. auditoria preparada; 3. erro antes do
commit.

Esperado:

``` text
nem alteração
nem evento de sucesso persistidos
```

## 23. Testes de segurança

Criar Empresa A/B e validar isolamento.

Gestor Pintura não pode consultar eventos de Laser por lista, filtro ou
evento individual.

Pesquisar snapshots persistidos e confirmar ausência de termos/campos
sensíveis:

``` text
password
senha
senhaHash
token
secret
authorization
```

## 24. Testes adicionais

Validar: - paginação; - ordenação; - filtros; - acentos; - null; -
enum; - BigDecimal; - datas; - correlationId; - snapshot before/after; -
usuário inativado após evento; - recurso posteriormente inativado; -
origem manual/importação/sistema.

## 25. Integração com relatórios

Não alterar cálculos da TASK 11.

A auditoria servirá futuramente para explicar mudanças nos dados
utilizados pelos relatórios.

## 26. Preparação para RAG

A trilha deverá futuramente permitir perguntas como:

``` text
Quem alterou a meta da Pintura?
Quando o valor da peça 54871 mudou?
Por que o tempo padrão atual é diferente do anterior?
Quais registros vieram da importação X?
```

Mas NÃO implementar IA/RAG nesta task.

## 27. Fora do escopo

NÃO implementar: - frontend/timeline visual; - IA/RAG; - blockchain; -
assinatura digital; - SIEM; - alertas; - retenção legal automática; -
arquivamento externo; - auditoria de cada GET; - captura indiscriminada
de request/response; - armazenamento de senha/token.

## 28. Regressão e comandos

Todos os testes das TASKS 01--11 devem continuar passando.

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar também a validação oficial do OpenSpec disponível na instalação
atual.

## 29. Proibições

É proibido: - gravar senha/hash/token; - confiar em usuarioId enviado
pelo cliente; - permitir alteração/exclusão comum da auditoria; -
registrar rollback como sucesso; - vazar outra empresa/setor; - vazar
financeiro; - serializar entidade inteira sem sanitização; - retornar
histórico sem paginação; - usar log como única auditoria; - desabilitar
testes; - editar migrations antigas; - concluir com build quebrado.

## 30. Critérios de aceite

Concluir somente quando: 1. EventoAuditoria for persistido; 2. empresa e
ator forem capturados corretamente; 3. ação/origem/recurso/ID forem
registrados; 4. before/after funcionarem; 5. senha/token nunca forem
registrados; 6. produção/paradas forem auditadas; 7. metas/tempos padrão
forem auditados; 8. valores industriais forem auditados; 9.
usuários/permissões forem auditados; 10. importações forem
correlacionáveis; 11. rollback não gerar falso evento; 12. consulta
paginada funcionar; 13. histórico por recurso funcionar; 14. isolamento
empresarial funcionar; 15. escopo setorial funcionar; 16. financeiro
respeitar permissão; 17. eventos forem imutáveis via API normal; 18.
testes de segurança passarem; 19. regressão passar; 20. `clean test` e
`build` passarem; 21. OpenSpec refletir a implementação.

## 31. Entrega esperada do Codex

Ao concluir, apresentar: - change/proposal/design/tasks/specs e
validação OpenSpec; - estratégia arquitetural da auditoria; -
transações; - contexto autenticado; - snapshots e sanitização; -
migration/tabela/índices; - endpoints/filtros/paginação/permissões; -
testes de produção, meta, valor, senha, permissão, importação, rollback,
isolamento e regressão; - resultados de `clean test` e `build`; -
pendências reais.

Não iniciar a próxima task.

## 32. Próxima etapa prevista

Após aprovação:

``` text
TASK 13 — Qualidade dos Dados, Validações Avançadas e Confiabilidade Analítica
```

Ela deverá detectar automaticamente:

``` text
produção sem tempo padrão
produção sem valor configurado
horários suspeitos
dados incompletos
possíveis duplicidades
baixa cobertura analítica
inconsistências de cadastro
```

O objetivo é impedir que o proprietário receba um Dashboard bonito
baseado em dados ruins.

Depois seguiremos para consolidação das APIs finais do Dashboard e
preparação da camada de IA/RAG.

**O frontend continuará bloqueado até o backend estar completo,
confiável, testado e estabilizado.**
