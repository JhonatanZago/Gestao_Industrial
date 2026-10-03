# Contratos reais do dashboard interativo

Este documento registra a origem dos dados usados pelo redesign do frontend. O cliente não cria indicadores de negócio quando o backend não fornece o campo correspondente.

| Área visual | Origem real | Regra de exibição |
| --- | --- | --- |
| Produção total | `CoreDashboard.quantidadeProduzida` | Exibir somente quando presente; formatar em pt-BR. |
| Setores com dados | `CoreDashboard.setoresComDados` | Exibir somente quando presente. |
| Funcionários envolvidos | `CoreDashboard.funcionariosEnvolvidos` | Exibir somente quando presente. |
| Peças produzidas | `CoreDashboard.codigosPecaProduzidos` | Exibir somente quando presente. |
| Duração | `CoreDashboard.duracaoRegistradaMinutos` | Exibir somente quando presente; não converter em eficiência. |
| Produção por setor | `CoreDashboard.porSetor[]` | Ordenar por `quantidadeProduzida`; nomes e IDs são dinâmicos. |
| Top funcionários | `CoreDashboard.porFuncionario[]` | Limitar a cinco na home; preservar `codigo` como texto. |
| Top peças | `CoreDashboard.porPeca[]` | Limitar a cinco na home; preservar `codigo` como texto. |
| Evolução | `CoreDashboard.evolucao[]` ou serviço de série temporal homologado | Mostrar apenas pontos reais; sem série sintética. |
| Resumo do período | `inicio`, `fim`, `quantidadeRegistros` e agregações reais | Dias/médias só quando deriváveis com segurança. |

Os endpoints principais são:

- `GET /api/v1/empresas/{empresaId}/analytics/dashboard`
- `GET /api/v1/empresas/{empresaId}/analytics/dashboard/setor/{setorId}`
- `GET /api/v1/empresas/{empresaId}/analytics/serie-temporal`
- `GET /api/v1/empresas/{empresaId}/analytics/funcionarios`
- `GET /api/v1/empresas/{empresaId}/analytics/pecas`
- `GET /api/v1/empresas/{empresaId}/relatorios/simplificado`

O backend atual não autoriza inventar OEE, lucro, faturamento, parada ou eficiência na tela. Quando um payload não traz uma métrica, o painel deve informar indisponibilidade ou permanecer em estado vazio.
