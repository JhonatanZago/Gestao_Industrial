## Purpose

Definir a fronteira entre o assistente e os dados do ERP por meio de portas de
IA e tools tipadas, reutilizáveis e somente leitura, evitando que o modelo
obtenha acesso genérico a banco, código ou serviços não autorizados.

## ADDED Requirements

### Requirement: Providers devem ser acessados por portas desacopladas

O sistema SHALL definir contratos equivalentes a `ChatModelGateway`,
`EmbeddingGateway` e `VectorStorePort`, com DTOs próprios para entradas,
saídas, metadados e erros. Implementações de fornecedor SHALL ficar atrás
dessas portas e não poderão ser obrigatórias para iniciar ou testar o ERP.

#### Scenario: Aplicação inicia sem credencial de IA

- **WHEN** nenhuma credencial, provider ou serviço vetorial externo está
  configurado
- **THEN** a aplicação inicia normalmente e os fakes/implementações desabilitadas
  permitem executar os testes offline

#### Scenario: Provider devolve saída incompatível

- **WHEN** o adapter devolve JSON inválido, schema desconhecido ou erro não
  classificado
- **THEN** a porta converte o resultado para erro seguro, impede a execução de
  comandos não validados e o endpoint retorna indisponibilidade apropriada

### Requirement: O catálogo de tools deve ser explícito e somente leitura

Cada tool SHALL declarar nome, versão, schema de entrada, schema de saída,
permissões necessárias, escopo de empresa/setor e limites. O catálogo SHALL
conter, quando a capacidade correspondente existir, consultas de produção,
comparação, série temporal, setor, funcionário, peça, máquina, paradas, metas,
desempenho, valor industrial, qualidade e auditoria.

#### Scenario: Modelo solicita uma tool registrada

- **WHEN** o orquestrador recebe uma chamada cujo nome e argumentos obedecem ao
  schema de uma tool registrada
- **THEN** a tool valida os argumentos, executa somente a consulta read-only
  permitida e retorna resultado estruturado com identificador de fonte

#### Scenario: Modelo solicita uma tool desconhecida ou argumento inválido

- **WHEN** o provider pede uma tool inexistente, um campo não declarado ou um
  valor fora dos limites
- **THEN** o sistema rejeita a chamada, registra apenas metadados mínimos e não
  acessa banco, serviço ou método arbitrário

### Requirement: Tools devem reutilizar as fontes de dados do ERP

As tools SHALL chamar os Services, facades ou consultas de analytics,
paradas, metas, desempenho, financeiro, qualidade e auditoria existentes, sem
duplicar regra de negócio silenciosamente. Quando uma facade nova for
necessária, ela SHALL preservar as regras de cálculo e autorização do módulo
de origem.

#### Scenario: Pergunta sobre produção usa resultado do ERP

- **WHEN** uma pergunta autorizada solicita produção por período, setor,
  funcionário, peça ou máquina
- **THEN** a tool traduz os filtros para a consulta existente, retorna valores e
  metadados do período e não calcula um número alternativo no prompt

#### Scenario: Consulta de qualidade acompanha o dado operacional

- **WHEN** a tool de produção, desempenho ou relatório encontra qualidade de
  dados parcial ou suspeita
- **THEN** o resultado inclui o estado/avisos de qualidade para que o
  orquestrador não apresente o número como plenamente confiável

### Requirement: Resultados de tools devem ser limitados e rastreáveis

Toda execução SHALL respeitar timeout, quantidade máxima de registros,
tamanho de payload e número total de chamadas configurados. O resultado SHALL
conter período/filtros efetivos, origem lógica, sucesso/erro e dados suficientes
para a camada de grounding citar a fonte, sem incluir segredos.

#### Scenario: Limite de chamadas é atingido

- **WHEN** uma pergunta exige mais chamadas do que o limite por requisição
- **THEN** o orquestrador interrompe o fluxo, retorna estado seguro e não cria
  novas chamadas automaticamente

#### Scenario: Tool expira

- **WHEN** uma consulta excede o timeout configurado
- **THEN** a chamada é cancelada ou abandonada de forma segura, o erro é
  classificado como `AI_TIMEOUT` e nenhuma resposta numérica baseada em
  resultado incompleto é emitida
