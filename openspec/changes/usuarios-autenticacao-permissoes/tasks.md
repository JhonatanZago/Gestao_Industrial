# Tasks

## 1. Dependências e configuração

- [x] 1.1 Adicionar dependências compatíveis de Spring Security e JWT/Nimbus para Spring Boot 4.1.1; verificar resolução sem duplicar versões incompatíveis.
- [x] 1.2 Criar configuração por ambiente para `JWT_SECRET`, `JWT_EXPIRATION` e `CORS_ALLOWED_ORIGINS`, com validação de segredo e nenhum default inseguro em produção; verificar startup de teste com segredo explícito.
- [x] 1.3 Configurar `SecurityFilterChain` stateless, default-deny, Bearer token, CORS restrito e CSRF coerente com API sem cookies; verificar endpoints públicos e protegidos com teste HTTP.

## 2. Modelo de identidade e seeds

- [x] 2.1 Criar enums/status e entidades `Usuario`, `Perfil`, `Permissao` e associação `UsuarioSetor`; verificar compilação, auditoria e ausência de campo de senha em respostas.
- [x] 2.2 Criar migrations Flyway para usuários, perfis, permissões, perfil-permissão e usuário-setor com FKs, unicidade, status e constraints; verificar aplicação limpa sem editar V1–V14.
- [x] 2.3 Criar seed idempotente dos perfis `ADMINISTRADOR`, `GESTOR`, `OPERACIONAL` e permissões granulares; verificar ausência de senha, token, secret e e-mail real nos arquivos versionados.
- [x] 2.4 Implementar bootstrap administrativo explícito por configuração/comando, desabilitado por padrão e alimentado por ambiente; verificar idempotência e impossibilidade de cadastro público permanente.
- [x] 2.5 Criar repositórios e testes de isolamento por e-mail, empresa, status, perfil e associação de setor; verificar duplicidade e referências entre empresas.

## 3. Autenticação e usuários

- [x] 3.1 Implementar política de senha configurável, BCrypt e serviço de usuário; verificar que senha correta autentica, incorreta falha e texto puro nunca é persistido/logado.
- [x] 3.2 Implementar emissão e validação de JWT com `sub`, usuário, empresa, perfil, `iat`, `exp` e versão de credenciais; verificar assinatura, expiração e ausência de claims sensíveis desnecessárias.
- [x] 3.3 Implementar `POST /api/v1/auth/login` com mensagem genérica para e-mail inexistente, senha incorreta e usuário inativo/bloqueado; verificar 200/401 conforme o caso.
- [x] 3.4 Implementar `GET /api/v1/auth/me` com perfil, permissões e setores, sem senha/hash, e `PATCH /api/v1/auth/senha` validando senha atual; verificar alteração e invalidação/inelegibilidade de tokens antigos.
- [x] 3.5 Implementar CRUD paginado de usuários e ativação/desativação sob `/api/v1/empresas/{empresaId}/usuarios`; verificar que somente usuário autorizado gerencia a própria empresa.
- [x] 3.6 Implementar reset administrativo de senha sem revelar senha anterior; verificar permissão administrativa, novo hash e rejeição por usuário sem autorização.

## 4. Perfis, permissões e autorização

- [x] 4.1 Implementar principal autenticado e serviço central para permissões, empresa e setores; verificar que regras não dependem somente de `if perfil` espalhado.
- [x] 4.2 Implementar respostas JSON padronizadas para 401 e 403 sem stack trace, secret, token, senha ou hash; verificar status e corpo com MockMvc.
- [x] 4.3 Classificar e proteger endpoints de empresas, setores, funcionários, peças, máquinas e turnos com permissões de visualizar/gerenciar; verificar matriz de perfil.
- [x] 4.4 Proteger produção, paradas e importação, exigindo `PRODUCAO_IMPORTAR` e acesso ao setor alvo; verificar usuário sem permissão e usuário sem escopo.
- [x] 4.5 Proteger metas, tempos padrão, valores e analytics/desempenho/financeiro com permissões separadas; verificar que acesso à produção não concede acesso financeiro.
- [x] 4.6 Proteger relatórios e demais recursos futuros por default-deny, mantendo somente login/health explicitamente públicos; verificar que endpoint novo não fica público por omissão.

## 5. Isolamento empresarial e escopo setorial

- [x] 5.1 Aplicar validação de empresa autenticada em todos os recursos e serviços, ignorando tentativa de substituir empresa pelo `empresaId` da URL; verificar Admin A negado para Empresa B.
- [x] 5.2 Implementar associação e manutenção de setores autorizados para gestores/operacionais; verificar setores pertencentes à empresa e duplicidades.
- [x] 5.3 Integrar predicados de escopo às consultas de funcionários, produção, importações, metas, desempenho, analytics e financeiro; verificar ausência de vazamento em totais/agregações.
- [x] 5.4 Rejeitar filtro explícito de setor fora do escopo com 403 antes da consulta; verificar Pintura/KTL permitidos e Laser proibido.
- [x] 5.5 Garantir que administrador tenha alcance de todos os setores apenas da própria empresa; verificar acesso cruzado sempre negado.

## 6. Testes e documentação de segurança

- [x] 6.1 Criar testes HTTP de login, token ausente/inválido/expirado, status de usuário, `/auth/me`, alteração e reset de senha; verificar 200/401/403.
- [x] 6.2 Criar testes de perfis e permissões para administrador, gestor e operacional; verificar menor privilégio, financeiro separado e gerenciamento separado de visualização.
- [x] 6.3 Criar testes multiempresa em funcionários, produção, analytics, financeiro, metas e importações; verificar que Empresa A nunca retorna dados de B.
- [x] 6.4 Criar testes de escopo setorial em consultas e agregações, incluindo setor explícito proibido; verificar que não há vazamento indireto.
- [x] 6.5 Atualizar todos os testes existentes das TASKS 01–09 para autenticação real, sem desabilitar filtros de segurança; verificar regressão completa.
- [x] 6.6 Documentar matriz perfil × permissão × escopo, endpoints públicos/protegidos, bootstrap, logout stateless, CORS, CSRF e variáveis de ambiente; verificar que exemplos não contêm segredos.

## 7. Verificação final

- [x] 7.1 Executar `./gradlew.bat clean test` e corrigir falhas de autenticação/autorização ou regressões; verificar suíte completa verde.
- [x] 7.2 Executar `./gradlew.bat build` e revisar migrations, configuração e contratos HTTP; verificar build sem falhas.
- [x] 7.3 Executar `openspec validate usuarios-autenticacao-permissoes --type change --strict`; verificar todos os artefatos e tarefas concluídos.
