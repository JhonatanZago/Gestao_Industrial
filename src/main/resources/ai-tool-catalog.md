# Catálogo interno de tools do assistente

As tools abaixo são versionadas (`1`), somente leitura e executadas por
`DefaultAiToolCatalog`. O contexto de empresa, usuário, permissões e setores é
sempre obtido do JWT; argumentos não podem ampliar o escopo.

| Tool | Permissão | Fonte |
| --- | --- | --- |
| `producao_resumo`, `producao_comparacao`, `producao_serie_temporal` | `ANALYTICS_VISUALIZAR` | `AnalyticsService` |
| `producao_por_setor`, `producao_por_funcionario`, `producao_por_peca`, `producao_por_maquina`, `funcionario_detalhe`, `paradas_por_motivo` | `ANALYTICS_VISUALIZAR` | `AnalyticsService` |
| `desempenho_resumo`, `metas_desempenho`, `desempenho_por_setor` | `ANALYTICS_VISUALIZAR` | `DesempenhoService` |
| `valor_industrial_resumo`, `valor_industrial_comparacao` | `FINANCEIRO_INDUSTRIAL_VISUALIZAR` | `FinanceiroIndustrialService` |
| `qualidade_resumo` | `QUALIDADE_DADOS_VISUALIZAR` | `QualidadeDadosService` |
| `auditoria_resumo` | `AUDITORIA_VISUALIZAR` | `AuditoriaConsultaService` |

Todas recebem `inicio`/`fim` em ISO-8601 e aceitam filtros de entidade quando
aplicável. Resultados carregam a fonte lógica, período e avisos para a camada de
grounding.
