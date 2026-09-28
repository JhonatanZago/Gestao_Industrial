# Design

## Context

O projeto é uma API Spring Boot 4.1.1 com Java 17, Spring MVC/Data JPA/Flyway e nenhum módulo de Spring Security ou domínio de usuários. Os recursos atuais recebem `empresaId` e filtros diretamente da URL, portanto a identidade e o escopo precisam ser resolvidos antes de qualquer consulta ou operação de serviço. Ver `proposal.md` e as especificações desta change para os contratos observáveis.

## Goals / Non-Goals

**Goals:**

- Introduzir identidade autenticada, hash de senha e token Bearer expirável.
- Modelar perfis, permissões e setores autorizados sem acoplar regras somente a nomes de perfil.
- Proteger todos os endpoints existentes com default-deny, empresa como limite absoluto e escopo setorial aplicado na consulta.
- Manter respostas 401/403 previsíveis, logs úteis e configurações sensíveis fora do Git.
- Permitir evolução posterior para múltiplos perfis, refresh tokens e auditoria detalhada.

**Non-Goals:**

- Frontend/tela de login, cadastro público, OAuth social, SSO, LDAP, MFA, recuperação por e-mail ou OAuth Authorization Server.
- Refresh token, blacklist distribuída e rate limiting distribuído nesta change; logout stateless será responsabilidade de descarte do access token pelo cliente.

## Decisions

### Identidade e persistência

Criar `Usuario` com empresa, nome, e-mail normalizado globalmente único, `senhaHash`, `StatusUsuario`, perfil principal, timestamps e `ultimoLoginEm`. Criar `Perfil`, `Permissao` e tabelas de associação perfil-permissão; criar `UsuarioSetor` para escopo de gestores/operacionais. Usar uma nova migration, ou conjunto ordenado de migrations, sem editar V1–V14, com FKs, unicidades e checks.

Será adotado um perfil principal por usuário, pois atende os três perfis definidos e evita ambiguidade na primeira versão. O modelo relacional de perfil-permissão permite adicionar múltiplos perfis no futuro sem migrar permissões gravadas em texto no usuário. A associação de setores terá unicidade por usuário/setor e garantirá empresa coerente no serviço e no banco quando possível.

### Senhas e bootstrap

Usar `BCryptPasswordEncoder` com política mínima configurável de oito caracteres. Nunca persistir ou logar senha, token ou hash em respostas; mensagens de login serão sempre genéricas. A alteração de senha validará a senha atual. Reset administrativo substituirá o hash e poderá marcar o usuário para troca futura, sem devolver senha existente.

O primeiro administrador será criado somente por comando/setup explícito ou runner habilitado por configuração de bootstrap, desabilitado por padrão e alimentado por variáveis de ambiente em execução. O procedimento será idempotente por e-mail; nenhuma credencial real será seedada em migration ou versionada.

### JWT e Spring Security

Adicionar Spring Security e o suporte Nimbus JOSE/JWT compatível com Boot 4.1.1. Configurar `SecurityFilterChain` stateless, `SessionCreationPolicy.STATELESS`, `PasswordEncoder`, `JwtEncoder` e `JwtDecoder` baseados em segredo de ambiente. O segredo deverá ter tamanho mínimo seguro e não terá fallback de produção embutido.

O login emitirá claims mínimas `sub`/id do usuário, `empresaId`, versão/identificador do usuário, perfil e `iat`/`exp`; permissões completas não serão dados sensíveis, mas serão carregadas/confirmadas pelo backend para evitar autorização obsoleta no token. Expiração padrão será 60 minutos e configurável por `JWT_EXPIRATION`. A troca de senha e mudanças de status invalidarão tokens por uma versão/`credentialsVersion` comparada no carregamento do principal, ou estratégia equivalente documentada.

Não haverá refresh token nesta change: isso evita introduzir armazenamento/revogação incompletos. Logout não revogará um access token já emitido; o cliente deverá descartá-lo, e alterações de senha/status terão invalidação por versão.

### Autorização e isolamento

Implementar um principal autenticado contendo usuário, empresa, perfil e permissões resolvidos do banco. Criar serviço central de autorização com verificações reutilizáveis para `hasPermission`, `sameCompany` e `canAccessSector`. Controladores poderão declarar permissões com anotações/metadados, mas serviços também validarão empresa e escopo para impedir bypass por chamada interna ou URL adulterada.

A configuração será default-deny: somente `/api/v1/auth/login` e health técnico explicitamente selecionado serão públicos. O `empresaId` da URL será comparado ao claim/principal antes de consultar repositórios. Administrador terá acesso a todos os setores da própria empresa; gestor e operacional dependerão de `UsuarioSetor`, salvo permissões globais explicitamente configuradas.

Para consultas de produção, funcionários, importações, metas, desempenho, financeiro e analytics, o escopo será convertido em predicado JPA/Specification antes da busca. Um `setorId` explícito fora do escopo resultará em 403, não em filtro silencioso. Agregações serão construídas somente com linhas autorizadas, evitando vazamento por totais.

### Matriz base

| Perfil | Escopo | Permissões padrão |
|---|---|---|
| ADMINISTRADOR | Todos os setores da própria empresa | Gestão da empresa, usuários, cadastros, produção/importação, analytics, desempenho, metas, tempos, financeiro e relatórios conforme seeds |
| GESTOR | Somente setores associados | Visualização/gestão operacional explicitamente seedada; financeiro, usuários e configurações somente se permissão atribuída |
| OPERACIONAL | Somente setores associados | Resumo e consultas operacionais mínimas; sem financeiro, usuários, metas ou valores por padrão |

Permissões de visualização e gerenciamento serão códigos separados. `PRODUCAO_IMPORTAR`, `FINANCEIRO_INDUSTRIAL_VISUALIZAR`, `VALOR_PRODUCAO_GERENCIAR`, `META_GERENCIAR`, `TEMPO_PADRAO_GERENCIAR` e equivalentes não serão herdadas apenas por ter acesso a produção.

### Endpoints, erros e configuração

Criar `/api/v1/auth/login`, `/api/v1/auth/me`, `/api/v1/auth/senha`, CRUD de usuários e reset administrativo. Proteger os recursos existentes por permissão correspondente, incluindo importações, financeiro, metas, tempos e valores.

Centralizar handlers de autenticação e acesso negado para 401/403 em JSON compatível com o `GlobalExceptionHandler`, sem stack trace. Configurar CORS com lista de origens de ambiente (`CORS_ALLOWED_ORIGINS`), nunca `*` com credenciais. Como a API usa Bearer stateless e não cookie de autenticação, CSRF será desabilitado explicitamente com justificativa; se a estratégia mudar para cookie, deverá ser reavaliada.

### Testes e compatibilidade

Usar testes HTTP com MockMvc/Spring Security e testes de serviço/repositório para senha, token, status, permissões, empresa e setores. Fixtures existentes receberão autenticação de teste real, sem desabilitar filtros. Cobrir pelo menos dois tenants e os módulos de funcionários, produção, analytics, financeiro, metas e importação.

## Risks / Trade-offs

- **[Risco]** Proteger endpoints existentes quebra testes/consumidores anônimos. **Mitigação:** atualizar fixtures para login/token real, documentar o breaking change e manter somente login/health públicos.
- **[Risco]** JWT stateless não permite logout imediato de cada token emitido. **Mitigação:** expiração curta configurável e versão de credenciais invalidando tokens após senha/status.
- **[Risco]** Escopo aplicado de forma incompleta pode vazar totais agregados. **Mitigação:** serviço/predicado central aplicado antes das consultas e testes de analytics por setor autorizado.
- **[Risco]** Secret ausente ou fraco inviabiliza emissão/validação. **Mitigação:** falhar no startup de ambiente protegido e fornecer apenas exemplo sem credencial real.
- **[Risco]** Seeds de permissões divergirem dos recursos. **Mitigação:** catálogo central de códigos, migration idempotente e testes de matriz por endpoint.

## Migration Plan

1. Adicionar dependências e configurações seguras de Security/JWT.
2. Criar tabelas, constraints e seeds idempotentes de perfis/permissões; executar bootstrap administrativo somente quando explicitamente habilitado.
3. Publicar autenticação e CRUD de usuários.
4. Publicar autorização central, escopo setorial e proteger recursos existentes.
5. Atualizar testes e documentação; executar clean test, build e revisão de 401/403.

Rollback da aplicação exige preservar a migration de identidade se dados já tiverem sido criados; rollback de banco deverá usar migration de compensação aprovada, sem editar V1–V14. Em implantação gradual, a configuração de bootstrap e o segredo devem estar disponíveis antes de tornar endpoints default-deny.

## Open Questions

Nenhuma decisão necessária permanece aberta: refresh token, recuperação por e-mail, MFA e rate limiting distribuído estão explicitamente fora do escopo desta change.
