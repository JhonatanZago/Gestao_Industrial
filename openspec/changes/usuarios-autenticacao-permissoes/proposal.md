# Proposal

## Why

As APIs do sistema atualmente não identificam o autor da requisição nem aplicam autorização por empresa, perfil ou setor. Com dados de produção, funcionários, metas, desempenho e valor industrial já disponíveis, é necessário estabelecer identidade autenticada e menor privilégio antes de expor o backend a consumidores reais.

## What Changes

- Adicionar usuários vinculados a uma empresa, status de acesso, e-mail normalizado, hash de senha e perfil principal.
- Adicionar perfis `ADMINISTRADOR`, `GESTOR` e `OPERACIONAL`, permissões granulares e associação perfil-permissão.
- Adicionar escopo de setores para gestores e isolamento obrigatório pela empresa da identidade autenticada.
- Implementar login stateless com Bearer JWT assinado por segredo de ambiente, expiração configurável e endpoints `/auth/login`, `/auth/me` e alteração de senha.
- Implementar cadastro, consulta, atualização, ativação/desativação e reset administrativo de senha de usuários.
- Proteger as APIs existentes com default-deny, diferenciação consistente de 401/403 e permissões específicas para financeiro, importação, metas, tempos padrão, valores e analytics.
- Configurar CORS por ambiente, CSRF coerente com API stateless, tratamento seguro de erros e logs sem credenciais.
- Criar migrations, seeds técnicos idempotentes, testes HTTP de autenticação/autorização, isolamento entre empresas e escopo setorial.
- Manter refresh token, recuperação por e-mail, MFA, SSO, OAuth social, frontend e rate limiting distribuído fora desta change.

## Capabilities

### New Capabilities

- `usuarios`: cadastro, ciclo de vida, senha e vínculo empresarial dos usuários.
- `autenticacao`: login, JWT, expiração, identidade atual, alteração de senha e respostas 401.
- `autorizacao`: default-deny, permissões, isolamento empresarial e respostas 403.
- `perfis-permissoes`: perfis base, permissões granulares e associação perfil-permissão.
- `escopo-setorial`: autorização de gestores e filtros de setor aplicados na consulta.

### Modified Capabilities

Nenhuma especificação principal registrada será modificada. A proteção dos endpoints existentes é uma mudança transversal de autorização e será descrita como integração nos novos contratos, preservando os comportamentos funcionais anteriores quando o usuário estiver autorizado.

## Impact

- **Persistência:** novas tabelas `usuario`, `perfil`, `permissao`, `perfil_permissao` e `usuario_setor`, com FKs, unicidade, status e seeds técnicos; migrations antigas não serão editadas.
- **Dependências:** Spring Security compatível com Spring Boot 4.1.1 e biblioteca/implementação JWT compatível, somente se necessária.
- **Aplicação:** filtros de autenticação, principal de segurança, serviços de autorização, resolução de escopo, `@PreAuthorize`/equivalente e revisão de todos os recursos REST.
- **API:** novos endpoints de autenticação e usuários; endpoints anteriores deixam de ser públicos e passam a exigir permissões.
- **Configuração:** `JWT_SECRET`, `JWT_EXPIRATION` e origens CORS vêm do ambiente; nenhum segredo real será versionado.
- **Testes:** login, hashes, tokens expirados/inválidos, perfis, permissões, isolamento entre empresas, escopo setorial, financeiro e importação.
