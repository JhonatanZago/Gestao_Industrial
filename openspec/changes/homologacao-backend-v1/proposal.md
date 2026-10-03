# Proposal

## Why

As TASKS 01--17 do Backend V1 estão concluídas, mas ainda falta uma validação integrada que prove que os contratos, escopos empresariais/setoriais, cálculos, importação, IA/RAG, observabilidade, segurança, banco e documentação funcionam em conjunto. Esta change fecha a fase de backend com uma homologação reproduzível e uma decisão explícita de GO/NO-GO baseada em evidências, sem introduzir novas funcionalidades.

## What Changes

- Criar uma massa sintética determinística com duas empresas e dados industriais conhecidos para os fluxos de homologação.
- Validar o fluxo E2E oficial, do login ao assistente IA/RAG, reutilizando dados produzidos nas etapas anteriores.
- Criar fixtures XLSX válidas e inválidas e verificar validação, persistência all-or-nothing, analytics, dashboard e relatórios.
- Homologar autenticação, perfis, permissões, isolamento empresarial/setorial, IDOR/BOLA, auditoria, financeiro e respostas de erro.
- Conferir cálculos oficiais de produção, paradas, metas, desempenho, valor industrial e qualidade de dados entre os módulos.
- Reexecutar testes de segurança, observabilidade, resiliência, performance, migrations em banco vazio e contratos dos endpoints críticos.
- Corrigir somente defeitos comprovados na homologação e adicionar testes de regressão correspondentes; não adicionar features, frontend ou integrações externas.
- Revisar e completar README, documentação operacional, arquitetura, banco, execução, deploy, segurança, performance, IA/RAG, importação, changelog, coleção de API e smoke test.
- Produzir matriz de aceite, inventário de TODO/FIXME/HACK, análise de dependências/CVEs, relatório final e decisão explícita `BACKEND V1: GO` ou `BACKEND V1: NO-GO`.

## Capabilities

### New Capabilities

- `backend-acceptance`: critérios e cenários de aceite funcional, matemático, de segurança e de isolamento para o Backend V1.
- `end-to-end-validation`: massa sintética, fixtures XLSX, fluxo E2E, testes de contrato/smoke, regressão, performance e validação de banco.
- `release-documentation`: documentação final do backend, matriz de evidências, API, operação, configuração, arquitetura e relatório de homologação.
- `deployment-readiness`: preparação agnóstica para execução, migrations, health checks, observabilidade, rollback e release/tag, sem publicação remota automática.

### Modified Capabilities

Nenhuma capability existente está publicada como spec principal em `openspec/specs`; as requirements desta change serão novas e deverão refletir somente o comportamento já implementado no código.

## Impact

- Código potencialmente afetado apenas por correções encontradas nos módulos existentes de resources, services, repositories, segurança, importação, analytics, relatórios, auditoria, qualidade, IA/RAG, observabilidade e configuração.
- Testes unitários, integração, contrato, segurança, E2E e performance, incluindo fixtures sintéticas e scripts reproduzíveis.
- Banco PostgreSQL/H2, migrations Flyway, Docker Compose e ambiente de execução; nenhuma migration histórica deverá ser editada.
- README, `CHANGELOG.md`, documentação em `docs/`, coleção de API/environment de exemplo e artefatos OpenSpec.
- A suíte padrão continuará independente de internet e APIs pagas; o frontend e a publicação remota permanecem fora do escopo.
