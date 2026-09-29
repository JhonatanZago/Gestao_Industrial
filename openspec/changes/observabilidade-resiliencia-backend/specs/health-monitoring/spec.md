# Spec Delta

## Purpose

Define sinais seguros e verificáveis de vida, prontidão e dependências do backend para operação, automação e diagnóstico sem exposição de detalhes internos.

## ADDED Requirements

### Requirement: Exposição mínima e protegida de saúde
O sistema SHALL expor publicamente apenas um resultado mínimo de saúde e SHALL exigir autenticação e autorização administrativa para detalhes de componentes. Endpoints administrativos sensíveis, incluindo ambiente, beans, configuração e heap dump, MUST permanecer indisponíveis externamente.

#### Scenario: Consulta pública de saúde
- **WHEN** um cliente não autenticado consulta o endpoint público de saúde
- **THEN** o sistema retorna somente o status agregado, sem banco, IA, configuração, credenciais ou detalhes internos

#### Scenario: Tentativa de consultar endpoint sensível
- **WHEN** um cliente tenta consultar um endpoint operacional não permitido
- **THEN** o sistema rejeita a solicitação ou mantém o endpoint não exposto

### Requirement: Probes com semântica independente
O sistema SHALL disponibilizar liveness e readiness com semânticas distintas. Liveness SHALL representar a capacidade do processo de continuar executando; readiness SHALL representar a capacidade de atender tráfego dependente do banco.

#### Scenario: Processo vivo com IA indisponível
- **WHEN** o processo está funcional e o provedor opcional de IA está indisponível
- **THEN** liveness permanece saudável e readiness não falha por causa exclusiva da IA

#### Scenario: Banco indisponível
- **WHEN** a aplicação não consegue validar uma conexão leve com o banco essencial
- **THEN** readiness indica indisponibilidade para tráfego sem declarar falsamente o banco como saudável

### Requirement: Estado operacional da IA e RAG
O sistema SHALL representar IA e RAG como componentes opcionais nos estados `DISABLED`, `AVAILABLE` ou `UNAVAILABLE`, sem revelar URLs, tokens, prompts ou documentos.

#### Scenario: IA desabilitada por configuração
- **WHEN** a aplicação inicia com IA desabilitada
- **THEN** o estado do componente é `DISABLED` e as funcionalidades principais do ERP permanecem disponíveis

#### Scenario: Adapter configurado sem resposta
- **WHEN** a IA está habilitada e sua verificação segura detecta indisponibilidade
- **THEN** o estado do componente é `UNAVAILABLE` sem tornar o processo não vivo

### Requirement: Informações seguras de build
O sistema SHALL disponibilizar nome, versão e dados seguros de build no endpoint de informações e MUST omitir secrets, variáveis sensíveis e configuração completa.

#### Scenario: Consulta de informações operacionais
- **WHEN** um operador autorizado consulta as informações da aplicação
- **THEN** recebe somente nome, versão e metadados seguros de build ou commit configurados

### Requirement: Healthcheck do container
Quando a aplicação for executada pelo ambiente Docker fornecido, o container SHALL possuir healthcheck baseado no probe apropriado e SHALL aguardar o banco conforme a dependência declarada.

#### Scenario: Aplicação pronta no Docker Compose
- **WHEN** banco e aplicação concluem a inicialização
- **THEN** o container da aplicação transita para saudável usando o endpoint de readiness

