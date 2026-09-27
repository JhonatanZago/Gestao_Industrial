# Proposal

## Why

A duração bruta de uma produção não representa o tempo efetivamente produtivo quando existem interrupções. A metalúrgica precisa registrar paradas com motivos dinâmicos para calcular tempo parado e tempo produtivo sem perder o histórico operacional.

## What Changes

- Criar Motivos de Parada dinâmicos, isolados por Empresa e com exclusão lógica.
- Criar Paradas de Produção obrigatoriamente vinculadas a um Registro de Produção e Motivo.
- Validar que a parada esteja dentro da produção e que paradas ativas do mesmo registro não se sobreponham.
- Calcular duração de parada, tempo total parado e tempo produtivo a partir dos intervalos ativos.
- Evoluir respostas de produção com duração bruta, tempo parado e tempo produtivo.
- Impedir novas paradas em produção inativa ou com motivo inativo.
- Revalidar paradas ao atualizar ou reativar e impedir redução do período da produção para fora de paradas existentes.
- Criar migrations, DTOs, services, repositories, APIs, testes e documentação reutilizando o padrão existente.
- Não implementar máquinas, turnos, Excel, frontend, indicadores avançados, autenticação ou IA.

## Capabilities

### New Capabilities

- `paradas`: cadastro, associação, ciclo de vida e APIs de interrupções de uma produção.
- `motivos-parada`: catálogo dinâmico de motivos de parada por Empresa.
- `tempos-produtivos`: cálculo derivado de duração bruta, tempo parado e tempo produtivo.

### Modified Capabilities

Nenhuma capability registrada pelo CLI foi identificada para modificação. A integração com Registro de Produção será feita preservando seus endpoints e regras existentes.

## Impact

- Novas entidades `MotivoParada` e `ParadaProducao`, com migrations posteriores à V5.
- Novos endpoints `/motivos-parada` e `/producoes/{producaoId}/paradas`.
- Evolução do response de produção e da validação de update do período.
- Novos índices e consultas de sobreposição/soma por produção.
- Testes de regressão das TASKS 01–03 e novos cenários temporais.
