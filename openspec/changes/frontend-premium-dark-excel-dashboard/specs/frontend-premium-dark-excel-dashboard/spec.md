# Frontend Premium Dark Excel Dashboard

## Purpose

Esta capability define o frontend operacional homologado para o fluxo Excel por setor, importação, produção, dashboards, drill-downs e relatório simplificado, usando somente dados e contratos disponíveis no Backend V1.

## ADDED Requirements

### Requirement: O shell autenticado SHALL apresentar somente o fluxo core

Após o login, o sistema SHALL exibir na navegação principal apenas Visão Geral, Setores, Importar Excel e Relatórios. Módulos secundários não SHALL aparecer como itens da sidebar principal, sem remover os endpoints do backend.

#### Scenario: Usuário autenticado acessa a aplicação

- **WHEN** o usuário conclui um login válido
- **THEN** o sistema SHALL abrir a Visão Geral e exibir exatamente as quatro áreas principais
- **AND** SHALL manter perfil, logout, proteção de sessão e redirecionamento de 401/403

#### Scenario: Usuário encerra a sessão

- **WHEN** o usuário aciona logout
- **THEN** o token e o estado de sessão SHALL ser removidos
- **AND** o sistema SHALL retornar para `/login` sem expor dados protegidos

### Requirement: O frontend SHALL usar uma linguagem visual Premium Dark compartilhada

Todas as telas do fluxo core SHALL usar tokens centralizados de cor, tipografia, espaçamento, borda, raio, sombra e estados sem duplicar CSS de componentes equivalentes.

#### Scenario: Tela é renderizada em desktop, tablet ou celular

- **WHEN** a viewport muda entre 1920x1080, 1366x768, tablet e mobile
- **THEN** o shell, tabelas, filtros, gráficos, drawer e ações SHALL permanecer utilizáveis
- **AND** foco visível, labels, contraste e navegação por teclado SHALL ser preservados

### Requirement: A Visão Geral SHALL consumir o dashboard real homologado

A Visão Geral SHALL consumir o contrato real de `GET /api/v1/empresas/{empresaId}/analytics/dashboard` e SHALL exibir apenas total de produção, setores com dados, funcionários envolvidos, códigos de peça, evolução e duração quando fornecidos pelo backend.

#### Scenario: Dashboard possui dados

- **WHEN** a API responde com dados válidos
- **THEN** os cards, lista de setores e evolução SHALL refletir a resposta sem KPI inventado
- **AND** o frontend SHALL formatar números, datas e duração em pt-BR

#### Scenario: Dashboard não possui dados ou falha

- **WHEN** a resposta é vazia, inválida, 401, 403 ou erro de rede
- **THEN** a tela SHALL exibir respectivamente estado empty, sessão expirada, acesso negado ou erro recuperável
- **AND** SHALL oferecer ação de tentar novamente quando aplicável

### Requirement: Setores SHALL ser dinâmicos e possuir dashboard contextual

A lista de setores SHALL vir da API autorizada do usuário. A tela de setor SHALL consumir `GET /api/v1/empresas/{empresaId}/analytics/dashboard/setor/{setorId}` com período e filtros suportados pelo contrato, sem nomes ou IDs hardcoded.

#### Scenario: Usuário seleciona um setor válido

- **WHEN** o usuário abre um setor retornado pela API
- **THEN** a tela SHALL mostrar nome, período, total produzido, funcionários, peças, evolução e duração quando disponíveis
- **AND** SHALL permitir navegação contextual para funcionários, peças e relatório

#### Scenario: Setor não está autorizado ou não possui dados

- **WHEN** a API responde 403, 404 ou sem registros
- **THEN** o frontend SHALL exibir forbidden, not found ou empty sem preencher valores fictícios

### Requirement: Drill-downs SHALL preservar identidade e contexto operacional

As telas de funcionário e peça SHALL receber o setor e o código original, preservar zeros à esquerda como `00147`, aplicar apenas métricas presentes no contrato e respeitar permissões de acesso.

#### Scenario: Usuário abre o funcionário 00147

- **WHEN** o usuário parte de um setor e seleciona o código `00147`
- **THEN** a URL, o título, filtros e requisições SHALL preservar `00147` sem conversão numérica
- **AND** SHALL exibir produção e relações retornadas pelo backend, sem avaliação subjetiva

#### Scenario: Usuário abre uma peça

- **WHEN** o usuário seleciona uma peça em um dashboard autorizado
- **THEN** o sistema SHALL mostrar os dados da peça e funcionários relacionados somente se retornados e autorizados

### Requirement: A importação SHALL validar e enviar XLSX com estados explícitos

A tela Importar Excel SHALL permitir setor dinâmico, seleção ou arraste de `.xlsx`, validação e envio usando o contrato existente de importação, reconhecendo as seis colunas oficiais e exibindo a resposta real do backend.

#### Scenario: Usuário importa um arquivo válido

- **WHEN** o usuário seleciona setor e arquivo XLSX válido e confirma uma única vez
- **THEN** o fluxo SHALL passar por selecionado, validando, enviando, processando e sucesso
- **AND** SHALL atualizar a visão core com a resposta da importação sem recarregar dados inventados

#### Scenario: Arquivo inválido, duplicado ou processamento parcial

- **WHEN** o arquivo tem extensão/conteúdo inválido, já foi importado ou o backend retorna erros por linha
- **THEN** o sistema SHALL impedir reenvio duplicado e apresentar partial, duplicate ou error com mensagens acionáveis
- **AND** SHALL ocultar stack trace, token e detalhes sensíveis

### Requirement: Relatórios SHALL usar somente formatos suportados pelo backend

A tela de Relatórios SHALL permitir período e setor, consumir `GET /api/v1/empresas/{empresaId}/relatorios/simplificado` e disponibilizar somente formatos realmente suportados, reconciliando os totais com o dashboard.

#### Scenario: Usuário gera relatório simplificado

- **WHEN** o usuário informa filtros válidos e solicita o relatório
- **THEN** o resultado SHALL refletir os mesmos dados homologados do dashboard
- **AND** o download SHALL indicar o formato real retornado pela API

#### Scenario: Relatório sem dados ou sem permissão

- **WHEN** o backend responde vazio, 401 ou 403
- **THEN** a tela SHALL exibir empty, sessão expirada ou forbidden sem criar arquivo vazio enganoso

### Requirement: Erros de API SHALL ser seguros, rastreáveis e recuperáveis

O frontend SHALL preservar AuthService, guards e interceptors existentes, tratar 401/403 globalmente, exibir correlation ID quando fornecido e nunca registrar senha, JWT ou dados sensíveis.

#### Scenario: Backend retorna erro com correlation ID

- **WHEN** uma requisição falha com um identificador de correlação
- **THEN** a mensagem SHALL informar o identificador de forma segura
- **AND** SHALL disponibilizar retry sem repetir automaticamente uma operação mutável

### Requirement: O fluxo SHALL possuir testes automatizados e documentação de homologação

A implementação SHALL manter os testes atuais, adicionar testes de contrato/componentes/permissões e executar E2E com a massa oficial: total 350, `00147=200`, `00152=150`, `PEC-001=270`, `PEC-002=80` e duração 185 minutos.

#### Scenario: Homologação executa o fluxo completo

- **WHEN** o teste faz login, importa o template para Pintura a pó, abre dashboard, setor, funcionário, peça e relatório e faz logout
- **THEN** todos os passos SHALL concluir sem erro e os totais SHALL coincidir com a massa oficial
- **AND** build frontend, validação OpenSpec e regressão dos 154 testes backend SHALL permanecer verdes

