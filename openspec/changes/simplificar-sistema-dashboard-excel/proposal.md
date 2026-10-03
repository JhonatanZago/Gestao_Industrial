# Proposal

## Why

O sistema já possui importação de XLSX, registros de produção, setores, analytics e relatórios, mas esses recursos ainda não formam um fluxo único e simples de negócio. A especificação adicionada em `openspec/specs/TASK_REFATORACAO_SIMPLIFICAR_EXCEL_DASHBOARD.md` define o caminho operacional oficial: planilha padronizada, setor escolhido no upload, produção validada e dashboards coerentes; esta mudança transforma esse caminho em contrato implementável sem descartar os módulos existentes.

## What Changes

- Padronizar o XLSX de produção em seis colunas obrigatórias: `Data`, `Código Funcionário`, `Código Peça`, `Quantidade`, `Hora Início` e `Hora Fim`.
- Reutilizar a importação existente com setor selecionado pelo contexto da requisição, sem inferir setor pelo nome do arquivo ou por uma coluna da planilha.
- Reforçar as regras de validação de datas, códigos textuais, quantidade inteira positiva, horários, duração e reimportação/duplicidade.
- Garantir que cada registro importado mantenha empresa, setor, funcionário, peça, origem, importação e intervalo registrado para alimentar os dashboards.
- Consolidar consultas de visão geral, setor, funcionário e peça, com filtros por período, setor, funcionário e peça e sem inventar métricas não suportadas pelos dados.
- Conectar explicitamente importação, agregações, dashboards e relatório simplificado, preservando autorização por empresa e setor.
- Criar documentação operacional, análise de impacto, fluxo Excel → Dashboard e um template XLSX com fixture determinística.
- Manter máquinas, turnos, qualidade, metas, financeiro, IA/RAG e auditoria desacoplados do caminho crítico; nenhum módulo funcional será removido nesta mudança.
- Adicionar testes unitários, integração e E2E para o fluxo completo e para as rejeições obrigatórias.

## Capabilities

### New Capabilities

- `excel-production-dashboard`: fluxo oficial de importação de produção por setor, validação, persistência, agregações, dashboards, drill-downs, relatório simplificado e seus contratos de dados.

### Modified Capabilities

Nenhuma capability registrada anteriormente foi encontrada (`openspec list --specs` não retornou especificações). A capability acima será criada como novo contrato.

## Impact

- Backend Spring Boot: `ExcelImportReader`, validação/importação, entidades e repositórios de produção/importação, analytics, setores, relatórios e autorização.
- API: revisão dos endpoints existentes de setores, importação, analytics e relatórios para evitar duplicação e garantir os filtros e respostas necessários.
- Banco/Flyway: somente migrações incrementais, índices ou ajustes seguros; migrations históricas e tabelas funcionais não serão apagadas.
- Testes e documentação: novos cenários obrigatórios, fixture e template em `docs/`.
- Frontend: não haverá reconstrução nesta mudança; será documentado o mapa futuro de telas e os contratos necessários para o consumo.
