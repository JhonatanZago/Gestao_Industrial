# Proposal

## Why

O sistema já registra produção, paradas, empresas e setores, mas ainda não identifica o recurso produtivo nem o turno em que uma atividade ocorreu. Essa ausência impede análises industriais futuras por máquina, equipamento, setor e turno e dificulta registrar o contexto operacional sem alterar dados históricos.

## What Changes

- Criar cadastro dinâmico de recursos produtivos, usando `Maquina` como entidade de domínio abrangente para máquinas, linhas, fornos, cabines e células.
- Vincular cada máquina a uma Empresa e a um Setor atual, com código único por empresa, filtros, ativação e desativação lógicas.
- Criar cadastro dinâmico de Turnos com janela nominal baseada em `LocalTime`, incluindo turnos que atravessam meia-noite e cálculo de duração sob demanda.
- Adicionar associações opcionais de máquina e turno ao Registro de Produção, validando empresa, status ativo e compatibilidade entre máquina e setor.
- Adicionar associação opcional de máquina à Parada de Produção, validando consistência com a produção e preservando os cálculos atuais.
- Criar migrations, endpoints paginados, DTOs, services, repositories e testes de integração sem exclusão física, frontend, importação Excel ou paradas independentes de produção.

## Capabilities

### New Capabilities

- `maquinas`: cadastro e ciclo de vida de máquinas/recursos produtivos vinculados a empresa e setor.
- `turnos`: cadastro, ciclo de vida, horários e duração nominal de turnos dinâmicos.
- `alocacao-produtiva`: associação opcional de máquina e turno à produção e de máquina às paradas, com validações de consistência.

### Modified Capabilities

Nenhuma capability existente possui spec principal versionada em `openspec/specs/`; as alterações de produção e parada serão descritas como novos requisitos de `alocacao-produtiva`, sem modificar silenciosamente as specs anteriores.

## Impact

- Novas entidades, migrations Flyway V8/V9 e repositories/services/resources para máquinas e turnos.
- Novas colunas opcionais e índices em `registro_producao` e `parada_producao`.
- Evolução dos DTOs, mappers e serviços de produção/parada, mantendo payloads existentes compatíveis.
- Novos endpoints `/api/v1/empresas/{empresaId}/maquinas` e `/turnos`.
- Testes de integração H2 e regressão da suíte atual; nenhuma dependência externa nova prevista.
