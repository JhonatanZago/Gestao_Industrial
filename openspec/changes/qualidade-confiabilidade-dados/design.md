# Design

## Context

As fontes de verdade já estão persistidas em `RegistroProducao`, `ParadaProducao`, `TempoPadraoProducao`, `ValorProducao`, `MetaProducao`, cadastros e `ImportacaoProducao`. `AnalyticsService`, `DesempenhoService`, `FinanceiroIndustrialService` e `RelatorioExecutivoService` aplicam período, empresa e escopo setorial, mas cada resposta ainda não compartilha um diagnóstico explícito de completude ou consistência. O change de auditoria também estabelece que leitura/diagnóstico não deve gerar evento de auditoria.

## Goals / Non-Goals

**Goals:**

- Calcular diagnósticos reproduzíveis sob demanda, com regras e códigos estáveis.
- Reutilizar as mesmas regras de aplicabilidade histórica de tempo padrão, valor e meta dos serviços analíticos.
- Expor resumo, problemas, duplicidades e detalhe por recurso com paginação, período e escopo seguro.
- Integrar metadados opcionais de qualidade aos analytics e ao relatório executivo e fornecer consulta de qualidade de lote importado.
- Centralizar limiares e limites operacionais e manter observabilidade sem registrar datasets completos.

**Non-Goals:**

- Persistir uma tabela de alertas derivados na primeira versão.
- Corrigir, excluir, mesclar ou desativar dados automaticamente.
- Transformar alerta não bloqueante em validação de domínio.
- Frontend, dashboard, ML, IA/RAG, score opaco, scheduler pesado, cache complexo ou notificações.

## Decisions

### 1. Diagnóstico calculado e modelo imutável

Criar enums estáveis para categoria e severidade e um modelo de resposta/registro imutável `ProblemaQualidadeDados`, com código, mensagem, empresa, recurso, setor, data de referência e detalhes. O serviço de qualidade agregará resultados de regras pequenas e determinísticas; não haverá entidade JPA na primeira versão.

Alternativas consideradas:

- Persistir cada alerta: rejeitado inicialmente porque cria ciclo de retenção, duplicação e necessidade de invalidar resultados derivados.
- Retornar mapas genéricos: rejeitado porque dificulta contrato, filtros e evolução de consumidores.

### 2. Serviços especializados sobre uma fachada de consulta

Separar resolução de cobertura, consistência temporal/paradas, cadastro/vigência e duplicidade em componentes de domínio, coordenados por um serviço de qualidade. A fachada validará período, empresa, filtros e escopo antes de chamar as regras. O controller apenas traduz parâmetros e retorna DTOs.

Essa separação permite testar regras isoladas e evita duplicar as fórmulas existentes. Quando uma cobertura depender de padrão ou valor, a resolução por data/contexto será compartilhada ou extraída para componente comum, em vez de reimplementar precedência no diagnóstico.

### 3. APIs sob demanda e limites obrigatórios

Implementar endpoints para resumo, problemas, cobertura, duplicidades e recurso específico sob `/qualidade-dados`. Período será obrigatório nas consultas que varrem produção; paginação e tamanho máximo serão aplicados em problemas e duplicidades. A resposta de duplicidade conterá grupo e referências dos registros, não uma cópia ilimitada das entidades.

O resumo de lote será exposto por consulta própria vinculada à importação, evitando alterar obrigatoriamente o payload legado. O relatório executivo e analytics receberão bloco opcional somente quando a compatibilidade de DTO permitir.

### 4. Consultas e performance

Usar Specifications, projections e consultas agregadas para contagens de cobertura e regras filtráveis. Duplicidades exatas serão agrupadas no banco por empresa e período antes de materializar grupos; regras aproximadas somente serão habilitadas por configuração e com janela limitada. Regras que exigirem detalhes carregarão IDs e campos mínimos, evitando N+1 e carregamento de milhões de entidades.

Índices novos só serão incluídos após verificar as consultas efetivas; migrations antigas não serão editadas. Se nenhum índice adicional for necessário, a decisão será registrada nos testes/revisão do change.

### 5. Configuração centralizada

Criar `@ConfigurationProperties` para limites e limiares: duração máxima, janela de futuro, duração curta, taxa suspeita, quantidade suspeita, cobertura de alerta, tolerância temporal opcional, tamanho máximo de página e máximo de registros analisáveis. Defaults serão conservadores e documentados; nenhuma regra terá magic number espalhado.

### 6. Segurança na origem

Adicionar `QUALIDADE_DADOS_VISUALIZAR` aos seeds e aplicar a permissão nas rotas. Cada consulta validará o `empresaId` do JWT, usará `SectorScopeService` na query e validará IDs de filtros. Indicadores de cobertura podem informar contagens de registros sem valor a usuários operacionais, mas detalhes monetários dependem de `FINANCEIRO_INDUSTRIAL_VISUALIZAR`.

### 7. Integrações compatíveis

O bloco `qualidadeDados` será opcional e omitido quando não houver dados calculáveis ou quando o contrato atual não o solicitar, preservando consumidores existentes. O relatório executivo reutilizará a mesma fachada e seus períodos/escopos. Importações não serão bloqueadas por ausência de padrão ou valor; após o lote, o diagnóstico poderá ser consultado sem criar auditoria por leitura.

### 8. Auditoria e correção

Nenhuma execução de resumo, problema, cobertura ou duplicidade chamará `AuditoriaService`. Se uma futura operação corrigir cadastro ou produção, ela deverá passar pelo serviço de negócio existente, que auditará a mudança normalmente.

## Risks / Trade-offs

- **[Risco]** Regras calculadas sob demanda podem ser custosas em históricos grandes → **Mitigação:** período obrigatório, limites, projections, agregações no banco, logs de duração e testes de volume.
- **[Risco]** Alertas de legado podem refletir limitações do modelo atual → **Mitigação:** emitir somente regras compatíveis com campos existentes e expor detalhes observados, sem inferir causa.
- **[Risco]** Cobertura financeira pode permitir inferência indireta → **Mitigação:** separar cobertura operacional de detalhes monetários e testar respostas sem permissão financeira.
- **[Risco]** Alterar DTOs de analytics/relatório pode quebrar clientes → **Mitigação:** bloco opcional, endpoint próprio quando necessário e testes de compatibilidade dos contratos atuais.
- **[Risco]** Duplicidade aproximada gera falsos positivos → **Mitigação:** iniciar por igualdade exata, tornar tolerância opt-in/configurável e nunca excluir automaticamente.
- **[Risco]** Dados inválidos não podem ser inseridos pelo modelo atual → **Mitigação:** validar regras com fixtures controladas/migrations de teste ou consultas de legado quando possível, sem relaxar constraints de domínio.

## Migration Plan

1. Adicionar configuração e modelos/serviços/queries de qualidade sem alterar migrations existentes.
2. Adicionar permissão e rotas protegidas, mantendo endpoints anteriores inalterados.
3. Integrar blocos opcionais a analytics/relatório e consulta de lote de importação.
4. Executar testes determinísticos e regressão completa; publicar apenas após `clean test`, `build` e validação OpenSpec verdes.
5. Rollback: remover as rotas e integrações opcionais; como não há tabela de alertas derivados, não existe migração de dados de qualidade para reverter.

## Open Questions

Nenhuma decisão pendente altera o contrato ou a arquitetura desta change. A escolha de adicionar índices específicos poderá ser confirmada pela análise das queries durante a implementação, sem mudar o comportamento observável.
