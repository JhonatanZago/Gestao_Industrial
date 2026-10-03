# Análise de impacto — simplificação Excel → Dashboard

## Baseline

- Backend: `./gradlew.bat test --no-daemon` — `BUILD SUCCESSFUL`.
- Frontend: `npm test -- --watch=false` — 6 arquivos e 15 testes aprovados.
- Frontend: `npm run build` — bundle de produção gerado com sucesso.
- O primeiro disparo do frontend no sandbox falhou por `Access denied` ao resolver caminhos; a repetição autorizada fora do sandbox passou. O Gradle precisou de `GRADLE_USER_HOME` dentro do workspace porque `C:\.gradle` não é gravável.

## Inventário e dependências

| Área | Componentes atuais | Dependências principais | Impacto |
|---|---|---|---|
| CORE | `ExcelImportReader`, `ImportacaoValidator`, `ImportacaoProducaoService`, `ImportacaoLifecycleService` | empresa, setor, funcionário, peça, produção, histórico de importação | Simplificar formato e regras sem quebrar histórico |
| CORE | `RegistroProducao`, `ImportacaoProducao`, `ResultadoLinhaImportacao` | migrations V5, V10 e V11, repositories e mappers | Preservar rastreabilidade e origem |
| CORE | `AnalyticsService`, `AnalyticsResource` | produção, setor, funcionário, peça, escopo de setor | Reconciliar resumo, agrupamentos e série temporal |
| CORE | `SetorResource`, `FuncionarioResource`, `PecaResource` | empresa, autorização e cadastros | Permitir seleção e validação do setor |
| MANTER | `RegistroProducaoResource`, `ImportacaoHistoricoResource` | APIs existentes e frontend | Compatibilidade de CRUD e histórico |
| SIMPLIFICAR | `RelatoriosOperacionaisService`, `RelatorioExecutivoService` | analytics, metas, desempenho, financeiro, qualidade | Criar/usar resumo core independente de dados opcionais |
| DESACOPLAR | paradas, máquinas, turnos, metas, desempenho, qualidade, financeiro | módulos e migrations V6–V9, V12–V14 e posteriores | Não bloquear a planilha de seis colunas |
| DESACOPLAR | IA/RAG e assistente | documentos, permissões e integrações próprias | Não participar do caminho crítico |
| MANTER | segurança, autorização, auditoria e observabilidade | filtro JWT, escopo empresa/setor, correlation ID | Aplicar as mesmas regras ao novo fluxo |
| OCULTAR_DO_FRONTEND | endpoints secundários quando não necessários ao fluxo | cliente atual em `frontend/src/app/core` | Não remover backend funcional |
| CANDIDATO_A_REMOCAO_FUTURA | nenhum módulo funcional identificado | decisão fora desta mudança | Não remover nesta implementação |

## Rotas reutilizáveis

- Importação: `POST /api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao/validar` e `POST .../importacoes/producao`.
- Setores: `GET/POST/PUT/PATCH /api/v1/empresas/{empresaId}/setores`.
- Dashboard/analytics: `GET /api/v1/empresas/{empresaId}/analytics/resumo`, `/setores`, `/funcionarios`, `/funcionarios/{id}`, `/pecas` e `/serie-temporal`.
- Relatórios: resources operacionais e executivo sob `/api/v1/empresas/{empresaId}/relatorios`.
- O frontend já consome o contexto de setor no upload e o resumo de analytics; não é necessário criar uma API paralela.

## Lacunas que a implementação deve fechar

1. O leitor reconhece aliases e colunas opcionais de setor, máquina e turno; o formato oficial precisa ser explícito e o setor do caminho não pode ser sobrescrito.
2. A quantidade é lida como `BigDecimal`; o fluxo oficial precisa rejeitar frações.
3. A regra atual calcula suporte a intervalo noturno; a regra oficial exige `fim > início` no mesmo registro.
4. O analytics atual também carrega paradas e expõe métricas secundárias; o resumo core deve continuar válido sem esses recursos.
5. Os índices existentes cobrem empresa/início, setor/início, funcionário/período e peça/início; qualquer novo índice precisa ser justificado por teste.
6. A fonte do relatório executivo inclui metas, desempenho, qualidade e financeiro; a saída simplificada precisa reconciliar diretamente com produção e analytics core.

## Resultado da classificação

O caminho mínimo será `Setor autorizado → XLSX → leitor/validador → ImportacaoProducao → RegistroProducao → Analytics → relatório`. O restante permanece disponível, mas não poderá impedir esse fluxo quando não estiver presente na planilha.
