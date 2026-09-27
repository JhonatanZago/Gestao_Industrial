# Proposal

## Why

O sistema precisa substituir a dependência inicial de planilhas por uma base organizacional persistida para a metalúrgica. Antes de cadastrar funcionários, peças e produção, é necessário representar empresas e seus setores de forma extensível, preservando histórico e permitindo que novos setores sejam cadastrados sem alteração de código.

## What Changes

- Criar o cadastro e o ciclo de vida de Empresas, com ativação e desativação lógica.
- Criar o cadastro de Setores vinculados a exatamente uma Empresa.
- Permitir setores dinâmicos, sem enum ou regras condicionadas a nomes fixos.
- Expor APIs REST versionadas para CRUD, listagem paginada, filtros básicos e ativação/desativação de Empresas e Setores.
- Aplicar normalização, validações, regras de unicidade e isolamento entre empresas.
- Adicionar DTOs, mappers, services, repositories e tratamento global padronizado de erros.
- Adicionar migrations Flyway, configuração de PostgreSQL/H2, testes automatizados e README inicial.
- Não implementar Funcionário, Peça, Produção, importação Excel, dashboards, autenticação, frontend ou IA nesta change.

## Capabilities

### New Capabilities

- `empresa-setores`: cadastro, relacionamento, APIs, validações e ciclo de vida de empresas e setores industriais dinâmicos.

### Modified Capabilities

Nenhuma capacidade existente foi identificada; `openspec list --specs` não retornou especificações registradas.

## Impact

- Backend Spring Boot em Java 17, com Spring Web, Spring Data JPA, Jakarta Validation, Lombok, Flyway e PostgreSQL.
- Novos modelos persistentes Empresa e Setor, constraints de unicidade e migrations versionadas.
- Novos endpoints sob `/api/v1/empresas` e `/api/v1/empresas/{empresaId}/setores`.
- Nova estrutura de testes unitários e de integração usando JUnit 5, Mockito e H2.
- O projeto atual é um esqueleto mínimo, portanto a implementação deverá estabelecer a organização inicial de pacotes e a configuração executável sem antecipar módulos fora do escopo.
