# Sistema de Gestão e Inteligência de Produção

Backend inicial para digitalizar a gestão de produção de uma metalúrgica, substituindo registros dispersos em planilhas por uma base evolutiva de empresas e setores.

## Paradas e tempos produtivos

Motivos de parada sao cadastraveis por Empresa, com codigo unico dentro da empresa e ativacao/desativacao logica:

- `POST|GET /api/v1/empresas/{empresaId}/motivos-parada`
- `GET|PUT /api/v1/empresas/{empresaId}/motivos-parada/{motivoId}`
- `PATCH /api/v1/empresas/{empresaId}/motivos-parada/{motivoId}/ativar|desativar`

Paradas pertencem a uma producao e a um motivo da mesma Empresa:

- `POST|GET /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas`
- `GET|PUT /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}`
- `PATCH /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}/ativar|desativar`

O intervalo deve ser positivo, estar contido na producao e nao se sobrepor a outra parada ativa; intervalos adjacentes sao permitidos. A resposta de producao informa `duracaoBrutaMinutos`, `tempoParadoMinutos` e `tempoProdutivoMinutos`, calculado como `max(0, bruto - parado)`. Historicos permanecem consultaveis apos inativacao e nao ha exclusao fisica.

## Stack

- Java 17 / Spring Boot
- Gradle Groovy
- Spring Web MVC, Spring Data JPA e Jakarta Validation
- PostgreSQL em runtime e H2 nos testes
- Flyway para migrations
- JUnit 5, Mockito e MockMvc

## Executar

Configure PostgreSQL e, se necessário, as variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. Os valores padrão apontam para `localhost:5432/sistema_gestao_industrial`, usuário `postgres` e senha `postgres`.

```powershell
.\gradlew.bat bootRun
.\gradlew.bat clean test
.\gradlew.bat build
```

As migrations ficam em `src/main/resources/db/migration` e são aplicadas automaticamente pelo Flyway. O perfil `test` usa um banco H2 em memória.

## Estrutura inicial

O backend separa domínio, DTOs, mappers, repositories, services, resources e exceções. A entidade `Empresa` é a raiz organizacional; cada `Setor` pertence a exatamente uma empresa e usa ativação/desativação lógica.

Os setores industriais são cadastráveis dinamicamente e não são definidos através de enum fixo. Assim, códigos como `PINT`, `SOLDA`, `KTL`, `LASER` ou um novo `USIN` podem ser cadastrados sem alteração de código.

## Endpoints

Empresas:

- `POST /api/v1/empresas`
- `GET /api/v1/empresas?page=0&size=20&ativo=true`
- `GET /api/v1/empresas/{id}`
- `PUT /api/v1/empresas/{id}`
- `PATCH /api/v1/empresas/{id}/ativar`
- `PATCH /api/v1/empresas/{id}/desativar`

Setores:

- `POST /api/v1/empresas/{empresaId}/setores`
- `GET /api/v1/empresas/{empresaId}/setores?page=0&size=20&codigo=PINT`
- `GET /api/v1/empresas/{empresaId}/setores/{setorId}`
- `PUT /api/v1/empresas/{empresaId}/setores/{setorId}`
- `PATCH /api/v1/empresas/{empresaId}/setores/{setorId}/ativar`
- `PATCH /api/v1/empresas/{empresaId}/setores/{setorId}/desativar`

Funcionários:

- `POST /api/v1/empresas/{empresaId}/funcionarios`
- `GET /api/v1/empresas/{empresaId}/funcionarios?page=0&size=20&codigo=00147`
- `GET /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}`
- `PUT /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}`
- `PATCH /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}/ativar`
- `PATCH /api/v1/empresas/{empresaId}/funcionarios/{funcionarioId}/desativar`

Peças:

- `POST /api/v1/empresas/{empresaId}/pecas`
- `GET /api/v1/empresas/{empresaId}/pecas?page=0&size=20&codigo=54871`
- `GET /api/v1/empresas/{empresaId}/pecas/{pecaId}`
- `PUT /api/v1/empresas/{empresaId}/pecas/{pecaId}`
- `PATCH /api/v1/empresas/{empresaId}/pecas/{pecaId}/ativar`
- `PATCH /api/v1/empresas/{empresaId}/pecas/{pecaId}/desativar`

Registro de Produção:

- `POST /api/v1/empresas/{empresaId}/producoes`
- `GET /api/v1/empresas/{empresaId}/producoes?page=0&size=20&funcionarioId=10&ativo=true`
- `GET /api/v1/empresas/{empresaId}/producoes/{producaoId}`
- `PUT /api/v1/empresas/{empresaId}/producoes/{producaoId}`
- `PATCH /api/v1/empresas/{empresaId}/producoes/{producaoId}/ativar`
- `PATCH /api/v1/empresas/{empresaId}/producoes/{producaoId}/desativar`

O payload de produção informa `setorId`, `funcionarioId`, `pecaId`, `quantidade`, `inicio`, `fim` e `observacao`. Os eventos usam `LocalDateTime` no fuso operacional documentado da planta (`America/Sao_Paulo`); a auditoria do sistema usa timestamps com offset. A resposta calcula `duracaoBrutaMinutos` sob demanda. Essa duração ainda não representa tempo produtivo, pois paradas ficam fora desta etapa.

Funcionários e peças pertencem obrigatoriamente a uma Empresa. Seus códigos são `String`: zeros à esquerda, letras e hífens são preservados, e a unicidade é garantida por Empresa. Nenhum deles possui setor fixo nesta etapa; o setor será informado futuramente no Registro de Produção.

Não há exclusão física nesta etapa. Funcionários, peças, produção, importação Excel, dashboards, autenticação e IA ficam para changes futuras.
