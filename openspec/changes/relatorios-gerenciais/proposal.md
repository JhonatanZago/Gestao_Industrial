# Proposal

## Why

O backend já reúne produção, paradas, metas, desempenho, valor industrial e analytics, mas ainda não oferece uma visão gerencial compacta nem exportações reutilizáveis. Esta change transforma os indicadores existentes em relatórios executivos e operacionais consistentes, permitindo decisão baseada em dados sem duplicar fórmulas nem expor informações fora do escopo autorizado.

## What Changes

- Criar relatórios sob demanda de resumo executivo, produção, paradas, desempenho e valor industrial.
- Compor os relatórios a partir dos serviços analíticos existentes, preservando regras de período, comparativos, cobertura e arredondamento.
- Adicionar pontos de atenção determinísticos, sem IA ou julgamentos subjetivos.
- Aplicar permissões específicas para relatórios e seções financeiras, mantendo isolamento empresarial e escopo setorial na origem das consultas.
- Disponibilizar exportações CSV e XLSX com tipos de dados corretos, limites de volume, nomes seguros, headers apropriados e proteção contra CSV Injection.
- Registrar auditoria técnica da geração sem persistir o conteúdo integral dos relatórios.
- Adicionar testes de consistência entre analytics e relatórios, permissões, escopo setorial, exportações, volume e regressão.

## Capabilities

### New Capabilities

- `relatorio-executivo`: resumo executivo composto por produção, comparativos, setores, dimensões operacionais, paradas, metas, desempenho, cobertura, valor industrial autorizado e pontos de atenção objetivos.
- `relatorios-producao`: relatórios filtráveis de produção, paradas e desempenho, com período explícito e detalhamento compacto por dimensão.
- `exportacao-relatorios`: geração de CSV e XLSX a partir do mesmo modelo autorizado do JSON, com segurança, limites e tipos corretos.

### Modified Capabilities

<!-- Nenhuma capability OpenSpec principal existente está registrada em openspec/specs/; as regras analíticas existentes serão reutilizadas como contrato de implementação. -->

## Impact

- Novos resources, DTOs, facade/services de relatório e exporters no backend Spring Boot.
- Reutilização de `AnalyticsService`, `FinanceiroIndustrialService`, `DesempenhoService` e `MetaProducaoService`, sem mover fórmulas para a camada de relatório.
- Ajustes de permissões/seeds e autorização para `RELATORIO_VISUALIZAR` e seções financeiras, se necessário.
- Uso do Apache POI já presente para XLSX; nenhuma implementação de frontend, PDF obrigatório, IA, RAG, e-mail ou agendamento.
- Novos testes HTTP, unitários e de exportação, além da regressão completa das TASKS 01–10.
