# Spec Delta

## Purpose

Definir valores de referência monetários por contexto produtivo, com moeda, tipo semântico, vigência e ciclo de vida para que a produção possa ser valorizada sem ser confundida com faturamento fiscal.

## ADDED Requirements

### Requirement: Cadastro de valor de produção
O sistema MUST permitir cadastrar, consultar e atualizar um valor associado a empresa, peça, setor, máquina opcional, tipo de valor, valor unitário positivo, moeda ISO e vigência.

#### Scenario: Cadastro de valor industrial
- **WHEN** o usuário informa peça, setor, `VALOR_PRODUCAO`, `0.85`, moeda `BRL` e vigência válida
- **THEN** o sistema MUST persistir o valor com precisão monetária e os dados do contexto

#### Scenario: Valor inválido
- **WHEN** o usuário informa valor zero, negativo ou moeda que não seja um código ISO válido
- **THEN** o sistema MUST rejeitar a operação com mensagem clara

### Requirement: Vigência e conflito
O sistema MUST aceitar vigência aberta ou com fim posterior/inicial e MUST rejeitar sobreposição entre valores ativos com a mesma empresa, peça, setor, máquina, tipo e moeda.

#### Scenario: Troca de valor
- **WHEN** existe valor de `0.80` até 30 de junho e outro começa em 1º de julho no mesmo contexto
- **THEN** o sistema MUST aceitar ambos

#### Scenario: Sobreposição rejeitada
- **WHEN** um novo valor ativo se sobrepõe a outro no mesmo contexto monetário
- **THEN** o sistema MUST retornar conflito e não persistir o novo valor

### Requirement: Moeda e semântica
O sistema MUST manter a moeda explícita em cada valor e MUST distinguir `VALOR_PROCESSAMENTO`, `VALOR_PRODUCAO` e `RECEITA_ESTIMADA`; valores de processamento ou produção não podem ser apresentados como faturamento contábil/fiscal.

#### Scenario: Receita explicitamente estimada
- **WHEN** o usuário consulta um valor do tipo `RECEITA_ESTIMADA`
- **THEN** o sistema MUST identificá-lo como receita estimada e MUST informar que não representa faturamento real

#### Scenario: Moedas distintas
- **WHEN** existem valores em BRL e USD para uma consulta agregada
- **THEN** o sistema MUST separar os resultados por moeda ou rejeitar a soma sem conversão

### Requirement: Ciclo de vida e isolamento
Ativar e desativar valores MUST ser operação lógica, preservando histórico, e consultas de uma empresa MUST não retornar nem validar valores de outra empresa.

#### Scenario: Desativação histórica
- **WHEN** um valor é desativado após ter sido usado em uma produção histórica
- **THEN** o sistema MUST preservar o registro do valor e impedir sua aplicação futura

#### Scenario: Referência de outra empresa
- **WHEN** uma peça, setor ou máquina pertence a outra empresa
- **THEN** o sistema MUST rejeitar o cadastro
