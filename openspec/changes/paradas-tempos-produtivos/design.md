# Design

## Context

O sistema possui Registro de Produção com `LocalDateTime` para o evento industrial, `OffsetDateTime` para auditoria, status lógico e consulta paginada por Specification. Motivos precisam ser dados dinâmicos; paradas devem depender de uma produção existente. A motivação está em `proposal.md` e os contratos em `specs/motivos-parada`, `specs/paradas` e `specs/tempos-produtivos`.

## Goals / Non-Goals

**Goals:**

- Criar catálogo dinâmico de motivos e paradas contidas em produções.
- Garantir isolamento empresarial, status, limites temporais e sobreposição.
- Calcular tempos derivados ignorando paradas inativas.
- Integrar validação de paradas à atualização do período da produção.

**Non-Goals:**

- Não criar enum de motivos específicos, máquinas, turnos, intervalos planejados, Excel, frontend ou indicadores avançados.
- Não persistir durações derivadas nem apagar paradas em cascata ao desativar produção.

## Decisions

### Motivo e categoria

`MotivoParada` será entidade com código único por Empresa. Categoria, se armazenada, será uma String de classificação de alto nível, não um enum que restrinja motivos específicos; isso permite evolução sem migration/código para cada causa operacional.

### Intervalos

`ParadaProducao` usará `LocalDateTime` como produção. O intervalo será semiaberto para conflito: `existente.inicio < novo.fim` e `existente.fim > novo.inicio`, permitindo adjacência. Contenção permitirá `parada.inicio >= producao.inicio` e `parada.fim <= producao.fim`, com duração positiva.

### Consultas e cálculos

Repository terá existência de sobreposição no banco e soma de durações ativas da produção. Como os intervalos já são validados e não se sobrepõem, a soma simples é consistente. Responses de produção calcularão métricas sob demanda; listagens não incluirão toda a coleção de paradas para evitar N+1. Um endpoint específico lista as paradas.

### Integração com produção

O service de Registro de Produção consultará se existem paradas fora do novo intervalo antes de aceitar update de `inicio/fim`. O service de parada validará produção/motivo ativos no create/update/reactivate. Não haverá cascade remove; FK e status preservam o histórico.

### Auditoria e concorrência

Eventos usam o mesmo `LocalDateTime` e fuso operacional definido na TASK 03; auditoria usa `OffsetDateTime`. Criação, atualização e reativação serão transacionais e consultarão o banco. A proteção contra corrida entre writers dependerá da transação e da consulta; uma constraint temporal portátil não será adicionada nesta etapa.

## Risks / Trade-offs

- [Corridas simultâneas na criação de paradas] → consulta transacional no banco; avaliar locking/constraint temporal quando houver necessidade de alta concorrência.
- [Categoria evoluir] → armazenar texto opcional de alto nível, sem enum de motivos.
- [Cálculo repetido] → manter duração derivada e somar somente paradas ativas da produção; índices por produção/status apoiam a consulta.
- [Update de produção com paradas] → consulta de existência fora do novo intervalo antes da alteração, com teste específico para fim igual ao fim da parada.

## Migration Plan

1. Criar migrations V6 para `motivo_parada` e V7 para `parada_producao`, com FKs, constraints e índices.
2. Implementar entidades, repositories, DTOs, mappers, services e resources.
3. Integrar métricas e validação de update no Registro de Produção.
4. Executar regressão completa e, em rollback, reverter a aplicação sem editar migrations V1–V5 nem apagar histórico.
