## Purpose

Garantir que o assistente responda a partir de fatos estruturados e documentos
autorizados, deixando claro o período e a qualidade dos dados e impedindo
valores, causas ou conclusões que não possam ser demonstrados pelas fontes
retornadas.

## ADDED Requirements

### Requirement: Toda resposta factual deve carregar evidências internas

Uma resposta com dados SHALL associar cada bloco factual a resultados de tools
ou trechos documentais autorizados. A resposta SHALL expor somente referências
internas seguras, como tipo, título, período, identificador e aviso, sem
expor prompt, credencial ou conteúdo não autorizado.

#### Scenario: Resposta de produção possui fonte e período

- **WHEN** a tool retorna produção para um intervalo válido
- **THEN** a resposta inclui `dataUsed`, o período efetivo, a fonte interna e
  qualquer aviso de qualidade relacionado ao cálculo

#### Scenario: Não há fonte suficiente para uma afirmação

- **WHEN** o provider produz uma conclusão que não corresponde a nenhum
  resultado de tool ou trecho recuperado
- **THEN** a camada de grounding remove ou rejeita a afirmação e não a apresenta
  como fato do ERP

### Requirement: Números e comparações devem ser verificáveis

O sistema SHALL validar números, percentuais, totais, médias, variações,
datas, rankings e nomes de entidades mencionados na resposta contra os dados
retornados. Claims sem correspondência SHALL ser rejeitados, regenerados uma
única vez com contexto válido ou substituídos por uma resposta segura.

#### Scenario: Provider inventa um total

- **WHEN** a resposta contém um total que não aparece nem pode ser derivado de
  um resultado autorizado com regra conhecida
- **THEN** o sistema não entrega esse total e retorna resposta corrigida ou
  aviso de que não há dado suficiente

#### Scenario: Comparação é sustentada pelas duas bases

- **WHEN** o usuário pede comparação entre dois períodos ou setores
- **THEN** a resposta identifica as duas bases, aplica somente a operação
  permitida e apresenta a variação com as fontes correspondentes

### Requirement: Ausência e qualidade devem influenciar a resposta

Quando não houver dados, houver escopo parcial ou a qualidade indicar
inconsistência, o sistema SHALL informar isso em `status`, `qualityData` ou
`warnings` e SHALL NOT preencher lacunas com estimativas não solicitadas.

#### Scenario: Dados parcialmente confiáveis

- **WHEN** a consulta encontra registros com alerta de qualidade, campos
  ausentes ou cobertura incompleta
- **THEN** a resposta apresenta o dado disponível junto do alerta, limita a
  conclusão ao escopo confiável e identifica a qualidade no contrato

#### Scenario: Documentos não substituem fato operacional ausente

- **WHEN** não há produção registrada no ERP, mas um manual menciona uma meta
  ou exemplo numérico
- **THEN** o sistema retorna `SEM_DADOS` para a produção e não usa o número do
  manual como se fosse medição realizada

### Requirement: O modelo deve receber contexto mínimo e separado por fonte

O orquestrador SHALL distinguir resultados de tools, trechos de RAG,
instruções de sistema e pergunta do usuário. Texto recuperado SHALL ser tratado
como dado não confiável, nunca como instrução com autoridade para mudar regras,
permissões ou tools.

#### Scenario: Documento contém uma instrução maliciosa

- **WHEN** um trecho recuperado diz para ignorar filtros, revelar dados ou
  executar uma ação
- **THEN** o sistema trata o trecho apenas como conteúdo documental, mantém as
  políticas de segurança e não executa a instrução

### Requirement: Respostas devem ser úteis sem expor raciocínio interno

O contrato SHALL permitir resposta curta, avisos, fontes e pedido de
clarificação, mas SHALL NOT expor chain-of-thought, tokens internos, prompts
privados ou credenciais.

#### Scenario: Grounding falha após a tentativa de correção

- **WHEN** a resposta continua contendo claims não sustentados após a validação
- **THEN** o sistema retorna uma resposta limitada com `warnings` ou
  `PRECISA_CLARIFICACAO`, sem publicar o conteúdo não fundamentado
