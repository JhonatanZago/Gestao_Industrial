# Design

## Context

As TASKS 01 e 02 já fornecem Empresa, Setor, Funcionário e Peça com vínculo empresarial, status e APIs aninhadas. O novo agregado deve consumir essas entidades sem alterar migrations anteriores e deve manter a camada de Resource fina. A motivação está em `proposal.md` e os contratos em `specs/producao/spec.md`.

## Goals / Non-Goals

**Goals:**

- Persistir registros de produção consistentes entre os quatro recursos da mesma Empresa.
- Validar quantidade, intervalo, status dos recursos e sobreposição por Funcionário.
- Oferecer consulta paginada e filtrável sem gerar métodos combinatórios no repository.
- Expor duração bruta calculada e manter auditabilidade e exclusão lógica.

**Non-Goals:**

- Não calcular tempo produtivo real, paradas, metas, indicadores ou valor.
- Não implementar Excel, frontend, autenticação, autorização ou relatórios.
- Não persistir duração bruta redundante nem criar relacionamento de setor fixo em Funcionário ou Peça.

## Decisions

### Tempo do evento e auditoria

Usar `LocalDateTime` para `inicio` e `fim`, representando o relógio operacional da planta e aceitando payloads como `2026-09-26T08:14:00` sem offset. A aplicação documentará o fuso operacional como `America/Sao_Paulo`; conversão para instante absoluto fica para uma futura política de timezone. `createdAt` e `updatedAt` continuarão como `OffsetDateTime`, representando o momento do sistema. `Duration.between(inicio, fim)` calculará a duração bruta sob demanda.

Alternativa rejeitada: `OffsetDateTime` nos eventos agora, pois exigiria offset obrigatório nos dados das planilhas e mudaria o contrato fornecido pela task. Alternativa rejeitada: `Duration` persistida, pois seria redundante e poderia ficar inconsistente com os horários.

### Quantidade

Usar `BigDecimal` com escala de até 3 casas e precisão suficiente para unidades inteiras ou fracionadas futuras, com validação estrita `> 0`. Persistir como `DECIMAL(19,3)`.

Alternativa rejeitada: `Integer`, pois limitaria unidades fracionadas e decisões futuras de unidade de medida.

### Integridade dos relacionamentos

O service buscará Empresa, Setor, Funcionário e Peça por IDs dentro do escopo. Cada entidade será validada como pertencente à Empresa e ativa no create/update. Os objetos JPA serão ligados por `@ManyToOne(fetch = LAZY)` e a resposta será DTO com IDs, códigos e nomes, evitando serialização circular.

### Sobreposição eficiente

O repository terá uma consulta de existência parametrizada pelo funcionário, intervalo e ID opcional a ignorar: um conflito existe quando `existente.inicio < novo.fim` e `existente.fim > novo.inicio`. Essa convenção trata intervalos como semiabertos, permitindo `10:00-11:00` após `08:00-10:00`. A consulta ocorrerá no banco com índice por funcionário/início/fim; não haverá carregamento de toda a produção em memória.

### Filtros

Estender `JpaSpecificationExecutor<RegistroProducao>` e construir uma Specification no service para aplicar somente filtros preenchidos, combinando-os com AND e ordenando `inicio DESC`. Isso evita explosão de métodos derivados e permite adicionar filtros futuros sem alterar o contrato atual.

### Status e histórico

Registros terão `ativo` e endpoints de ativação/desativação. A sobreposição será verificada contra registros existentes independentemente de status, preservando a exclusividade temporal do funcionário e evitando que desativar um histórico permita duplicar o mesmo evento. Consultas históricas continuarão aceitando registros associados a recursos que foram desativados depois.

## Risks / Trade-offs

- [Relógio local sem offset] → documentar o fuso operacional e manter auditoria do sistema com offset; uma futura política multi-fuso poderá migrar eventos para instante absoluto.
- [Concorrência entre criações simultâneas] → fazer consulta no banco e manter transação; uma constraint temporal nativa não é portátil entre H2/PostgreSQL, portanto a garantia forte multiwriter poderá exigir evolução futura.
- [Intervalos muito longos] → validar somente `fim > inicio`; duração e filtros usam os tipos java.time sem limitar artificialmente atravessamento de meia-noite.
- [Filtros combinados] → usar Specification e índices de empresa/período/recursos, evitando métodos combinatórios e N+1 na resposta.

## Migration Plan

1. Adicionar migration V5 para `registro_producao` com FKs, `DECIMAL(19,3)`, timestamps, status, índices e campos temporais.
2. Publicar entidade, repository/specifications, DTOs, mapper, service e resource.
3. Executar testes de validação temporal, sobreposição, escopo, filtros e regressão das TASKS 01/02.
4. Em rollback, reverter a aplicação sem apagar registros; a migration nova não deve alterar as anteriores.
