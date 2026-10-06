# Central de importações automáticas — frontend

## Contrato utilizado

- Status: `GET /api/v1/empresas/{empresaId}/importacoes/automaticas/status`.
- Histórico: `GET /api/v1/empresas/{empresaId}/importacoes/producao`.
- Detalhe: `GET /api/v1/empresas/{empresaId}/importacoes/producao/{importacaoId}`.

O status é protegido por `OPERACAO_MONITORAR`; as consultas respeitam o escopo de empresa do backend. A tela usa somente os campos dos DTOs reais e não apresenta paths, UNC, IPs, credenciais, tokens ou stacktrace.

O histórico suporta paginação backend e os filtros existentes `setorId`, `status`, `nomeArquivo`, `dataInicio` e `dataFim`. Não há filtro backend por `origem`; por isso a origem é apenas apresentada na tabela e não é simulada filtrando a página atual.

## Estados

`ATIVO`, `DESATIVADO` e `ATENCAO` são apresentados a partir do status global retornado. Fontes usam os estados retornados pelo backend. Loading, vazio e erro possuem mensagens próprias; o erro oferece nova tentativa.
