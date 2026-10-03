# Integração Frontend/API

| Feature | Endpoint real | Método | Permissão/escopo | Observação |
|---|---|---|---|---|
| Login | `/api/v1/auth/login` | POST | Público | Token stateless; descarte no logout |
| Sessão | `/api/v1/auth/me` | GET | Autenticado | Perfil e permissões reais |
| Dashboard | `/api/v1/empresas/{empresaId}/analytics/resumo` | GET | Empresa + setor | Não existe endpoint dedicado dashboard |
| Analytics | `/api/v1/empresas/{empresaId}/analytics/*` | GET | Empresa + filtros | Resumo, setores, funcionários, peças, máquinas, turnos, paradas e séries |
| Produção | `/api/v1/empresas/{empresaId}/producoes` | GET/POST/PUT/PATCH | Empresa + permissão | Paginação e estado ativo |
| Cadastros | `/api/v1/empresas/{empresaId}/setores`, `/funcionarios`, `/pecas`, `/maquinas`, `/turnos` | GET/POST/PUT/PATCH | Empresa + permissão | CRUD conforme recurso real |
| Importação | `/api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao` | POST | Empresa + setor | Validar antes de persistir |
| Qualidade | `/api/v1/empresas/{empresaId}/qualidade-dados/*` | GET | Empresa + permissão | Resumo, problemas, duplicidades e recurso |
| Relatórios | `/api/v1/empresas/{empresaId}/relatorios/*` | GET | Empresa + permissão | Exportação real CSV/XLSX quando disponível |
| Auditoria | `/api/v1/empresas/{empresaId}/auditoria` | GET | Auditoria | Nunca expor secrets |
| Assistente | `/api/v1/empresas/{empresaId}/assistente/perguntar` | POST | IA + empresa/setor | Grounding, sem chain-of-thought; estados retornados pelo backend |
| RAG | `/api/v1/empresas/{empresaId}/assistente/documentos` | POST/DELETE | AI_DOCUMENTO_GERENCIAR + escopo | Upload/remoção; backend devolve empresa/setor/status |

## Implementação do cliente

`frontend/src/app/core/feature-services.ts` concentra paginação, CRUD, estado ativo/inativo, analytics, qualidade, importação multipart, relatórios binários, auditoria, assistente e RAG. Filtros opcionais vazios são removidos; IDs, empresa e setor são enviados sem transformação de regra de negócio.

O fluxo de importação é `POST .../validar` seguido de `POST .../importacoes/producao`; o cliente só mostra contagens/status retornados pelo backend. Exportações aceitas são apenas `CSV` e `XLSX`, com nome e MIME lidos dos headers. Auditoria passa por sanitização visual recursiva para remover token, senha, segredo, autorização, credencial e API key.

Fixtures com `File`, páginas vazias e respostas `SEM_DADOS` existem exclusivamente nos arquivos `*.spec.ts`; não há dataset produtivo fake habilitado por environment.

Erros 401/403/429/500/503 são tratados de forma segura; quando houver correlation ID, ele aparece como código de suporte. DTOs e nomes finais devem ser conferidos nos controllers antes de cada feature.
