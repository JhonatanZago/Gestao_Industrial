# Design

## Context

O backend atual é um monólito Spring Boot com resources `/api/v1`, serviços, JPA/Flyway, PostgreSQL em runtime, H2 nos testes, importação XLSX, analytics, relatórios, auditoria, qualidade de dados, IA/RAG fake/disabled e observabilidade operacional. As TASKS 01--17 e suas changes estão presentes, mas a raiz ainda não possui uma suíte E2E de fechamento nem o pacote documental completo descrito pela TASK 18. A homologação deve distinguir evidência executada de ferramenta indisponível e não pode alterar o escopo funcional.

## Goals / Non-Goals

**Goals:**

- Construir uma prova reproduzível baseada nos contratos HTTP, nos serviços reais e em dados sintéticos determinísticos.
- Verificar equivalência dos números entre importação, analytics, dashboard, relatórios e IA autorizada.
- Exercitar isolamento por empresa/setor, permissões, entradas inválidas, uploads, contratos, observabilidade, migrations e performance.
- Transformar resultados em matriz de aceite, relatório GO/NO-GO e documentação executável.
- Manter a suíte independente de internet e registrar limitações de ambiente sem convertê-las em aprovação.

**Non-Goals:**

- Não criar frontend, novos KPIs, regras financeiras, relatórios, integrações remotas ou fluxos de IA.
- Não reescrever migrations históricas, editar dados reais, publicar remotamente ou executar rollback destrutivo.
- Não tratar um resultado local como SLA de produção.

## Decisions

### 1. Validar pelo contrato público e reutilizar a suíte existente

A validação combinará MockMvc/integration tests existentes, testes de contrato para rotas críticas e um fluxo E2E com o servidor/configuração suportados pelo projeto. Isso observa comportamento real e reduz duplicação de regras em uma suíte paralela. Testes puramente unitários continuarão sendo usados para fórmulas e validadores que não exigem HTTP.

Alternativas consideradas: testar somente services, rejeitado porque não prova autenticação, serialização, filtros, escopo e status HTTP; testar somente um ambiente externo, rejeitado porque tornaria a regressão não determinística.

### 2. Usar massa determinística, fixtures pequenas e providers de IA fake

O dataset terá uma empresa de homologação, uma segunda empresa para IDOR/BOLA, setores/recursos com códigos fixos e os períodos matemáticos oficiais. Fixtures XLSX serão geradas ou mantidas como artefatos pequenos e válidos, com negativas separadas. O fake/disabled gateway será a fonte de IA da suíte, sem chamadas pagas ou internet.

Alternativas consideradas: fixture gigante versionada, rejeitada por custo e fragilidade; dados gerados aleatoriamente, rejeitados porque impedem comparar números e reproduzir defeitos.

### 3. Separar gates obrigatórios de limitações operacionais

Cada evidência será classificada como aprovada, reprovada ou não aplicável. PostgreSQL vazio, scanner de CVEs, k6 e ferramentas de contrato serão executados quando disponíveis; quando não forem, o comando, ambiente e impacto serão registrados. A ausência de uma ferramenta não poderá ser usada para declarar GO de um gate que exige sua execução.

Alternativas consideradas: ignorar ferramentas ausentes, rejeitado por mascarar cobertura; bloquear toda a change, rejeitado porque a especificação permite registrar pendências operacionais sem inventar resultados.

### 4. Corrigir somente defeitos reproduzidos e fechar cada um com regressão

A execução seguirá uma matriz de cenários. Cada falha terá severidade, passos, evidência, correção mínima e teste de regressão; a suíte será reexecutada após correções relevantes. A implementação não poderá ser usada para ampliar requisitos ou “fazer o teste passar” sem preservar o contrato.

Alternativas consideradas: corrigir preventivamente áreas não observadas, rejeitado por aumentar o risco no fechamento; aceitar falhas de segurança/integridade, rejeitado pelo critério GO/NO-GO.

### 5. Documentar recursos implementados a partir do inventário real

O README, guias operacionais, arquitetura Mermaid, configuração, API collection, smoke test e relatório serão derivados dos resources, properties, migrations, testes e documentos atuais. Formatos, endpoints, circuitos e provedores não existentes serão marcados como não aplicáveis ou futuros, nunca descritos como aprovados.

Alternativas consideradas: copiar a TASK 18 literalmente para a documentação, rejeitado porque ela inclui condicionais e itens dependentes do código real.

### 6. Preparar release sem publicar automaticamente

A saída será um checklist para build, migrations, health, observabilidade, backup/rollback e possível tag `backend-v1.0.0`. Nenhum comando terá autorização implícita para publicar, apagar dados ou alterar ambiente remoto.

## Risks / Trade-offs

- [Ambiente sem k6, scanner ou PostgreSQL alvo] → Registrar pendência e comando reproduzível; não declarar métricas ou equivalência não observadas.
- [Grande quantidade de endpoints e perfis] → Priorizar matriz derivada dos resources e agrupar cenários por contrato, mantendo pelo menos um caso positivo e negativo por fronteira crítica.
- [Diferenças entre H2 e PostgreSQL] → Executar migrations/queries em PostgreSQL quando possível e declarar explicitamente qualquer cobertura exclusiva de H2.
- [Valores do cenário oficial divergirem do comportamento real] → Tratar como defeito de contrato/cálculo, preservar a evidência e corrigir apenas após confirmar a regra existente.
- [Documentação desatualizar durante correções] → Gerar a revisão documental depois da última regressão e incluir comando/commit/ambiente no relatório.

## Migration Plan

1. Executar a suíte atual e registrar o baseline.
2. Provisionar massa/fixtures e executar os gates em ordem, preservando evidências.
3. Corrigir falhas reproduzidas com regressões e repetir os gates afetados.
4. Validar banco vazio, build, contratos, segurança, performance e documentação final.
5. Classificar pendências e emitir GO/NO-GO.
6. Em caso de rollback da change, remover apenas artefatos/alterações introduzidos pela homologação conforme o controle de versão; não editar migrations históricas nem apagar dados de ambiente automaticamente.

## Open Questions

Nenhuma questão aberta altera as requirements ou o desenho. Ferramentas e ambientes indisponíveis serão tratados como limitações observáveis durante a execução.
