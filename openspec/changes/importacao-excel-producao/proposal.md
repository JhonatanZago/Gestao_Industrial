# Proposal

## Why

As planilhas Excel ainda são uma fonte operacional da metalúrgica, a ausência de uma importação controlada obriga lançamentos manuais e impede rastreabilidade confiável. A integração precisa validar o arquivo inteiro antes de gravar produção, reaproveitar as regras de domínio existentes e oferecer um relatório auditável de cada tentativa.

## What Changes

- Adicionar suporte seguro a arquivos `.xlsx`, com seleção controlada de aba, mapeamento por cabeçalhos/aliases e parsing explícito de textos, datas, horários e números.
- Criar pré-validação e importação de produção com contexto obrigatório de Empresa e Setor, aceitando máquina e turno opcionais conforme os cadastros existentes.
- Criar auditoria de importação com hash SHA-256, status de workflow, contagens, timestamps e resultados por linha com códigos de erro estáveis.
- Aplicar estratégia all-or-nothing: nenhuma produção será persistida se qualquer linha ou conflito do arquivo for inválido.
- Impedir reimportação do mesmo conteúdo concluído no mesmo contexto e permitir novo processamento somente quando o arquivo corrigido tiver hash diferente.
- Expor endpoints de validar, importar, listar histórico e consultar detalhe, sem frontend, exportação, modo parcial, mensageria ou dashboard.

## Capabilities

### New Capabilities

- `importacao-excel`: leitura segura de XLSX, escolha de aba, aliases de cabeçalhos, normalização e modelo intermediário.
- `validacao-importacao`: resolução de referências, validações por linha, conflitos internos/banco, atomicidade e relatório de erros.
- `historico-importacao`: auditoria, hash, status, rastreabilidade por linha e APIs de histórico/detalhe.

### Modified Capabilities

Nenhuma capability principal versionada existe em `openspec/specs/`; a importação será adicionada como fluxo novo que reutiliza os contratos existentes de produção, máquinas, turnos e paradas sem alterar silenciosamente seus requisitos.

## Impact

- Nova dependência Apache POI para `.xlsx` e configuração de limite de upload.
- Novas entidades/migrations para importação e resultados de linha, além de origem/rastreabilidade opcional em produção.
- Novos services de parsing, validação, resolução em lote e persistência transacional.
- Novos endpoints multipart sob `/api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao`.
- Testes unitários com fixtures sintéticas XLSX e testes de integração H2; regressão das TASKS 01–05 obrigatória.
