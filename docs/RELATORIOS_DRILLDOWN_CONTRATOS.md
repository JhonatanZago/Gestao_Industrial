# Relatórios — drill-down de funcionários e peças

## Contratos existentes reaproveitados

- `GET /api/v1/empresas/{empresaId}/analytics/funcionarios?inicio&fim&setorId&page&size` → `AnalyticsResource.employees` → `AnalyticsService.grouped(FUNCIONARIO)` → consulta ativa de produção e agregação por funcionário → `Page<ProducaoAgrupadaResponse>` (`id`, `codigo`, `nome`, `quantidadeProduzida`, `quantidadeRegistros` e demais métricas analíticas). `size` é limitado a 100 pelo serviço; o cliente segue `totalPages` para compor a lista completa.
- `GET /api/v1/empresas/{empresaId}/analytics/pecas?inicio&fim&setorId&page&size` usa o mesmo pipeline agrupado por peça e contrato paginado. Os filtros existentes também permitem `funcionarioId` e `pecaId`.
- `GET /api/v1/empresas/{empresaId}/analytics/funcionarios/{funcionarioId}?inicio&fim&setorId` → `AnalyticsResource.employee` → `AnalyticsService.employeeDetail` → registros filtrados/metrics/repositórios → `FuncionarioAnaliticoResponse` (produção, quantidade de registros, métricas existentes e listas relacionadas limitadas a 100).
- `GET /api/v1/empresas/{empresaId}/analytics/serie-temporal?inicio&fim&granularidade=DIA&setorId&funcionarioId|pecaId` → `AnalyticsService.timeSeries` → séries diárias calculadas sobre os registros filtrados.
- `GET /api/v1/empresas/{empresaId}/analytics/dashboard?inicio&fim&setorId[&pecaId]` → `AnalyticsService.coreDashboard` → totais e distribuição até o limite de agrupamentos definido pelo serviço. Usado para total do escopo, participação e métricas agregadas, não como fonte da lista completa.

As chamadas exigem sessão bearer e autorização de analytics definida pelo backend; o frontend não altera nem contorna essa autorização.

## Limitações explícitas

- Não existe endpoint dedicado `/analytics/pecas/{pecaId}`. O detalhe de peça usa os filtros `pecaId` existentes no dashboard, série temporal e endpoint paginado de funcionários. Não foi adicionado endpoint novo.
- Os contratos analíticos retornam agregados e quantidades de registros, não linhas individuais de produção nem dias distintos. O frontend não mostra registros brutos nem KPI de dias com produção.
- O detalhe de funcionário não retorna uma série temporal própria, mas `/serie-temporal` já aceita `funcionarioId`; as peças relacionadas completas são carregadas pelo endpoint paginado filtrado por funcionário, sem depender do array limitado do DTO de detalhe.
- Participação da peça é quantidade da peça / produção total do mesmo período e setor; participação do funcionário usa o mesmo denominador. Produção média é produção total / quantidade de entidades retornada no escopo.
- Empresa continua determinada pela sessão autenticada; período e setor são preservados nos parâmetros da URL (`period`, `setorId`) nas rotas internas `/app/relatorios/...`.
