# Integração frontend com o fluxo core

O frontend consulta os contratos reais do Backend V1:

- `GET /api/v1/empresas/{empresaId}/analytics/dashboard`
- `GET /api/v1/empresas/{empresaId}/analytics/dashboard/setor/{setorId}`
- `GET /api/v1/empresas/{empresaId}/analytics/funcionarios/{funcionarioId}`
- `GET /api/v1/empresas/{empresaId}/analytics/pecas`
- `GET /api/v1/empresas/{empresaId}/relatorios/simplificado`

Os valores `quantidadeProduzida`, `setoresComDados`, `funcionariosEnvolvidos`, `codigosPecaProduzidos`, `duracaoRegistradaMinutos`, listas agrupadas e `evolucao` são opcionais no modelo. Quando ausentes, a interface mostra `-` ou estado vazio; nenhum KPI é calculado ou inventado no cliente.

Os períodos são enviados como `inicio` e `fim` ISO-8601. Identificadores operacionais são exibidos como strings para preservar zeros à esquerda.
