# Design

## Context

O repositório contém apenas o Backend V1 Spring Boot, com API versionada em `/api/v1`, JWT stateless, autorização por empresa/setor/permissão, paginação, importação Excel, analytics, relatórios, qualidade, auditoria e assistente IA/RAG. Não há `package.json`, `angular.json` ou aplicação frontend existente; portanto, a change começa por uma fundação nova e deve tratar o backend homologado como contrato de integração.

## Goals / Non-Goals

**Goals:**

- Entregar uma aplicação Angular standalone, modularizada por features e lazy-loaded.
- Centralizar sessão, HTTP, modelos DTO, tratamento de erros e permissões em `core`; componentes visuais em `shared`; shell em `layout`.
- Construir primeiro uma base navegável e verificável, depois habilitar as telas por fases, com checkpoints de build e testes.
- Preservar paginação server-side, filtros reais, escopo empresarial/setorial e proteção de dados financeiros.

**Non-Goals:**

- Alterar controllers, DTOs, permissões, migrations ou regras do Backend V1.
- Criar frontend com dados produtivos fake permanentes, endpoints presumidos ou regras de negócio duplicadas.
- Introduzir NgRx, microfrontends, SSR, telemetria de máquina ou provedor pago de IA sem evidência de necessidade no contrato atual.

## Decisions

### Aplicação standalone e organização por feature

Usar Angular standalone compatível com o ambiente disponível, com `core`, `shared`, `layout` e `features/<domínio>`. Cada domínio terá rotas lazy-loaded, serviços de API, modelos de transporte separados de view models e componentes de tela. Isso reduz acoplamento e permite checkpoints por fase.

Alternativas consideradas: um componente monolítico (rejeitado por manutenção e isolamento insuficientes) e NgRx desde o início (rejeitado por adicionar complexidade antes de comprovar necessidade).

### Camada de integração orientada ao contrato

Centralizar base URL, ambientes, interceptors, serialização de erros, correlation ID e serviços HTTP tipados. O mapa endpoint → método → DTO → permissão será mantido na documentação e usado como checklist de implementação. Onde o backend não oferece uma capacidade, a UI mostrará estado não disponível em vez de criar um endpoint.

Alternativa considerada: gerar automaticamente todos os modelos a partir de Swagger; só será usada se uma especificação OpenAPI real estiver disponível e compatível, evitando transformar documentação incompleta em contrato falso.

### Estado local proporcional e server-side

Usar signals/services para estado de sessão, filtros e telas; cache local somente quando seguro. Tabelas respeitarão paginação e ordenação do backend, e buscas terão debounce/cancelamento. Não carregar grandes conjuntos para paginar no navegador.

### Design System por tokens

Criar tokens CSS/tema central e componentes compartilhados para estados recorrentes. Gráficos serão encapsulados em wrappers comuns e escolhidos entre uma única biblioteca mantida compatível com a versão Angular disponível.

### Segurança de sessão e apresentação

O token não será escrito em logs nem incluído em mensagens de erro. Guards servem à UX, enquanto o backend permanece autoridade final. Dados financeiros somente serão montados quando a permissão e a resposta autorizada existirem; auditoria e RAG seguirão o mesmo princípio.

### Entrega faseada

Executar em fases A–Q da master task: contrato/backend, fundação, design system, auth/shell, dashboard, módulos operacionais, relatórios/auditoria/IA, integração, testes e homologação. Cada checkpoint deve executar o build/teste disponível antes de avançar.

## Risks / Trade-offs

- [Contrato incompleto ou divergente] → validar os controllers/DTOs reais antes de cada feature e registrar a divergência na matriz de integração.
- [Ausência de OpenAPI formal] → usar README, resources e DTOs/controllers como fontes observáveis; não gerar endpoints por suposição.
- [Biblioteca visual incompatível] → fixar versões compatíveis e encapsular gráficos; manter fallback de tabela/estado vazio.
- [Dados financeiros renderizados antes da autorização] → resolver permissões antes da montagem dos cards e testar usuários sem acesso.
- [Escopo muito amplo da master task] → manter fases, critérios de entrada/saída e não marcar tasks sem evidência de build/teste.
- [Backend indisponível durante desenvolvimento] → usar mocks contratuais somente em testes; manter integração real como critério de homologação.

## Migration Plan

1. Criar a aplicação frontend e configuração de desenvolvimento apontando para o backend local.
2. Implementar fundação, design system, autenticação e shell; validar build/testes.
3. Habilitar cada feature usando os endpoints reais e atualizar a matriz de contratos.
4. Executar integração com backend homologado, testes de permissões, E2E e validação visual responsiva.
5. Publicar o artefato de build conforme o ambiente escolhido, mantendo rollback como retorno ao artefato frontend anterior; nenhuma alteração de banco ou migration é necessária.

## Open Questions

- A versão exata de Node/npm e Angular deverá ser fixada no primeiro checkpoint após confirmar as ferramentas disponíveis no ambiente de desenvolvimento.
- A biblioteca de gráficos será escolhida nesse checkpoint, condicionada à compatibilidade verificada e à licença aceitável para o projeto.
