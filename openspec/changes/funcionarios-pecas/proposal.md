# Proposal

## Why

As planilhas industriais identificam registros principalmente por códigos de funcionários e peças. Antes de importar ou registrar produção, o sistema precisa de cadastros confiáveis, estáveis e isolados por empresa para preservar zeros à esquerda, aceitar códigos alfanuméricos e manter referências históricas.

## What Changes

- Criar o cadastro de Funcionários vinculados obrigatoriamente a uma Empresa.
- Criar o cadastro de Peças vinculadas obrigatoriamente a uma Empresa.
- Tratar códigos de Funcionário e Peça como `String`, preservando zeros, letras e hífens quando aplicável.
- Aplicar unicidade de código por Empresa, normalização, validações e exclusão lógica.
- Expor APIs REST paginadas para cadastro, consulta, atualização, filtros e ativação/desativação.
- Garantir isolamento: recursos de uma Empresa não podem ser acessados pelo escopo de outra.
- Manter Funcionários e Peças independentes de um setor fixo; o setor da produção será definido na change de Registro de Produção.
- Reutilizar DTOs, mappers, services, repositories, migrations e tratamento de erros existentes.
- Não implementar Registro de Produção, importação Excel, frontend, autenticação, indicadores ou IA.

## Capabilities

### New Capabilities

- `funcionarios`: cadastro, identificação empresarial, ciclo de vida e APIs de Funcionários por Empresa.
- `pecas`: cadastro, identificação empresarial, ciclo de vida e APIs de Peças por Empresa.

### Modified Capabilities

Nenhuma capability registrada pelo CLI foi identificada para modificação. A change anterior permanece compatível e será coberta pela suíte de regressão.

## Impact

- Novas entidades persistentes `Funcionario` e `Peca`, com foreign key para `Empresa` e constraints `(empresa_id, codigo)`.
- Novas migrations Flyway e índices para consultas por empresa, código, nome e descrição.
- Novos endpoints sob `/api/v1/empresas/{empresaId}/funcionarios` e `/api/v1/empresas/{empresaId}/pecas`.
- Novos DTOs, mappers, services, repositories e testes de integração no package-base existente.
- A implementação prepara referências futuras de Registro de Produção sem criar essa entidade ou impor setor fixo.
