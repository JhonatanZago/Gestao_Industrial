# Rotas core e permissões

| Rota | Proteção | Contrato principal |
|---|---|---|
| `/app/dashboard` | sessão | `analytics/dashboard` |
| `/app/setores` | sessão + `SETOR_VISUALIZAR` | setores autorizados |
| `/app/setores/:setorId` | sessão | `analytics/dashboard/setor/:setorId` |
| `/app/setores/:setorId/funcionarios/:codigo` | sessão | `analytics/funcionarios/:id` |
| `/app/setores/:setorId/pecas/:codigo` | sessão | analytics de peças |
| `/app/importacoes` | sessão + `PRODUCAO_IMPORTAR` | importação XLSX |
| `/app/relatorios` | sessão + `RELATORIO_VISUALIZAR` | `relatorios/simplificado` |

Sessão ausente redireciona para `/login`; permissão ausente redireciona para `/forbidden`. Os códigos de funcionário e peça são tratados como strings nas URLs e respostas.
