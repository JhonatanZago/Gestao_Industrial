# Design

## Context

As entidades atuais `RegistroProducao` e `ParadaProducao` já possuem empresa, setor, período e ciclo de vida lógico, mas não possuem referências a recurso produtivo ou turno. O projeto usa Spring Boot, JPA, Flyway, DTOs, mappers, services e resources REST, com H2 na suíte de integração.

## Goals / Non-Goals

**Goals:**

- Modelar uma máquina/recuso produtivo dinâmico vinculado atualmente a setor e empresa.
- Modelar turnos dinâmicos com horários diurnos e noturnos e duração derivada.
- Adicionar associações opcionais e compatíveis a produção e parada, com validações no service.
- Manter históricos consultáveis e os cálculos de tempo existentes.

**Non-Goals:**

- Divisão automática de produção entre turnos ou determinação automática de turno.
- Histórico de movimentação de máquinas entre setores.
- Intervalos de almoço, calendário operacional, manutenção ou disponibilidade/OEE.
- Paradas independentes de uma produção, frontend e importação Excel.

## Decisions

### Recurso produtivo representado por `Maquina`

Usar `Maquina` como nome técnico, com campo textual `tipo` para classificar máquinas, linhas, fornos, cabines e células. Isso mantém o domínio simples e extensível sem enum com nomes concretos da empresa; uma entidade genérica `RecursoProdutivo` adicionaria indirection sem requisito atual.

### Horários de turno com `LocalTime`

Persistir `horaInicio` e `horaFim` como horários locais. A duração será calculada como minutos entre os horários e, quando o fim não for posterior ao início, com um dia adicional. Igualdade será inválida; não haverá persistência de duração redundante.

### Migrations incrementais

Criar V8 para as tabelas `maquina` e `turno`, com unicidade por empresa e FKs sem cascade remove. Criar V9 para as colunas opcionais `maquina_id` e `turno_id` em `registro_producao` e `maquina_id` em `parada_producao`, além dos índices de consulta por recurso/turno e período. Nenhuma migration V1-V7 será editada.

### Validação centralizada nos services

Resources recebem DTOs e delegam ao service. Services resolvem empresa, setor, máquina e turno no escopo correto, validam status e regras de compatibilidade antes de salvar. Os vínculos opcionais permanecem nulos quando omitidos.

### Compatibilidade das respostas

Adicionar blocos resumidos opcionais `maquina` e `turno` às respostas de produção e um resumo opcional de máquina às respostas de parada. Os campos anteriores permanecem inalterados. A produção pode atravessar troca de turno, mas mantém no máximo um turno explícito.

### Histórico após inativação

Desativação será lógica. Cadastros inativos não entram em novas associações, mas continuam sendo retornados quando fazem parte de produção ou parada já registrada, evitando quebrar históricos.

## Risks / Trade-offs

- [Risco] Uma máquina pode ser movida de setor e deixar históricos antigos apontando para o setor atual. → [Mitigação] Esta task documenta que o setor é a alocação atual; histórico de movimentação fica para change futura.
- [Risco] Uma produção atravessa a janela de um turno ou a meia-noite. → [Mitigação] Não dividir nem inferir automaticamente; aceitar o turno explicitamente informado e testar os limites.
- [Risco] Alterar construtores de services pode afetar testes ou injeção. → [Mitigação] Usar injeção existente, atualizar testes de integração e executar regressão completa.
- [Risco] Consultas por máquina/turno podem gerar custo crescente. → [Mitigação] Criar índices compostos com período e manter listagens paginadas.

## Migration Plan

1. Aplicar V8 e V9 em banco novo ou existente; as novas colunas aceitam nulo.
2. Implantar código que mantém associações opcionais e não altera registros históricos existentes.
3. Validar suite H2 e build antes da disponibilização.
4. Rollback de código pode ocorrer sem remoção das colunas; rollback de banco deve usar procedimento operacional aprovado, pois migrations aplicadas não serão editadas.
