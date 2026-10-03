# TASK EXECUTÁVEL 18 --- Homologação, Testes E2E, Documentação e Fechamento do Backend V1

## Instrução principal

Execute esta task somente após a conclusão e estabilização das TASKS
01--17 do **Sistema de Gestão e Inteligência de Produção da
Metalúrgica**.

Esta é a **última task do Backend V1**. Não implemente novas
funcionalidades: prove que o sistema completo funciona de forma
integrada, segura, consistente, reproduzível e documentada.

Antes de alterar código: 1. Leia o projeto e todas as changes/specs
OpenSpec. 2. Execute a suíte existente e confirme o estado inicial. 3.
Inventarie funcionalidades, endpoints, permissões, configurações,
migrations, integrações e documentação. 4. Crie a change OpenSpec
`homologacao-backend-v1`. 5. Corrija apenas defeitos encontrados na
homologação. 6. Reexecute regressão após cada correção relevante. 7.
Gere evidências e relatório final GO/NO-GO.

**Não implementar frontend. Não adicionar features. Não mascarar testes.
Não declarar aprovação sem evidência. Não tornar a suíte dependente de
API paga ou internet. Não iniciar o frontend automaticamente.**

## 1. OpenSpec

Criar:

``` text
openspec/changes/homologacao-backend-v1/
```

Capabilities:

``` text
backend-acceptance
end-to-end-validation
release-documentation
deployment-readiness
```

Documentar escopo homologado, cenários, critérios, evidências, defeitos,
correções, limitações e resultado final.

## 2. Congelamento do Backend V1

Permitido:

``` text
correção de bug
correção de segurança
correção de migration
correção de contrato
correção de documentação
correção de teste
```

Fora do escopo:

``` text
nova feature
novo KPI
nova regra financeira
novo relatório
nova integração
novo fluxo de IA
frontend
```

## 3. Fluxo E2E oficial

Validar de ponta a ponta:

``` text
Login
→ Empresa/permissões
→ Cadastros industriais
→ Importação Excel
→ Produção
→ Paradas
→ Analytics
→ Metas
→ Desempenho
→ Valor industrial
→ Qualidade dos dados
→ Dashboard
→ Relatórios
→ Auditoria
→ Assistente IA/RAG
```

Cada etapa deve utilizar dados produzidos/validados pelas etapas
anteriores.

## 4. Massa sintética oficial

Criar massa determinística sem dados reais:

``` text
Empresa: Metalúrgica Homologação

Setores:
Pintura
Solda
KTL
Laser

Funcionários:
00147
00148
00149

Peças:
PEC-A
PEC-B
PEC-C

Máquinas:
PINT-01
SOLDA-01
LASER-01
```

Criar também uma segunda empresa para testes de isolamento.

## 5. Excel oficial de homologação

Criar fixture `.xlsx` compatível com o importador real contendo valores
conhecidos de:

``` text
funcionário
peça
quantidade
horário
setor
máquina, quando aplicável
```

Criar fixtures negativas:

``` text
arquivo vazio
formato inválido
coluna ausente
funcionário inválido
peça inválida
quantidade inválida
horário inválido
arquivo acima do limite
```

Validar upload → processamento → persistência → Analytics → Dashboard →
relatório.

## 6. Cenário matemático oficial

Período atual:

``` text
Produção = 10.000 peças
Tempo bruto = 600 min
Paradas = 60 min
Tempo produtivo = 540 min
Meta = 11.000
Valor industrial = BRL 8.500
```

Período anterior:

``` text
Produção = 8.000 peças
Paradas = 80 min
Valor industrial = BRL 7.000
```

Esperado:

``` text
produção = +25%
paradas = -25%
atingimento meta ≈ 90,91%
percentual parada = 10%
valor industrial ≈ +21,43%
```

Os valores devem coincidir entre Analytics, Dashboard, Relatório e
Assistente IA autorizado.

## 7. Autenticação e autorização

Homologar:

``` text
login válido/inválido
sem token
token inválido
token expirado
perfis
permissões
empresa
setor
financeiro
auditoria
```

Utilizar os perfis reais do projeto, equivalentes quando existentes a:

``` text
DONO
ADMINISTRADOR
GESTOR_SETOR
USUARIO_GERAL
```

## 8. Isolamento empresarial e setorial

Criar Empresa A e Empresa B.

Usuário A nunca acessa B, mesmo alterando IDs manualmente.

Gestor restrito a Pintura/KTL deve receber somente esses setores em
TODAS as agregações e módulos autorizados:

``` text
Dashboard
Analytics
funcionários
peças
máquinas
paradas
metas
desempenho
relatórios
qualidade
IA
RAG
```

Executar testes IDOR/BOLA.

## 9. Cadastros industriais

Homologar CRUDs realmente existentes:

``` text
setores
funcionários
peças
máquinas
turnos
demais cadastros implementados
```

Testar criação, consulta, alteração, inativação/exclusão conforme regra,
validação e duplicidade.

Confirmar que setores adicionais podem ser cadastrados sem alteração de
código quando essa for a arquitetura implementada.

## 10. Produção, paradas e Analytics

Validar filtros por:

``` text
empresa
setor
funcionário
peça
máquina
turno
período
```

Paradas:

``` text
ocorrências
motivos
duração
percentual
agregações
```

Não recriar fórmulas; validar as fórmulas já aprovadas.

## 11. Metas, desempenho e valor industrial

Homologar:

``` text
vigência
meta
atingimento
ausência de meta
tempo padrão
quantidade esperada
índice de desempenho
cobertura
valor unitário
moeda
agregação
permissão financeira
```

Ausência de meta/dado não deve virar zero artificialmente.

Nunca misturar moedas silenciosamente.

## 12. Qualidade dos dados

Validar:

``` text
cobertura
alertas
críticos
possíveis duplicidades
inconsistências
```

Os resultados devem corresponder à massa determinística.

## 13. Dashboard

Homologar:

``` http
GET /api/v1/empresas/{empresaId}/dashboard
```

Validar:

``` text
cards
comparativos
série temporal
setores
peças
máquinas
funcionários
paradas
metas
desempenho
financeiro autorizado
qualidade
```

Homologar drill-down de:

``` text
funcionário
setor
peça
máquina
```

## 14. Relatórios

Homologar somente formatos realmente implementados:

``` text
JSON
CSV
XLSX
PDF/HTML, se existirem
```

Para filtros iguais:

``` text
Analytics = Dashboard = Relatório
```

Não marcar formato inexistente como aprovado.

## 15. Auditoria

Executar operações auditáveis e validar:

``` text
quem
quando
ação
recurso
empresa
antes/depois, quando aplicável
```

Nenhum secret deve aparecer.

## 16. IA e RAG

Com fake/mock determinístico, testar:

``` text
Quanto a Pintura produziu?
Como o período atual ficou contra o anterior?
Qual foi o principal motivo de parada?
Quais peças o funcionário 00147 produziu?
```

A IA deve usar os mesmos números do backend.

Testar:

``` text
SEM_DADOS
empresa não autorizada
setor não autorizado
financeiro sem permissão
auditoria sem permissão
prompt injection
tool injection
```

Se RAG estiver implementado, indexar documento sintético de Pintura e
validar:

``` text
recuperação correta
fonte
empresa
setor
cross-company bloqueado
cross-sector bloqueado
```

## 17. Observabilidade e resiliência

Revalidar TASK 16:

``` text
health
liveness
readiness
metrics
correlation ID
logs
timeouts
retry/circuit breaker onde aplicável
```

Simular IA indisponível:

``` text
ERP principal continua operacional
IA fica degradada/indisponível
status e métricas permanecem coerentes
```

## 18. Segurança final

Reexecutar o hardening da TASK 17:

``` text
JWT
IDOR
CORS
CSRF
security headers
SQL injection
uploads
ZIP/XLSX
formula injection
path traversal
Actuator
secrets
prompt injection
RAG isolation
rate limits
```

Nenhum defeito crítico/alto impeditivo pode permanecer aberto.

## 19. Performance final

Reexecutar o cenário da TASK 17 usando a mesma massa/ambiente sempre que
possível.

Comparar:

``` text
baseline
resultado final
latência
taxa de erro
queries
pool
memória
```

Não tratar resultado de notebook local como SLA de produção.

## 20. Banco e migrations

Em banco vazio:

``` text
executar todas as migrations
subir aplicação
executar smoke test
```

Quando possível, validar no banco alvo real, por exemplo PostgreSQL, se
este for o banco definido para produção.

Validar:

``` text
FK
UNIQUE
NOT NULL
CHECK
índices
queries nativas
```

Nunca editar migration histórica para fazer homologação passar.

## 21. OpenAPI e contratos

Revisar todos os endpoints e documentar:

``` text
request
response
status
filtros
paginação
enums
segurança
erros
```

Confirmar padrão `/api/v1`.

Criar testes de contrato para endpoints críticos.

## 22. Configuração final

Documentar todas as variáveis reais usadas, com placeholders:

``` text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
AI_ENABLED
AI_PROVIDER
AI_MODEL
AI_API_KEY
APP_CORS_ALLOWED_ORIGINS
```

Adaptar aos nomes reais.

Revisar profiles:

``` text
dev
test
prod
```

Nenhum secret real deve estar versionado.

## 23. README final

Atualizar `README.md` com:

``` text
objetivo
arquitetura
stack
pré-requisitos
como executar
como testar
configuração
banco
OpenSpec
API
segurança
IA/RAG
documentação
```

Tudo deve refletir o código real.

## 24. Documentação obrigatória

Criar/revisar:

``` text
CHANGELOG.md
docs/HOMOLOGACAO_BACKEND_V1.md
docs/ARQUITETURA_BACKEND.md
docs/BANCO_DE_DADOS.md
docs/EXECUTAR_PROJETO.md
docs/DESENVOLVIMENTO.md
docs/DEPLOY_BACKEND.md
docs/OPERACAO_BACKEND.md
docs/SECURITY_HARDENING.md
docs/PERFORMANCE_BACKEND.md
docs/ASSISTENTE_IA_RAG.md
docs/IMPORTACAO_EXCEL.md
```

Somente documentar recursos realmente implementados.

## 25. Arquitetura final

Em `ARQUITETURA_BACKEND.md`, documentar as camadas reais e incluir
Mermaid.

Exemplo conceitual:

``` mermaid
flowchart TD
    Excel --> Importacao
    Importacao --> Producao
    Producao --> Analytics
    Analytics --> Dashboard
    Analytics --> Relatorios
    Dashboard --> Assistente
    Documentos --> RAG
    RAG --> Assistente
```

Ajustar ao código real.

## 26. Guia de execução

Criar passo a passo Windows equivalente a:

``` powershell
git clone <repositorio>
cd <projeto>
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

Adicionar banco/configuração conforme projeto real.

## 27. Guia de deploy

Documentar de forma agnóstica:

``` text
build
variáveis
banco
migrations
health
logs
backup
rollback
```

Não inventar AWS/Azure/GCP se nenhum provedor foi escolhido.

## 28. Coleção de API

Gerar coleção Postman ou equivalente com requests principais:

``` text
login
cadastros
importação
produção
Dashboard
relatórios
qualidade
auditoria
assistente
```

Criar environment de exemplo sem secrets.

## 29. Smoke test

Criar script/documentação reproduzível:

``` text
health
login
consulta básica
Dashboard
```

Deve permitir verificar rapidamente um deploy.

## 30. TODO/FIXME e dependências

Inventariar:

``` text
TODO
FIXME
HACK
```

Classificar:

``` text
resolver antes V1
aceitável pós-V1
```

Executar análise final de dependências/CVEs.

Nenhum TODO crítico de segurança/integridade pode permanecer sem decisão
explícita.

## 31. Relatório oficial de homologação

Criar:

``` text
docs/HOMOLOGACAO_BACKEND_V1.md
```

Incluir:

``` text
versão
data
ambiente
commit
escopo
cenários
resultados
falhas encontradas
correções
segurança
performance
limitações
pendências
decisão
```

Criar matriz:

``` text
Funcionalidade | Cenário | Resultado | Evidência
```

Resultados permitidos:

``` text
APROVADO
REPROVADO
NÃO APLICÁVEL
```

## 32. Defeitos

Ao encontrar bug:

``` text
registrar
classificar
corrigir
criar teste de regressão
reexecutar cenário
```

Severidade:

``` text
CRÍTICO
ALTO
MÉDIO
BAIXO
```

Backend V1 não pode receber GO com defeito CRÍTICO/ALTO impeditivo de
segurança, integridade ou fluxo principal.

## 33. GO / NO-GO

Resultado final deve ser explicitamente:

``` text
BACKEND V1: GO
```

somente se os critérios obrigatórios passarem.

Caso contrário:

``` text
BACKEND V1: NO-GO
```

e listar exatamente o que impede a homologação.

Nunca declarar GO automaticamente.

## 34. Release

Se e somente se o backend receber GO, preparar instruções para
release/tag:

``` text
backend-v1.0.0
```

Não executar publicação remota/destrutiva automaticamente.

## 35. Testes obrigatórios

Organizar e executar:

``` text
unitários
integração
contrato
segurança
E2E
performance
regressão
```

A suíte padrão não deve depender de internet/API paga.

## 36. Comandos obrigatórios

Windows:

``` powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

Executar também conforme ferramentas reais:

``` text
OpenSpec validation
migrations em banco limpo
E2E
security tests
performance profile
dependency scan
```

## 37. Critérios finais de aceite

Somente considerar concluído quando: 1. TASKS 01--17 estiverem
integradas; 2. fluxo E2E passar; 3. Excel válido/importação passarem; 4.
arquivos inválidos forem tratados; 5. Analytics estiver correto; 6.
metas/desempenho estiverem corretos; 7. financeiro estiver correto e
protegido; 8. qualidade estiver correta; 9. Dashboard coincidir com
Analytics; 10. drill-downs funcionarem; 11. relatórios coincidirem; 12.
auditoria funcionar; 13. IA estiver grounded; 14. IA não inventar
ausência; 15. RAG respeitar empresa/setor; 16. isolamento
empresarial/setorial passar; 17. IDOR passar; 18. observabilidade
funcionar; 19. migrations funcionarem do zero; 20. banco alvo estiver
validado quando disponível; 21. OpenAPI estiver atualizada; 22.
contratos críticos estiverem testados; 23. configurações estiverem
documentadas; 24. README estiver atualizado; 25. documentação
obrigatória existir; 26. coleção de API existir; 27. smoke test existir;
28. hardening estiver revisado; 29. performance estiver revisada; 30.
dependency scan estiver revisado; 31. TODOs críticos estiverem
resolvidos; 32. nenhum defeito CRÍTICO/ALTO impeditivo permanecer; 33.
`clean test` passar; 34. `build` passar; 35. OpenSpec passar; 36.
relatório oficial existir; 37. decisão GO/NO-GO tiver evidência.

## 38. Entrega esperada do Codex

Ao concluir, apresentar:

### Resultado

``` text
BACKEND V1: GO
```

ou:

``` text
BACKEND V1: NO-GO
```

### OpenSpec

-   change;
-   proposal;
-   design;
-   tasks;
-   specs;
-   validação.

### Homologação

-   fluxos E2E;
-   matriz de aceite;
-   evidências;
-   bugs encontrados/corrigidos.

### Segurança

-   resultado do hardening final;
-   pendências.

### Performance

-   baseline;
-   resultado final;
-   limitações.

### Banco

-   migrations;
-   banco limpo;
-   banco alvo;
-   constraints/índices.

### Documentação

Confirmar cada arquivo criado/revisado.

### API

-   OpenAPI;
-   coleção Postman/equivalente;
-   environment;
-   smoke test.

### Testes

Informar quantidade e resultado por categoria.

### Build

Informar resultado real:

``` text
.\gradlew.bat clean test
.\gradlew.bat build
```

### Defeitos

Separar:

``` text
CRÍTICOS
ALTOS
MÉDIOS
BAIXOS
```

Não iniciar o frontend.

## 39. Encerramento da fase

Se a decisão for:

``` text
BACKEND V1: GO
```

considerar concluída:

``` text
FASE 1 — BACKEND
```

A próxima fase será criada separadamente:

``` text
FASE 2 — FRONTEND PREMIUM DARK
```

Ela deverá consumir exclusivamente os contratos estabilizados do Backend
V1 e contemplar progressivamente:

``` text
Login
Dashboard Executivo
Setores
Produção
Funcionários
Peças
Máquinas
Importações
Paradas
Metas
Desempenho
Qualidade
Relatórios
Auditoria
Assistente Industrial IA/RAG
Configurações
```

**A primeira task do frontend só deve ser criada depois que esta TASK 18
estiver concluída e o Backend V1 receber GO.**
