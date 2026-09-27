# Design

## Context

O projeto já possui uma implementação funcional da TASK 01, com entidades Empresa/Setor, APIs aninhadas, DTOs, mappers manuais, services, repositories, `@RestControllerAdvice`, Flyway, H2 para testes e PostgreSQL para runtime. A motivação desta change está em `proposal.md`; os contratos estão em `specs/funcionarios/spec.md` e `specs/pecas/spec.md`.

## Goals / Non-Goals

**Goals:**

- Adicionar Funcionário e Peça seguindo os mesmos padrões de camadas, respostas e erros existentes.
- Preservar códigos empresariais como texto, incluindo zeros à esquerda e formatos alfanuméricos.
- Garantir vínculo obrigatório com Empresa, unicidade por Empresa, isolamento e exclusão lógica.
- Preparar referências estáveis para a futura entidade Registro de Produção.

**Non-Goals:**

- Não adicionar Registro de Produção, setor de produção, quantidade, horários, Excel, frontend, autenticação ou relatórios.
- Não adicionar valor, tempo padrão, meta, cliente ou roteiro à Peça.
- Não criar exclusão física nem mecanismo de busca avançado.

## Decisions

### Modelo e relacionamento

Criar entidades `Funcionario` e `Peca` com `@ManyToOne` obrigatório para Empresa, timestamps e `ativo`. Os códigos serão colunas `VARCHAR` e as tabelas terão constraints únicas em `(empresa_id, codigo)`. O padrão de busca por ID será `findByIdAndEmpresaId`, como na relação Empresa–Setor, para impedir acesso cruzado.

Alternativa rejeitada: usar `Long` para códigos, pois removeria zeros à esquerda e impediria códigos alfanuméricos. Também não haverá uso do ID técnico como código de integração.

### Setor principal do Funcionário

Não será criado `setorPrincipal` nesta change. A lotação atual pode mudar e não deve limitar a produção futura em outros setores; adicionar esse vínculo agora criaria regras extras de status, transferência e compatibilidade sem ser necessário para os contratos atuais. O Registro de Produção deverá guardar explicitamente o setor onde cada produção ocorreu. Essa decisão mantém Funcionário e Peça independentes de um setor fixo.

Alternativa considerada: adicionar um relacionamento opcional com Setor, mas foi rejeitada por não agregar comportamento necessário nesta etapa. Uma futura change poderá introduzir lotação se houver requisito administrativo claro.

### Normalização e validação

Códigos serão trimados, nunca convertidos numericamente, e terão limite máximo razoável documentado na API/modelo. Nome do Funcionário e descrição da Peça serão obrigatórios, trimados e limitados. A validação antecipada produzirá conflito compreensível; constraints de banco protegerão concorrência.

### APIs e consultas

Criar Resources aninhados por Empresa, requests/responses separados e mappers manuais. Services resolverão Empresa, normalização, duplicidade, atualizações e status. Repositories usarão consultas derivadas para paginação e filtros por ativo, código e nome/descrição. Respostas serão `Page<DTO>` para manter o padrão existente e evitar exposição JPA.

### Migrations e regressão

Adicionar migrations novas após `V2`, sem alterar migrations já aplicadas. Criar tabelas com FK para Empresa, constraints de unicidade e índices coerentes. Os testes de integração reutilizarão H2 e limparão as tabelas novas antes de cada caso; a suíte existente da TASK 01 deverá continuar passando.

## Risks / Trade-offs

- [Dados reais podem exigir códigos maiores] → usar limites amplos de `VARCHAR`, sem impor formato numérico ou tamanho fixo.
- [Conflito entre validação e concorrência] → manter checagem no service e constraint única no banco.
- [Necessidade futura de lotação] → deixar a associação fora desta change e registrar o setor no Registro de Produção, evitando migração prematura.
- [Diferença H2/PostgreSQL] → manter migrations simples e executar a suíte completa em H2; validar PostgreSQL no ambiente de implantação.

## Migration Plan

1. Aplicar migrations novas após as de Empresa e Setor.
2. Publicar entidades, repositories, services, DTOs, mappers e resources.
3. Executar regressão da TASK 01 e testes novos em H2.
4. Em rollback, reverter a aplicação sem apagar dados; não alterar nem remover migrations anteriores.
