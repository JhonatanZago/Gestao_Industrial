# Relatórios — contrato funcional verificado

## Consulta e reconciliação

- `GET /api/v1/empresas/{empresaId}/relatorios/simplificado?inicio={ISO-local}&fim={ISO-local}[&setorId={id}]`
- Controller: `RelatoriosOperacionaisResource.simplified` → `RelatorioSimplificadoService.generate` → `AnalyticsService.coreDashboard` → consulta ativa do repositório de produção.
- Resposta JSON: `RelatorioSimplificadoResponse`, contendo `dashboard` (`quantidadeProduzida`, `setoresComDados`, `funcionariosEnvolvidos`, `codigosPecaProduzidos`, `porSetor`, `porFuncionario`, `porPeca`, `evolucao`, `inicio`, `fim`) e resumo operacional.
- Setores: `GET /api/v1/empresas/{empresaId}/setores`; o seletor usa essa resposta e oferece “Todos os setores” sem inventar opções.
- Dashboard de comparação: `GET /api/v1/empresas/{empresaId}/analytics/dashboard` aceita o mesmo intervalo e setor e usa `AnalyticsService.coreDashboard`.
- Acesso autenticado via bearer/JWT e permissão `RELATORIO_VISUALIZAR`; setor fora do escopo é validado no backend.

## Exportação e recursos ausentes

- `GET /api/v1/empresas/{empresaId}/relatorios/executivo/exportar?inicio={data}&fim={data}&formato=CSV|XLSX[&setorId={id}][&top={n}]`.
- Controller retorna `byte[]`, MIME e `Content-Disposition`; formatos reais são CSV e XLSX. Frontend consome como Blob, valida MIME e prefere o filename do header.
- PDF e histórico de relatórios não foram encontrados no backend. Portanto não são oferecidos. Não há múltiplos tipos de relatório configuráveis: há uma consulta simplificada e exportação executiva.

## Testes diretos realizados (ambiente local homolog)

- Consulta: `GET /api/v1/empresas/1/relatorios/simplificado?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:59`; status 200; `Content-Type: application/json`.
- Resposta da consulta foi resumida como: produção 41.710, 3 setores, 23 funcionários, 12 códigos de peça. Mesma chamada ao endpoint de dashboard: mesmos totais e distribuição.
- Filtro KTL (`setorId=3`): status 200; produção 31.905, 1 setor, 20 funcionários e 8 códigos. Reconciliado com o endpoint Dashboard usando os mesmos filtros.
- Sem token: status 401. Export CSV: status 200, `text/csv;charset=UTF-8`, `Content-Disposition` com `relatorio-executivo-2026-09-01-a-2026-09-30.csv`. XLSX: status 200, MIME spreadsheet e filename `.xlsx` (assinatura ZIP/PK).
- Geração anterior parecia inoperante porque a chamada JSON terminava com sucesso, mas a página não apresentava a resposta nem usava o serviço Blob de exportação. Corrigido no frontend, sem alteração do backend.
- As chamadas do frontend usam caminhos relativos `/api/v1`; portanto compartilham origem com a aplicação. Não foi necessário alterar CORS. Não foi encontrada causa de 403/404/422/500 no fluxo validado.

## Mapeamento frontend

- `AppComponent` (`app.html`/`app.ts`) usa o período comum do cabeçalho, seleciona setor retornado pelo catálogo, consulta e apresenta `SimplifiedReport`.
- `ResourceService.simplifiedReport` consulta o JSON; `ResourceService.exportReport` define `responseType: 'blob'`, envia o formato e os filtros; `reportFilename` interpreta `Content-Disposition`.
- A prévia, os KPIs, série temporal, distribuição e rankings derivam exclusivamente de `dashboard` retornado. Não foi criado histórico no browser.
