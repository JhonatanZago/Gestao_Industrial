# Proposal

## Why

As planilhas registram funcionário, peça, setor, quantidade e horários de produção, mas esses dados ainda não formam um registro transacional consistente. É necessário estruturar o primeiro registro de produção antes de importar Excel ou construir indicadores, garantindo que os envolvidos pertençam à mesma Empresa e que o histórico temporal seja confiável.

## What Changes

- Criar o Registro de Produção ligando Empresa, Setor, Funcionário e Peça.
- Validar quantidade positiva, início anterior ao fim e duração atravessando meia-noite.
- Calcular e expor duração bruta sem confundi-la com tempo produtivo.
- Rejeitar sobreposição temporal do mesmo Funcionário, permitindo intervalos adjacentes e simultaneidade de funcionários diferentes.
- Impedir novos registros com Empresa, Setor, Funcionário ou Peça inativos.
- Permitir consulta paginada com filtros por recursos, período e status.
- Permitir atualização controlada e ativação/desativação lógica sem DELETE físico.
- Criar migration, DTOs, mapper, repository, service, endpoints, testes e documentação compatíveis com as TASKS 01 e 02.
- Não implementar paradas, tempo produtivo real, Excel, frontend, autenticação, dashboards ou indicadores.

## Capabilities

### New Capabilities

- `producao`: registro, validação temporal, associação consistente de recursos, consulta e ciclo de vida de produção industrial.

### Modified Capabilities

Nenhuma capability registrada pelo CLI foi identificada para modificação. As entidades Empresa, Setor, Funcionário e Peça existentes serão consumidas por FK e permanecerão compatíveis.

## Impact

- Nova entidade persistente `RegistroProducao` e migration Flyway posterior às versões existentes.
- Novos endpoints sob `/api/v1/empresas/{empresaId}/producoes`.
- Novos DTOs, mapper, repository, service e testes de integração.
- Novos índices para consultas por Empresa, período, setor, funcionário, peça e status.
- A duração bruta será calculada sob demanda; tempo produtivo e paradas permanecem fora do escopo.
