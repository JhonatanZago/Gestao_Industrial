# TASK EXECUTÁVEL 10 --- Usuários, Autenticação, Perfis e Permissões

## Instrução principal

Implemente integralmente esta task no projeto **Sistema de Gestão e
Inteligência de Produção da Metalúrgica**, utilizando **OpenSpec +
Codex**.

Executar somente após a conclusão e estabilização das TASKS 01--09.

Antes de alterar código:

1.  leia integralmente o projeto atual;
2.  leia todas as specs e changes OpenSpec existentes;
3.  identifique a arquitetura e versões reais do Spring Boot/Spring
    Security;
4.  execute todos os testes existentes;
5.  confirme que o build está estável;
6.  preserve regras e contratos anteriores;
7.  crie a nova change OpenSpec;
8.  implemente segurança por camadas;
9.  crie testes de autenticação e autorização;
10. execute regressão completa.

**Não implementar frontend.** **Não criar tela de login.** **Não
implementar OAuth social nesta task.** **Não iniciar a próxima task
automaticamente.**

------------------------------------------------------------------------

# 1. Objetivo

Implementar a camada de identidade e controle de acesso do sistema.

O sistema possui dados sensíveis e diferentes níveis de informação:

``` text
produção
funcionários
peças
máquinas
metas
desempenho
valor industrial
analytics
importações
```

Nem todo usuário poderá acessar tudo.

O backend deve responder:

``` text
Quem está fazendo a requisição?
A qual empresa pertence?
Qual perfil possui?
Quais setores pode acessar?
Qual ação pode executar?
Pode visualizar dados financeiros?
Pode visualizar funcionários?
Pode alterar cadastros?
Pode importar planilhas?
Pode acessar analytics?
```

------------------------------------------------------------------------

# 2. OpenSpec

Criar change:

``` text
usuarios-autenticacao-permissoes
```

Capacidades sugeridas:

``` text
usuarios
autenticacao
autorizacao
perfis-permissoes
escopo-setorial
```

O `design.md` deve documentar:

-   modelo de identidade;
-   autenticação;
-   hash de senha;
-   tokens/sessão;
-   expiração;
-   autorização;
-   isolamento por empresa;
-   perfis;
-   permissões;
-   escopo por setor;
-   proteção de endpoints;
-   tratamento de 401/403;
-   estratégia de testes.

------------------------------------------------------------------------

# 3. Princípio fundamental

Nunca confiar em:

``` text
empresaId
setorId
perfil
permissão
```

apenas porque vieram do cliente.

A identidade autenticada deve ser a fonte de verdade para autorização.

Exemplo:

``` text
JWT diz:
usuario = 25
empresa = 1
```

O usuário não pode simplesmente chamar:

``` http
/api/v1/empresas/2/...
```

e acessar outra empresa.

------------------------------------------------------------------------

# 4. Modelo Usuario

Criar entidade equivalente a:

``` text
Usuario
```

Campos mínimos conceituais:

``` text
id
empresa
nome
email
senhaHash
status
ultimoLoginEm?
createdAt
updatedAt
```

Não armazenar senha em texto puro.

------------------------------------------------------------------------

# 5. E-mail

Normalizar e-mail de maneira consistente.

Definir unicidade.

Preferência inicial:

``` text
email único globalmente
```

Se o domínio real exigir mesmo e-mail em empresas diferentes, documentar
e utilizar unicidade por empresa.

Não alterar essa regra silenciosamente.

------------------------------------------------------------------------

# 6. Status do usuário

Criar enum técnico estável, por exemplo:

``` text
ATIVO
INATIVO
BLOQUEADO
```

Usuário inativo/bloqueado não pode autenticar.

------------------------------------------------------------------------

# 7. Senha

Utilizar algoritmo de hash apropriado suportado pelo Spring Security.

Preferência:

``` text
BCrypt
```

ou algoritmo moderno equivalente já padronizado no projeto.

Nunca:

``` text
MD5
SHA-1 simples
Base64
criptografia reversível
texto puro
```

------------------------------------------------------------------------

# 8. Política mínima de senha

Criar política configurável e razoável.

Exemplo inicial:

``` text
mínimo 8 caracteres
```

Não criar regras exageradas sem necessidade.

Nunca registrar senha em logs.

------------------------------------------------------------------------

# 9. Perfil

Criar conceito equivalente a:

``` text
Perfil
```

Perfis iniciais recomendados:

``` text
ADMINISTRADOR
GESTOR
OPERACIONAL
```

Não hardcodar toda autorização somente em `if role == ...`.

Perfis devem agrupar permissões.

------------------------------------------------------------------------

# 10. Administrador / Dono

O perfil administrativo deve poder receber permissões para:

``` text
gerenciar usuários
gerenciar empresa
gerenciar setores
gerenciar funcionários
gerenciar peças
gerenciar máquinas
gerenciar turnos
registrar/importar produção
visualizar analytics
visualizar desempenho
visualizar valores financeiros industriais
gerenciar metas
gerenciar tempos padrão
gerenciar valores
consultar relatórios
```

O nome técnico pode ser `ADMINISTRADOR`.

Não é necessário criar perfil separado `DONO` agora, salvo se o domínio
atual justificar.

------------------------------------------------------------------------

# 11. Gestor

O gestor poderá ter acesso limitado aos setores autorizados.

Exemplo:

``` text
Gestor A
├── Pintura
├── KTL
└── Zincagem
```

Ele não deve visualizar automaticamente:

``` text
Solda
Laser
Estamparia
```

se não possuir autorização.

------------------------------------------------------------------------

# 12. Operacional

Perfil operacional deve possuir conjunto mínimo de permissões.

Exemplo:

``` text
visualizar resumo autorizado
consultar informações operacionais permitidas
```

Não conceder automaticamente:

``` text
financeiro
gestão de usuários
configurações
metas
valores
dados completos de outros funcionários
```

------------------------------------------------------------------------

# 13. Permissões granulares

Criar conceito equivalente a:

``` text
Permissao
```

Utilizar códigos técnicos estáveis.

Exemplos:

``` text
USUARIO_VISUALIZAR
USUARIO_GERENCIAR

SETOR_VISUALIZAR
SETOR_GERENCIAR

FUNCIONARIO_VISUALIZAR
FUNCIONARIO_GERENCIAR

PECA_VISUALIZAR
PECA_GERENCIAR

MAQUINA_VISUALIZAR
MAQUINA_GERENCIAR

PRODUCAO_VISUALIZAR
PRODUCAO_REGISTRAR
PRODUCAO_IMPORTAR

PARADA_VISUALIZAR
PARADA_GERENCIAR

META_VISUALIZAR
META_GERENCIAR

TEMPO_PADRAO_VISUALIZAR
TEMPO_PADRAO_GERENCIAR

ANALYTICS_VISUALIZAR
DESEMPENHO_VISUALIZAR

FINANCEIRO_INDUSTRIAL_VISUALIZAR
VALOR_PRODUCAO_GERENCIAR

RELATORIO_VISUALIZAR
```

Ajustar nomes à convenção real do projeto.

------------------------------------------------------------------------

# 14. Perfil x Permissão

Modelar relação:

``` text
Perfil
   N:N
Permissao
```

ou solução equivalente robusta.

Evitar duplicar permissões manualmente em cada usuário quando um perfil
resolver.

------------------------------------------------------------------------

# 15. Usuário x Perfil

Nesta primeira versão, definir se:

``` text
1 usuário = 1 perfil
```

ou:

``` text
1 usuário = N perfis
```

Preferência inicial: um perfil principal por usuário, salvo necessidade
real já existente.

Projetar sem impedir evolução futura.

------------------------------------------------------------------------

# 16. Escopo por setor

Criar associação equivalente a:

``` text
UsuarioSetor
```

ou:

``` text
Usuario ↔ Setor
```

para limitar acesso operacional.

Exemplo:

``` text
Usuário 12:
Pintura
KTL
```

O backend deve aplicar o escopo.

------------------------------------------------------------------------

# 17. Administrador e escopo

Definir explicitamente se administrador possui:

``` text
todos os setores da própria empresa
```

sem precisar cadastrar cada associação.

Preferência: administrador com permissão global dentro da própria
empresa.

Nunca global entre empresas.

------------------------------------------------------------------------

# 18. Autenticação

Implementar endpoint:

``` http
POST /api/v1/auth/login
```

Request:

``` json
{
  "email": "usuario@empresa.com",
  "senha": "********"
}
```

Response conceitual:

``` json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "usuario": {
    "id": 1,
    "nome": "Administrador",
    "email": "usuario@empresa.com",
    "empresaId": 1,
    "perfil": "ADMINISTRADOR"
  }
}
```

Não retornar senha/hash.

------------------------------------------------------------------------

# 19. Estratégia de token

Utilizar estratégia stateless com token assinado, preferencialmente JWT,
se compatível com a arquitetura atual.

O token deve possuir somente claims necessárias.

Exemplo:

``` text
sub = usuarioId
empresaId
perfil/capacidades mínimas quando apropriado
iat
exp
```

Não colocar dados sensíveis desnecessários no JWT.

------------------------------------------------------------------------

# 20. Assinatura do JWT

A chave/secret MUST vir de configuração segura:

``` text
variável de ambiente
secret manager futuramente
```

Nunca versionar segredo real no Git.

Criar exemplo de configuração sem credencial real, quando necessário.

------------------------------------------------------------------------

# 21. Expiração

Access token deve expirar.

Valor configurável.

Exemplo inicial:

``` text
60 minutos
```

Não criar token permanente.

------------------------------------------------------------------------

# 22. Refresh token

Avaliar a arquitetura.

Se refresh token for implementado, deve possuir:

-   expiração;
-   revogação;
-   rotação quando apropriado;
-   armazenamento seguro de referência/hash.

Se isso aumentar excessivamente o escopo, documentar e deixar refresh
token para change específica.

NÃO criar refresh inseguro apenas para "ter a funcionalidade".

------------------------------------------------------------------------

# 23. Logout

Em autenticação puramente stateless com access token sem blacklist,
logout pode ser inicialmente responsabilidade do cliente descartando o
token.

Documentar a limitação.

Se houver refresh token persistido, implementar revogação no logout.

------------------------------------------------------------------------

# 24. Endpoint do usuário autenticado

Implementar:

``` http
GET /api/v1/auth/me
```

Retornar:

``` text
id
nome
email
empresa
perfil
permissoes
setores autorizados
```

Não retornar senha/hash.

------------------------------------------------------------------------

# 25. Criação do primeiro administrador

Resolver o bootstrap.

Não deixar endpoint público permanente para qualquer pessoa criar
administrador.

Estratégias aceitáveis:

``` text
seed controlado de desenvolvimento
bootstrap por configuração
migration apenas de estrutura + comando/setup explícito
```

Credenciais reais nunca devem estar versionadas.

Documentar.

------------------------------------------------------------------------

# 26. Cadastro de usuários

Endpoint administrativo:

``` http
POST /api/v1/empresas/{empresaId}/usuarios
```

Somente usuário autorizado.

Permitir definir:

``` text
nome
email
senha inicial
perfil
setores autorizados
```

Validar tudo contra a empresa autenticada.

------------------------------------------------------------------------

# 27. APIs de usuários

Implementar, conforme necessário:

``` http
POST   /api/v1/empresas/{empresaId}/usuarios
GET    /api/v1/empresas/{empresaId}/usuarios
GET    /api/v1/empresas/{empresaId}/usuarios/{id}
PUT    /api/v1/empresas/{empresaId}/usuarios/{id}
PATCH  /api/v1/empresas/{empresaId}/usuarios/{id}/ativar
PATCH  /api/v1/empresas/{empresaId}/usuarios/{id}/desativar
```

Listagem paginada.

------------------------------------------------------------------------

# 28. Alteração de senha

Implementar endpoint autenticado equivalente a:

``` http
PATCH /api/v1/auth/senha
```

Exigir:

``` text
senhaAtual
novaSenha
```

Validar senha atual antes de alterar.

Após alteração, definir estratégia de invalidação/revogação compatível
com tokens implementados.

------------------------------------------------------------------------

# 29. Reset administrativo de senha

Se implementado:

``` http
PATCH /api/v1/empresas/{empresaId}/usuarios/{id}/reset-senha
```

deve exigir permissão administrativa.

Preferir senha temporária/fluxo seguro em vez de expor senha existente.

Nunca existe "recuperar senha atual", pois só há hash.

------------------------------------------------------------------------

# 30. Proteção dos endpoints existentes

Revisar TODAS as APIs das TASKS 01--09.

Classificar cada endpoint por permissão.

Nenhum endpoint sensível deve permanecer público acidentalmente.

------------------------------------------------------------------------

# 31. Segurança em camada de serviço

Não confiar somente em esconder rotas no futuro frontend.

A autorização MUST ocorrer no backend.

Utilizar Spring Security e, quando apropriado:

``` java
@PreAuthorize(...)
```

ou mecanismo equivalente.

Não espalhar expressões gigantes e duplicadas.

------------------------------------------------------------------------

# 32. Isolamento por empresa

Cenário obrigatório:

``` text
Usuário Empresa A
token válido
```

Tenta:

``` http
GET /api/v1/empresas/{empresaB}/analytics/resumo
```

Esperado:

``` text
403 Forbidden
```

ou política equivalente consistente.

Nunca retornar dados da Empresa B.

------------------------------------------------------------------------

# 33. Escopo de setor nos analytics

Se gestor possui somente:

``` text
Pintura
KTL
```

uma consulta geral de analytics não pode vazar:

``` text
Solda
Laser
```

O escopo deve ser aplicado na consulta, não apenas filtrado depois no
JSON quando isso puder causar vazamento/performance inadequada.

------------------------------------------------------------------------

# 34. Escopo explícito solicitado

Se o gestor de Pintura chamar:

``` http
...?setorId=LASER
```

deve receber:

``` text
403
```

ou erro de acesso equivalente.

Não retornar silenciosamente dados proibidos.

------------------------------------------------------------------------

# 35. Funcionários

A permissão de visualizar funcionário deve ser separada de gerenciar
funcionário.

Além disso, escopo setorial deve ser considerado quando aplicável.

------------------------------------------------------------------------

# 36. Financeiro industrial

Endpoints:

``` text
/analytics/financeiro/**
/valores-producao/**
```

devem possuir permissões próprias.

Um usuário que pode visualizar produção não deve automaticamente
visualizar valores financeiros.

------------------------------------------------------------------------

# 37. Importação Excel

Endpoint de importação deve exigir:

``` text
PRODUCAO_IMPORTAR
```

e acesso ao setor alvo.

------------------------------------------------------------------------

# 38. Metas e tempos padrão

Separar:

``` text
visualizar
gerenciar
```

quando apropriado.

------------------------------------------------------------------------

# 39. Respostas HTTP

Padronizar:

``` text
401 Unauthorized
```

quando não autenticado/token inválido.

``` text
403 Forbidden
```

quando autenticado, mas sem permissão.

Não transformar tudo em `400`.

------------------------------------------------------------------------

# 40. Mensagens seguras no login

Não revelar excessivamente:

``` text
“este e-mail existe mas a senha está errada”
```

Preferir:

``` text
Credenciais inválidas.
```

Isso reduz enumeração de contas.

------------------------------------------------------------------------

# 41. Rate limiting

Não implementar infraestrutura complexa nesta task se não existir
suporte.

Registrar como recomendação futura para:

``` text
login
reset de senha
```

Não simular proteção inexistente.

------------------------------------------------------------------------

# 42. CORS

Configurar de maneira segura e preparada para frontend futuro.

Em desenvolvimento, origem configurável.

Não usar:

``` text
*
```

com credenciais de forma insegura.

------------------------------------------------------------------------

# 43. CSRF

Se a API for stateless com Bearer token e sem autenticação por cookie,
documentar decisão de CSRF conforme arquitetura do Spring Security.

Não desabilitar segurança sem justificativa.

------------------------------------------------------------------------

# 44. Headers e erros

Não expor:

-   stack trace;
-   segredo JWT;
-   senha;
-   hash;
-   detalhes internos sensíveis.

Reutilizar `@RestControllerAdvice`.

------------------------------------------------------------------------

# 45. Logs de segurança

Registrar eventos úteis:

``` text
login bem-sucedido
login rejeitado
usuário bloqueado/inativo
alteração de senha
criação/desativação de usuário
acesso proibido relevante
```

Não registrar:

``` text
senha
token completo
secret
```

------------------------------------------------------------------------

# 46. Auditoria futura

Preparar o contexto de segurança para que tasks futuras consigam
registrar:

``` text
quem alterou
quando
qual empresa
```

Disponibilizar `usuarioId` autenticado de forma limpa para camada de
auditoria.

------------------------------------------------------------------------

# 47. Migrations

Criar migrations novas para estruturas equivalentes a:

``` text
usuario
perfil
permissao
perfil_permissao
usuario_setor
```

e demais tabelas realmente necessárias.

Não editar migrations antigas.

------------------------------------------------------------------------

# 48. Seeds técnicos

Permissões e perfis-base podem ser inseridos por migration/seed
idempotente conforme padrão do projeto.

Não inserir:

``` text
senha real
token
secret
e-mail pessoal real
```

no repositório.

------------------------------------------------------------------------

# 49. Constraints

Criar constraints para:

-   e-mail;
-   FKs;
-   perfil;
-   empresa;
-   associações duplicadas;
-   usuário/setor;
-   status.

------------------------------------------------------------------------

# 50. Testes de senha

Validar:

-   senha nunca persiste em texto puro;
-   hash muda adequadamente;
-   senha correta autentica;
-   senha incorreta falha;
-   usuário inativo falha;
-   usuário bloqueado falha.

------------------------------------------------------------------------

# 51. Testes de login

Testar:

``` text
credenciais válidas → 200
senha inválida → 401
e-mail inexistente → 401
usuário inativo → 401/403 conforme política documentada
token expirado → 401
token inválido → 401
sem token → 401
```

------------------------------------------------------------------------

# 52. Testes de isolamento empresarial

Criar:

``` text
Empresa A
Empresa B
Admin A
Admin B
```

Admin A não pode acessar recursos da B.

Testar em múltiplos módulos:

``` text
funcionários
produção
analytics
financeiro
metas
importações
```

------------------------------------------------------------------------

# 53. Testes de perfil

Criar:

``` text
ADMINISTRADOR
GESTOR
OPERACIONAL
```

Validar matriz de permissões.

------------------------------------------------------------------------

# 54. Testes de setor

Gestor:

``` text
Pintura
KTL
```

Validar:

``` text
Pintura → permitido
KTL → permitido
Laser → proibido
```

Também validar analytics agregados para impedir vazamento indireto.

------------------------------------------------------------------------

# 55. Testes financeiros

Usuário com:

``` text
ANALYTICS_VISUALIZAR
```

mas sem:

``` text
FINANCEIRO_INDUSTRIAL_VISUALIZAR
```

não pode acessar analytics financeiro.

------------------------------------------------------------------------

# 56. Testes de importação

Usuário sem:

``` text
PRODUCAO_IMPORTAR
```

não pode importar Excel.

Usuário com permissão, mas sem acesso ao setor, também não pode.

------------------------------------------------------------------------

# 57. Testes de gerenciamento

Operacional não pode:

``` text
criar usuário
alterar perfil
gerenciar valores
alterar meta
```

salvo permissão explicitamente concedida.

------------------------------------------------------------------------

# 58. Testes com MockMvc/Security

Utilizar ferramentas compatíveis com o projeto para testar segurança no
nível HTTP e, quando necessário, Services.

Não testar apenas métodos isolados ignorando filtros Spring Security.

------------------------------------------------------------------------

# 59. Documentação OpenAPI

Se Swagger/OpenAPI estiver presente:

-   documentar Bearer authentication;
-   marcar endpoints protegidos;
-   manter `/auth/login` acessível;
-   proteger adequadamente documentação em produção conforme estratégia
    futura/configuração.

Não expor secrets.

------------------------------------------------------------------------

# 60. Configuração por ambiente

Configurações sensíveis devem vir de ambiente:

``` text
JWT_SECRET
JWT_EXPIRATION
CORS_ALLOWED_ORIGINS
```

Criar exemplo seguro quando apropriado.

Nunca commitar segredo real.

------------------------------------------------------------------------

# 61. Dependências

Adicionar somente dependências necessárias e compatíveis com a versão
real do Spring Boot.

Não copiar configuração antiga de Spring Security incompatível com a
versão atual.

------------------------------------------------------------------------

# 62. Performance

Evitar consultar banco repetidamente para a mesma informação de
autorização dentro de uma única requisição sem necessidade.

Entretanto, não sacrificar segurança por micro-otimização.

Cache de permissões pode ser avaliado futuramente.

------------------------------------------------------------------------

# 63. Princípio do menor privilégio

Por padrão:

``` text
não permitido
```

Acesso deve ser concedido explicitamente.

Não utilizar lógica em que endpoints novos ficam públicos até alguém
lembrar de protegê-los.

------------------------------------------------------------------------

# 64. Configuração default-deny

Configurar Spring Security de forma que apenas endpoints explicitamente
públicos sejam liberados.

Públicos mínimos:

``` text
login
health técnico quando apropriado
documentação apenas conforme ambiente
```

Demais endpoints exigem autenticação.

------------------------------------------------------------------------

# 65. Fora do escopo

NÃO implementar:

-   frontend;
-   tela de login;
-   cadastro público;
-   login Google/Microsoft;
-   SSO;
-   LDAP;
-   MFA;
-   biometria;
-   recuperação por e-mail;
-   envio de e-mail;
-   rate limiter distribuído;
-   gateway;
-   OAuth Authorization Server próprio;
-   RAG;
-   IA.

------------------------------------------------------------------------

# 66. Regressão

Todos os testes das TASKS 01--09 devem continuar passando.

Como a segurança passará a proteger endpoints, atualizar testes
existentes de integração para autenticar corretamente.

NÃO desabilitar segurança nos testes apenas para fazê-los passar.

------------------------------------------------------------------------

# 67. Comandos obrigatórios

No Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar validação OpenSpec oficial disponível.

------------------------------------------------------------------------

# 68. Não mascarar falhas

É proibido:

-   senha em texto puro;
-   secret no Git;
-   token permanente;
-   `permitAll()` global;
-   desabilitar segurança nos testes;
-   confiar somente no `empresaId` da URL;
-   confiar somente no frontend;
-   permitir acesso financeiro por consequência de acesso à produção;
-   retornar hash de senha;
-   logar token completo;
-   misturar empresas;
-   editar migration antiga;
-   considerar task concluída com testes/build falhando.

------------------------------------------------------------------------

# 69. Critérios de aceite

A task somente estará concluída quando:

1.  usuários puderem ser cadastrados por usuário autorizado;
2.  senha for armazenada com hash seguro;
3.  login funcionar;
4.  token possuir expiração;
5.  `/auth/me` funcionar;
6.  usuário inativo/bloqueado não autenticar;
7.  perfis existirem;
8.  permissões granulares existirem;
9.  perfil-permissão funcionar;
10. escopo por setor funcionar;
11. administrador ficar restrito à própria empresa;
12. gestor ficar restrito aos setores autorizados;
13. operacional possuir acesso mínimo;
14. financeiro possuir permissão separada;
15. importação possuir permissão separada;
16. endpoints antigos estiverem protegidos;
17. 401/403 estiverem corretos;
18. alteração de senha funcionar;
19. secrets vierem de configuração;
20. logs não vazarem credenciais;
21. isolamento entre empresas estiver testado;
22. analytics não vazarem setores;
23. testes de segurança passarem;
24. testes anteriores passarem;
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

## Segurança

Explicar: - autenticação; - hash; - JWT/token; - expiração; -
default-deny; - 401/403; - CORS/CSRF.

## Autorização

Apresentar matriz:

``` text
perfil × permissão × escopo
```

## Banco

-   migrations;
-   tabelas;
-   constraints;
-   seeds técnicos.

## Endpoints

Listar: - públicos; - autenticados; - administrativos; - permissões
requeridas.

## Testes

Informar: - login; - token; - senha; - perfis; - permissões; -
empresa; - setor; - financeiro; - importação; - regressão.

## Build

Informar resultados de:

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
TASK 11 — Relatórios Gerenciais, Resumos Executivos e Exportações
```

Ela utilizará toda a base já construída:

``` text
PRODUÇÃO
+ PARADAS
+ KPIs
+ METAS
+ DESEMPENHO
+ VALOR INDUSTRIAL
+ PERMISSÕES
        ↓
RELATÓRIO DO PROPRIETÁRIO
```

O objetivo será produzir relatórios de períodos como:

``` text
Resumo executivo
Produção total
Comparativo com período anterior
Setores
Peças
Máquinas
Paradas
Metas
Desempenho
Valor industrial
Pontos de atenção
```

sempre respeitando as permissões do usuário autenticado.

**O frontend continuará fora do escopo até o backend estar completo,
testado e estabilizado.**
