# Importacao Excel

O formato oficial esta em `docs/PADRAO_PLANILHA_PRODUCAO.md`. O template de homologacao esta em `docs/templates/PRODUCAO_PADRAO.xlsx` e o fluxo de consumo em `docs/FLUXO_EXCEL_DASHBOARD.md`.

## Endpoints

- `POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/validar`
- `POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao`
- `GET /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao`
- `GET /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/{importacaoId}/linhas`

O multipart usa o campo `file`. O setor e escolhido no caminho e deve estar ativo e autorizado para a empresa/usuário. O nome do arquivo é apenas auditoria.

## Resposta e erros

A resposta registra `status`, `totalLinhas`, `linhasValidas`, `linhasInvalidas`, `linhasImportadas`, setor, hash, correlation ID e código/mensagem de erro. Linhas rejeitadas ficam disponíveis no endpoint de linhas com número, códigos e motivo. Stack traces, caminhos locais e segredos nunca são retornados.

O processamento e all-or-nothing. Falha em arquivo, cabecalho, linha, referência, setor ou duplicidade não grava produção parcial. A mesma combinação de conteúdo, empresa e setor não pode ser importada novamente após conclusão.

## Dashboard e relatório core

- `GET /api/v1/empresas/{empresaId}/analytics/dashboard`
- `GET /api/v1/empresas/{empresaId}/analytics/dashboard/setor/{setorId}`
- `GET /api/v1/empresas/{empresaId}/relatorios/simplificado`

Todos exigem `inicio` e `fim`; filtros opcionais são `setorId`, `funcionarioId` e `pecaId`. Os totais usam duração registrada por linha e não inferem parada a partir de gaps.

## Segurança

O backend preserva autenticação JWT, isolamento por empresa/setor, limites de linhas e ZIP, aceitação somente de XLSX, proteção contra arquivos malformados, path traversal e fórmulas em células de dados. Máquinas, turnos, metas, qualidade, financeiro e IA/RAG não são obrigatórios para o formato oficial.
