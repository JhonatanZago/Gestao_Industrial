# Sistema de Gestão e Inteligência de Produção

Backend inicial para digitalizar a gestão de produção de uma metalúrgica, substituindo registros dispersos em planilhas por uma base evolutiva de empresas e setores.

## Paradas e tempos produtivos

## Importação de produção por XLSX

A importação aceita apenas arquivos `.xlsx` sem macros, com as colunas obrigatórias `funcionário`, `peça`, `quantidade`, `data`, `início` e `fim`. Os cabeçalhos são normalizados e aceitam aliases como `matrícula`, `cód. funcionário`, `cód. peça`, `qtd`, `dia`, `hora entrada` e `hora saída`. `setor`, `máquina` e `turno` são opcionais; códigos são lidos como texto para preservar zeros à esquerda.

- `POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/validar`
- `POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao`
- `GET /api/v1/empresas/{empresaId}/importacoes/producao?setorId=&status=&nomeArquivo=&dataInicio=&dataFim=`
- `GET /api/v1/empresas/{empresaId}/importacoes/producao/{importacaoId}`
- `GET /api/v1/empresas/{empresaId}/importacoes/producao/{importacaoId}/linhas`

A validação e a importação são all-or-nothing: nenhuma produção é criada se uma linha falhar. O histórico registra hash SHA-256, status, contagens e erros; o mesmo hash concluído não pode ser importado novamente no mesmo Setor. Arquivos vazios, corrompidos, com cabeçalho ausente ou acima de `IMPORT_MAX_FILE_SIZE`/`IMPORT_MAX_LINHAS` são rejeitados com códigos como `ARQUIVO_VAZIO`, `ARQUIVO_CORROMPIDO`, `CABECALHO_AUSENTE` e `LIMITE_LINHAS_EXCEDIDO`.

## Indicadores e analytics

A camada analítica exige `inicio` e `fim` em todos os endpoints principais e usa limites inclusivos. Entra na análise a produção ativa cujo início esteja no intervalo; sua duração e suas paradas ativas são consideradas integralmente. Não há rateio proporcional para registros que atravessam limites. O fuso operacional dos parâmetros é `America/Sao_Paulo`.

- `GET /api/v1/empresas/{empresaId}/analytics/resumo`
- `GET /api/v1/empresas/{empresaId}/analytics/setores`
- `GET /api/v1/empresas/{empresaId}/analytics/funcionarios`
- `GET /api/v1/empresas/{empresaId}/analytics/funcionarios/{funcionarioId}`
- `GET /api/v1/empresas/{empresaId}/analytics/pecas`
- `GET /api/v1/empresas/{empresaId}/analytics/maquinas`
- `GET /api/v1/empresas/{empresaId}/analytics/turnos`
- `GET /api/v1/empresas/{empresaId}/analytics/paradas/motivos`
- `GET /api/v1/empresas/{empresaId}/analytics/serie-temporal?granularidade=DIA|SEMANA|MES`
- `GET /api/v1/empresas/{empresaId}/analytics/comparativo`

Os filtros opcionais são `setorId`, `funcionarioId`, `pecaId`, `maquinaId` e `turnoId`. As métricas retornam quantidade, apontamentos, minutos brutos/parados/produtivos, taxas em peças/hora e percentual parado. Taxas ou variações com denominador zero retornam `null`; valores apresentados usam escala 2 e arredondamento HALF_UP. Produções e paradas inativas ficam fora por padrão, mas a desativação posterior de uma dimensão relacionada não remove o histórico. Máquinas e turnos ausentes aparecem como grupos técnicos `SEM_MAQUINA` e `SEM_TURNO`.

Motivos de parada sao cadastraveis por Empresa, com codigo unico dentro da empresa e ativacao/desativacao logica:

- `POST|GET /api/v1/empresas/{empresaId}/motivos-parada`
- `GET|PUT /api/v1/empresas/{empresaId}/motivos-parada/{motivoId}`
- `PATCH /api/v1/empresas/{empresaId}/motivos-parada/{motivoId}/ativar|desativar`

Paradas pertencem a uma producao e a um motivo da mesma Empresa:

- `POST|GET /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas`
- `GET|PUT /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}`
- `PATCH /api/v1/empresas/{empresaId}/producoes/{producaoId}/paradas/{paradaId}/ativar|desativar`

O intervalo deve ser positivo, estar contido na producao e nao se sobrepor a outra parada ativa; intervalos adjacentes sao permitidos. A resposta de producao informa `duracaoBrutaMinutos`, `tempoParadoMinutos` e `tempoProdutivoMinutos`, calculado como `max(0, bruto - parado)`. Historicos permanecem consultaveis apos inativacao e nao ha exclusao fisica.

## Tempos padrão, metas e desempenho

Tempos padrão são versionados por peça/setor e máquina opcional, com vigência baseada na data de início da produção. O padrão específico da máquina tem precedência sobre o padrão geral do setor; registros sem padrão permanecem nos totais reais e aparecem na cobertura.

- `POST|GET /api/v1/empresas/{empresaId}/tempos-padrao`
- `GET|PUT /api/v1/empresas/{empresaId}/tempos-padrao/{tempoId}`
- `PATCH /api/v1/empresas/{empresaId}/tempos-padrao/{tempoId}/ativar|desativar`

Metas aceitam os tipos `EMPRESA`, `SETOR`, `FUNCIONARIO`, `PECA`, `MAQUINA`, `TURNO` e `COMPOSTA`, com periodicidade `TURNO`, `DIA`, `SEMANA` ou `MES`. Metas mensais não são rateadas automaticamente em consultas parciais.

- `POST|GET /api/v1/empresas/{empresaId}/metas-producao`
- `GET|PUT /api/v1/empresas/{empresaId}/metas-producao/{metaId}`
- `PATCH /api/v1/empresas/{empresaId}/metas-producao/{metaId}/ativar|desativar`

Os indicadores derivados usam tempo produtivo e retornam quantidade esperada, desempenho, atingimento e cobertura (`registrosTotais`, `registrosComPadrao`, `registrosSemPadrao`, `quantidadeComPadrao`, percentual). Denominadores zero ou inexistentes retornam `null`.

- `GET /api/v1/empresas/{empresaId}/analytics/desempenho`
- `GET /api/v1/empresas/{empresaId}/analytics/desempenho/funcionarios|setores|maquinas|pecas`
- `GET /api/v1/empresas/{empresaId}/analytics/metas`

## Máquinas, recursos e turnos

Máquinas/recursos produtivos são cadastrados por Empresa e Setor atual:

- `POST|GET /api/v1/empresas/{empresaId}/maquinas`
- `GET|PUT /api/v1/empresas/{empresaId}/maquinas/{maquinaId}`
- `PATCH /api/v1/empresas/{empresaId}/maquinas/{maquinaId}/ativar|desativar`

Turnos são dinâmicos e aceitam janelas que atravessam meia-noite. A duração nominal é calculada em minutos:

- `POST|GET /api/v1/empresas/{empresaId}/turnos`
- `GET|PUT /api/v1/empresas/{empresaId}/turnos/{turnoId}`
- `PATCH /api/v1/empresas/{empresaId}/turnos/{turnoId}/ativar|desativar`

Produções podem informar opcionalmente `maquinaId` e `turnoId`; paradas podem informar `maquinaId`. Os vínculos devem pertencer à mesma Empresa, estar ativos e, para máquinas, coincidir com o Setor da produção. Não há inferência automática de turno, divisão entre turnos, manutenção ou parada independente de produção nesta etapa.

## Stack

## Valorização e análise financeira industrial

Valores de produção são referências industriais, não faturamento contábil ou fiscal. Cada valor é associado a peça, setor, máquina opcional, tipo (`VALOR_PROCESSAMENTO`, `VALOR_PRODUCAO` ou `RECEITA_ESTIMADA`), moeda ISO e vigência histórica.

- `POST|GET /api/v1/empresas/{empresaId}/valores-producao`
- `GET|PUT /api/v1/empresas/{empresaId}/valores-producao/{valorId}`
- `PATCH /api/v1/empresas/{empresaId}/valores-producao/{valorId}/ativar|desativar`
- `GET /api/v1/empresas/{empresaId}/analytics/financeiro/resumo`
- `GET /api/v1/empresas/{empresaId}/analytics/financeiro/setores|funcionarios|pecas|maquinas|turnos`
- `GET /api/v1/empresas/{empresaId}/analytics/financeiro/serie-temporal`
- `GET /api/v1/empresas/{empresaId}/analytics/financeiro/comparativo`

Os resultados mantêm moeda e tipo explícitos e nunca somam moedas diferentes. A resolução usa o valor específico da máquina e depois o fallback geral de peça/setor, pela data de início da produção. Ausência de valor é contabilizada na cobertura, não convertida em zero. Não são calculados faturamento real, custo, lucro, margem ou perda financeira por parada.

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

### Executar com Docker

Com Docker Desktop em execução, suba a API e o PostgreSQL com:

```powershell
docker compose up --build
```

A API ficará disponível em `http://localhost:8080`. Para executar em outra porta,
defina `APP_PORT`; as variáveis `POSTGRES_DB`, `POSTGRES_USER` e
`POSTGRES_PASSWORD` também podem ser sobrescritas no ambiente antes do comando.
Em produção, defina obrigatoriamente um `JWT_SECRET` próprio com pelo menos 32
bytes.

Para encerrar os containers:

```powershell
docker compose down
```

Os dados do PostgreSQL permanecem no volume `postgres_data`. Para removê-los
explicitamente, use `docker compose down -v`.

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

## Autenticacao e autorizacao

As rotas da API exigem Bearer JWT, exceto `POST /api/v1/auth/login` e health explicitamente configurado. O token e stateless; logout e realizado descartando-o no cliente. CSRF permanece desabilitado porque a API nao usa cookies de sessao.

Variaveis principais: `JWT_SECRET` (minimo de 32 bytes, sem default de producao), `JWT_EXPIRATION`, `CORS_ALLOWED_ORIGINS` (origens explicitas, sem `*`) e `BOOTSTRAP_ADMIN_ENABLED` com `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD` e `BOOTSTRAP_ADMIN_EMPRESA_ID`. O bootstrap e desligado por padrao, explicito e idempotente.

Os perfis semeados sao `ADMINISTRADOR`, `GESTOR` e `OPERACIONAL`. Permissoes de visualizacao, gerenciamento, importacao e financeiro sao independentes. A empresa do token sempre prevalece sobre `empresaId` da URL; gestores e operacionais precisam de associacao ao setor para filtros setoriais explicitos.

Endpoints principais: `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `PATCH /api/v1/auth/senha`, `GET|POST /api/v1/empresas/{empresaId}/usuarios`, `PATCH /api/v1/empresas/{empresaId}/usuarios/{usuarioId}/status/{status}`, `PATCH /api/v1/empresas/{empresaId}/usuarios/{usuarioId}/perfil` e `POST /api/v1/empresas/{empresaId}/usuarios/{usuarioId}/reset-senha`.

| Perfil | Permissoes | Escopo |
|---|---|---|
| ADMINISTRADOR | Todas da propria empresa, incluindo financeiro e usuarios | Todos os setores da propria empresa |
| GESTOR | Operacao, metas, analytics e gerenciamento operacional | Somente setores associados |
| OPERACIONAL | Visualizacao e registro de producao | Somente setores associados |

Respostas 401 e 403 nao incluem token, senha, hash ou stack trace. O logout stateless consiste em descartar o token; troca/reset de senha incrementa a versao de credenciais e invalida tokens anteriores.

O provisionamento inicial de uma empresa usa `POST /api/v1/empresas` autenticado com a permissao `EMPRESA_GERENCIAR`; os demais recursos de empresa exigem que a empresa da URL coincida com a empresa do token. Nao existe cadastro publico anonimo.

## Relatorios gerenciais

Os relatorios exigem `RELATORIO_VISUALIZAR` e respeitam empresa e escopo setorial na origem das consultas:

- `GET /api/v1/empresas/{empresaId}/relatorios/executivo?inicio=...&fim=...&inicioAnterior=...&fimAnterior=...&top=10`
- `GET /api/v1/empresas/{empresaId}/relatorios/producao?inicio=...&fim=...`
- `GET /api/v1/empresas/{empresaId}/relatorios/paradas?inicio=...&fim=...`
- `GET /api/v1/empresas/{empresaId}/relatorios/desempenho?inicio=...&fim=...`
- `GET /api/v1/empresas/{empresaId}/relatorios/executivo/exportar?inicio=...&fim=...&formato=CSV|XLSX`

Exportacoes usam UTF-8, separador `;`, escaping e nomes de arquivo previsiveis. Campos textuais iniciados por `=`, `+`, `-` ou `@` sao neutralizados contra CSV Injection. XLSX usa celulas numericas/datetime quando aplicavel, abas sem dados financeiros para usuarios sem `FINANCEIRO_INDUSTRIAL_VISUALIZAR` e limites configuraveis por `REPORT_MAX_PERIOD_DAYS`, `REPORT_MAX_TOP` e `REPORT_MAX_ROWS`. Valor industrial representa valor produzido configurado, nao faturamento real.

## Auditoria e rastreabilidade

Eventos de negocio sao persistidos de forma append-only na tabela `evento_auditoria`; nao existe endpoint comum para alterar ou excluir eventos e esta task nao executa retencao automatica. A trilha registra ator, empresa, recurso, acao, origem, data/hora, snapshots controlados e `correlationId` quando aplicavel. Snapshots nao armazenam senha, hash, token, secret, credencial, Authorization ou cookie de autenticacao.

Importacoes Excel podem ser correlacionadas por `correlationId`; registros derivados usam origem `IMPORTACAO_EXCEL`. O nome do arquivo e sanitizado e caminhos locais nao sao persistidos. Consultas e politica de permissao da auditoria serao expostas somente pelas rotas planejadas da capability, sempre com isolamento de empresa/setor e paginação.

## Qualidade e confiabilidade dos dados

A qualidade dos dados e calculada sob demanda, sem persistir alertas derivados, sem criar
eventos de auditoria e sem corrigir ou alterar registros. As consultas exigem a permissao
`QUALIDADE_DADOS_VISUALIZAR`, respeitam empresa e escopo setorial e aceitam filtros de
periodo, setor, funcionario, peca, maquina e turno.

Endpoints planejados:

- `GET /api/v1/empresas/{empresaId}/qualidade-dados/resumo?inicio=...&fim=...`
- `GET /api/v1/empresas/{empresaId}/qualidade-dados/problemas?inicio=...&fim=...`
- `GET /api/v1/empresas/{empresaId}/qualidade-dados/duplicidades?inicio=...&fim=...`
- `GET /api/v1/empresas/{empresaId}/qualidade-dados/recursos/{tipoRecurso}/{recursoId}`

Codigos iniciais de diagnostico: `QD-PADRAO-001` (sem tempo padrao),
`QD-VALOR-001` (sem valor industrial), `QD-TEMP-001` (intervalo invalido),
`QD-TEMP-002` (duracao longa/futuro), `QD-QTD-001` (quantidade suspeita),
`QD-TAXA-001` (taxa suspeita), `QD-PARADA-001` (parada fora da producao),
`QD-PARADA-002` (tempo produtivo negativo), `QD-CAD-001` (cadastro incompleto),
`QD-VIG-001` (vigencia conflitante) e `QD-DUP-001` (duplicidade exata).
Severidades sao `INFO`, `ALERTA` e `CRITICO`. A ausencia de cobertura nao e tratada
como zero e nao produz score subjetivo: o resumo informa contagens, quantidades e
percentuais objetivos.

Os limites de periodo, pagina, volume, duracao, tolerancia temporal, quantidade, taxa e
cobertura sao configuraveis por `quality.*` ou pelas variaveis `QUALITY_*` documentadas
em `application.properties`. Valores financeiros detalhados permanecem protegidos pela
permissao financeira e nao sao expostos por diagnosticos operacionais.

Analytics preserva o payload legado por padrao; `GET /api/v1/empresas/{empresaId}/analytics/resumo`
aceita `incluirQualidade=true` para incluir o bloco opcional `qualidadeDados`. O relatorio
executivo inclui esse mesmo bloco, alem de pontos objetivos para alertas criticos e duplicidades.
Para importacoes, `GET /api/v1/empresas/{empresaId}/importacoes/producao/{importacaoId}/qualidade`
resume linhas com alerta, duplicidades e lacunas de tempo/valor. Importacoes rejeitadas ou
revertidas nao geram diagnosticos de producao inexistente.
