# Spec Delta

## Purpose

Define uma experiência Premium Dark interativa para o fluxo operacional já homologado, mantendo os dados reais do Backend V1, a navegação contextual e os estados seguros de autenticação, importação e relatórios.

## ADDED Requirements

### Requirement: O shell Premium Dark SHALL concentrar o fluxo core

O shell autenticado SHALL apresentar sidebar desktop de aproximadamente 220–250px, drawer utilizável em mobile, estado ativo discreto, ícones semânticos consistentes e header compacto com título, contexto e período. A navegação SHALL manter Visão Geral, Setores, Importar Excel e Relatórios conforme permissões do usuário.

#### Scenario: Usuário acessa o shell em desktop

- **WHEN** uma sessão autorizada abre a aplicação em viewport desktop
- **THEN** o sistema SHALL exibir a sidebar compacta, a área de conteúdo e o item ativo sem usar letras como substitutos de ícones
- **AND** o header SHALL reservar espaço para período e Atualizar sem ocupar altura excessiva

#### Scenario: Usuário acessa o shell em mobile

- **WHEN** a viewport é mobile e o usuário abre o menu
- **THEN** o sistema SHALL exibir um drawer com foco e fechamento previsíveis
- **AND** a área de conteúdo SHALL permanecer navegável sem rolagem horizontal

### Requirement: A Visão Geral SHALL exibir somente KPIs e agregações reais

A Visão Geral SHALL exibir quatro KPIs compactos para produção total, setores com dados, funcionários envolvidos e peças produzidas, além de evolução temporal, produção por setor e resumo do período. Nenhum KPI, comparação percentual ou indicador de OEE, lucro, faturamento, parada ou eficiência SHALL ser criado sem campo real suportado pelo backend.

#### Scenario: Dashboard possui dados homologados

- **WHEN** o dashboard responde com dados válidos para o período selecionado
- **THEN** os KPIs, totais e números SHALL refletir a resposta real formatada em pt-BR
- **AND** os cards SHALL apresentar descrição, valor, ícone semântico e microvisual somente quando derivável do payload

#### Scenario: Dashboard está vazio

- **WHEN** a API responde sem registros para o período
- **THEN** cada painel afetado SHALL apresentar estado empty explicativo
- **AND** o sistema SHALL evitar zeros ou gráficos que aparentem dados inexistentes

### Requirement: Período e Atualizar SHALL controlar o mesmo estado de consulta

O seletor de período SHALL ser compartilhado pelos painéis compatíveis, SHALL refletir a seleção atual e SHALL estar conectado à ação Atualizar. O botão SHALL informar os estados Atualizar, Atualizando e Atualizado sem duplicar requisições desnecessárias.

#### Scenario: Usuário altera o período

- **WHEN** o usuário seleciona um período válido
- **THEN** o sistema SHALL atualizar os painéis que suportam esse período usando os limites corretos
- **AND** SHALL preservar a seleção ao navegar entre dashboard, setor e relatório quando o contexto permitir

#### Scenario: Usuário atualiza o dashboard

- **WHEN** o usuário aciona Atualizar
- **THEN** o sistema SHALL executar uma consulta real para o período atual e informar visualmente o progresso
- **AND** SHALL impedir cliques concorrentes até a consulta terminar

### Requirement: Gráficos e ranking de setores SHALL ser interativos e dinâmicos

O gráfico de evolução SHALL usar linha, área transparente, pontos discretos, grid sutil, tooltip e informação textual equivalente. O painel de produção por setor SHALL ordenar dados reais, mostrar no máximo sete itens na home, oferecer Ver todos e permitir abertura do dashboard do setor.

#### Scenario: Usuário interage com evolução ou setor

- **WHEN** o usuário passa o cursor, move o foco por teclado ou clica em um ponto/painel permitido
- **THEN** o sistema SHALL exibir tooltip ou detalhe textual acionável e manter cursor, foco e contraste adequados
- **AND** um setor clicável SHALL abrir seu contexto sem perder o período compartilhado

#### Scenario: Setores retornados são dinâmicos

- **WHEN** a API retorna nomes, IDs e quantidades diferentes da massa de teste
- **THEN** a ordenação, rótulos e links SHALL ser construídos a partir da resposta
- **AND** nenhum nome ou ID de setor SHALL ser hardcoded

### Requirement: A home SHALL oferecer Top funcionários, Top peças e resumo derivável

Os painéis inferiores SHALL mostrar até cinco funcionários e cinco peças com seus códigos originais e quantidades reais. O resumo SHALL mostrar dias com produção, média diária e maior/menor produção somente quando esses valores forem corretamente deriváveis ou fornecidos.

#### Scenario: Usuário abre um item de ranking

- **WHEN** o funcionário ou peça possui navegação autorizada no contexto atual
- **THEN** o item SHALL ser clicável e SHALL preservar código, setor e período
- **AND** códigos como `00147` SHALL continuar textuais, sem conversão numérica

#### Scenario: Backend não fornece métrica derivável

- **WHEN** um valor do resumo não pode ser calculado com segurança a partir do contrato
- **THEN** o painel SHALL omitir o valor ou informar indisponibilidade
- **AND** SHALL não preencher a lacuna com estimativa ou ranking subjetivo

### Requirement: Setores, importação e relatórios SHALL compartilhar a linguagem interativa

A página de Setores SHALL ter cards compactos com busca e navegação real; o Dashboard do Setor SHALL manter KPIs, evolução e rankings contextuais; Importar Excel SHALL apresentar quatro passos e estados de progresso; Relatórios SHALL ter período, setor e resultado reconciliado. Todas as telas SHALL reutilizar dados e permissões reais.

#### Scenario: Usuário navega do setor para detalhes

- **WHEN** o usuário seleciona um card ou Ver detalhes
- **THEN** o sistema SHALL abrir o dashboard do setor com produção, funcionários, peças e duração somente quando disponíveis
- **AND** SHALL oferecer drill-down para funcionário e peça sem perder o contexto

#### Scenario: Usuário importa uma planilha

- **WHEN** o usuário seleciona setor, escolhe ou arrasta um XLSX e confirma a validação
- **THEN** o fluxo SHALL mostrar Selecionar setor, Enviar arquivo, Validar e Concluir com estados reais do backend
- **AND** sucesso, parcial, duplicidade e erro SHALL oferecer resultado acionável sem expor dados sensíveis

#### Scenario: Usuário gera um relatório

- **WHEN** o usuário informa período e setor e solicita relatório
- **THEN** o resultado SHALL usar somente formato suportado e SHALL reconciliar seus totais com o dashboard
- **AND** resposta vazia, sem permissão ou erro SHALL aparecer como estado explícito recuperável

### Requirement: Cada painel SHALL ter estados seguros e recuperáveis

Dashboard, setores, importação e relatórios SHALL distinguir loading, success, empty e error. Erros de autenticação/autorização SHALL respeitar guards e interceptor, e erros recuperáveis SHALL oferecer tentar novamente e correlationId quando fornecido.

#### Scenario: Consulta falha com correlationId

- **WHEN** uma requisição retorna erro recuperável com identificador de correlação
- **THEN** o painel SHALL exibir mensagem segura, o correlationId e uma ação de retry
- **AND** SHALL não exibir stack trace, senha ou JWT nem repetir automaticamente operação mutável

#### Scenario: Sessão expira ou acesso é negado

- **WHEN** a API retorna 401 ou 403
- **THEN** o sistema SHALL direcionar para a resposta de sessão expirada ou acesso proibido prevista pelo fluxo
- **AND** SHALL não manter dados protegidos visíveis após logout ou expiração

### Requirement: O redesign SHALL ser responsivo, acessível e performático

O layout SHALL atender 1920x1080, 1366x768, tablet e mobile, priorizando informação útil na primeira dobra. SHALL existir foco visível, teclado, aria-label/current, contraste e texto equivalente aos gráficos; animações SHALL respeitar `prefers-reduced-motion`. O frontend SHALL evitar requisições duplicadas e biblioteca de gráficos desnecessariamente pesada.

#### Scenario: Usuário reduz movimento ou usa teclado

- **WHEN** `prefers-reduced-motion` está ativo ou o usuário navega por teclado
- **THEN** transições/animações SHALL ser reduzidas e todos os controles acionáveis SHALL possuir ordem de foco previsível
- **AND** gráficos SHALL continuar compreensíveis sem depender apenas de cor, hover ou movimento

#### Scenario: Dashboard é carregado com múltiplos painéis

- **WHEN** o payload consolidado contém dados suficientes para a tela
- **THEN** o frontend SHALL compartilhar o estado da consulta entre os painéis
- **AND** SHALL evitar uma chamada independente por widget quando o contrato já atende a necessidade

### Requirement: O fluxo SHALL possuir testes e build verificáveis

A implementação SHALL manter os testes existentes, adicionar cobertura para KPIs, período, Atualizar, setor clicável, Ver todos, rankings, estados de painel, sidebar e preservação de dados reais, e SHALL passar `npm test` e `npm run build`.

#### Scenario: Suíte do frontend é executada

- **WHEN** os testes unitários, de componente e E2E aplicáveis são executados
- **THEN** as interações principais e os estados loading, empty e error SHALL ser verificados sem dados fictícios
- **AND** o build SHALL concluir sem erro de TypeScript ou template
