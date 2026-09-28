# Proposal

## Why

O sistema já registra quantidade, contexto, tempo produtivo e desempenho, mas ainda não consegue associar uma produção a um valor industrial historicamente válido. Sem essa camada, comparações financeiras podem misturar moedas, aplicar valores atuais a registros antigos ou confundir valor processado com receita fiscal.

## What Changes

- Adicionar valores de produção versionados por empresa, peça, setor e máquina opcional, com tipo semântico, moeda ISO, vigência e ciclo de vida lógico.
- Resolver o valor aplicável por registro usando primeiro o contexto específico da máquina e depois o valor geral do setor, sempre pela data de início da produção.
- Calcular valor produzido com `BigDecimal`, cobertura financeira, valor por hora bruta/produtiva e indicadores separados para valor industrial e receita estimada.
- Disponibilizar CRUD paginado de valores, ativação/desativação e filtros por contexto, tipo, moeda, ativo e vigência.
- Disponibilizar resumo, agregações por setor, funcionário, peça, máquina e turno, série temporal e comparativos financeiros.
- Preservar a separação entre valor industrial, receita estimada e faturamento contábil/fiscal; não implementar custos, lucro, câmbio ou perda financeira por parada.
- Reutilizar os registros manuais e importados, filtros e períodos dos analytics existentes, evitando consultas N+1 na resolução histórica.

## Capabilities

### New Capabilities

- `valor-producao`: cadastro, moeda, vigência, validação, fallback e aplicação do valor de referência.
- `historico-valores`: preservação e resolução histórica de valores por data, máquina e contexto.
- `analytics-financeiro-industrial`: cálculos monetários, cobertura, agregações, valor/hora, séries e comparativos financeiros.

### Modified Capabilities

Nenhuma capacidade existente será modificada em nível de requisitos. A mudança integra-se aos analytics e à produção existentes sem alterar contratos ou semântica dos indicadores físicos atuais.

## Impact

- **Persistência:** nova tabela `valor_producao`, enums, FKs, constraints, índices e migração Flyway sem editar migrations anteriores.
- **Domínio e aplicação:** entidade, repositório, serviço de resolução em lote, DTOs, mapper e recurso REST sob `/api/v1/empresas/{empresaId}`.
- **Analytics:** novos endpoints financeiros e respostas específicas para quantidade, valor, moeda, cobertura, valor/hora e comparativos.
- **Regras:** `BigDecimal` para todos os valores monetários; moedas diferentes nunca serão somadas sem conversão.
- **Fora de escopo:** frontend, NF-e, faturamento real, impostos, descontos, contas a receber, custos completos, margem, lucro, câmbio, ERP e dinheiro perdido por parada.
