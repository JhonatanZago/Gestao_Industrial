# Spec Delta

## Purpose

Disponibilizar problemas de qualidade, possíveis duplicidades e diagnósticos por recurso em consultas controladas, paginadas e explicáveis para operação e investigação histórica.

## ADDED Requirements

### Requirement: Consultar problemas de qualidade com filtros e paginação

O sistema MUST oferecer `GET /api/v1/empresas/{empresaId}/qualidade-dados/problemas` com período obrigatório, paginação limitada e filtros opcionais por categoria, severidade, setor, tipo de recurso e código. Cada item MUST retornar código, categoria, severidade, mensagem, recurso, setor, data de referência e detalhes permitidos. A ordenação MUST ser determinística.

#### Scenario: Filtrar problemas críticos de um setor

- **WHEN** o cliente informa período, categoria, severidade `CRITICO` e setor autorizado
- **THEN** a API MUST retornar somente problemas que atendam a todos os filtros, em página limitada e sem dados de outros setores

#### Scenario: Tamanho de página inválido

- **WHEN** o cliente solicita tamanho zero, negativo ou acima do limite configurado
- **THEN** a API MUST rejeitar a consulta antes de executar uma busca irrestrita

### Requirement: Detectar e consultar possíveis duplicidades sem exclusão automática

O sistema MUST oferecer `GET /api/v1/empresas/{empresaId}/qualidade-dados/duplicidades` paginado. A detecção inicial MUST priorizar duplicidade exata por empresa, funcionário, peça, setor, início, fim, quantidade, máquina e turno; tolerância temporal somente pode ser usada quando configurada. A resposta MUST informar `grupoDuplicidade` e os registros relacionados, sem excluir, mesclar ou corrigir qualquer registro.

#### Scenario: Dois apontamentos idênticos

- **WHEN** existem dois registros com funcionário 00147, peça 54871, setor Pintura, período 08:00–10:00 e quantidade 300
- **THEN** a API MUST retornar um grupo com os dois registros relacionados e MUST preservar ambos na base e nos endpoints operacionais

#### Scenario: Registros parecidos fora da tolerância

- **WHEN** dois registros têm contexto igual, mas diferença temporal superior à tolerância configurada
- **THEN** o sistema MUST NOT agrupá-los como duplicidade aproximada

### Requirement: Consultar qualidade de um recurso específico

Quando aplicável, o sistema MUST oferecer `GET /api/v1/empresas/{empresaId}/qualidade-dados/recursos/{tipoRecurso}/{recursoId}` para retornar problemas detectados para um recurso sem expor registros de outra empresa ou setor não autorizado. Recurso inexistente ou pertencente a outra empresa MUST resultar em resposta segura e não revelar sua existência.

#### Scenario: Recurso com múltiplos diagnósticos

- **WHEN** uma produção possui duração excepcional e não possui padrão aplicável
- **THEN** a consulta do recurso MUST retornar os dois diagnósticos, cada um com código, severidade e detalhes próprios

#### Scenario: Recurso de outra empresa

- **WHEN** o cliente informa o identificador de produção de outra empresa
- **THEN** a API MUST retornar erro de escopo ou recurso não encontrado sem incluir mensagem, contagem ou detalhe da empresa estranha

### Requirement: Não persistir nem auditar cada diagnóstico derivado

Na primeira versão, diagnósticos MUST ser calculados sob demanda ou derivados de consultas controladas, sem criar `EventoAuditoria` para cada leitura e sem persistir alertas derivados quando não houver necessidade operacional explícita. Uma futura ação de correção MUST passar pelo fluxo de negócio correspondente e ser auditada como alteração real.

#### Scenario: Repetir consulta não cria auditoria

- **WHEN** um usuário consulta resumo, problemas e duplicidades várias vezes
- **THEN** o sistema MUST retornar os diagnósticos calculados sem criar eventos de auditoria de leitura ou alterar os dados operacionais

### Requirement: Respeitar empresa, setor e permissões financeiras

Todas as consultas de qualidade MUST validar empresa autenticada e aplicar escopo setorial na origem da consulta. Usuários operacionais podem receber indicadores de ausência/cobertura financeira, mas não valores unitários, totais ou detalhes monetários sem `FINANCEIRO_INDUSTRIAL_VISUALIZAR`.

#### Scenario: Gestor limitado à Pintura

- **WHEN** existem problemas e duplicidades em Pintura e Laser, mas o gestor está associado somente à Pintura
- **THEN** resumo, problemas, duplicidades, cobertura e consulta por recurso MUST omitir completamente registros, contagens e agregados de Laser

#### Scenario: Diagnóstico financeiro sem autorização

- **WHEN** um usuário sem permissão financeira consulta cobertura de valor industrial
- **THEN** a resposta pode informar quantidade e percentual de registros sem valor, mas MUST omit valores unitários, totais monetários e moeda quando isso permitir inferência financeira indevida
