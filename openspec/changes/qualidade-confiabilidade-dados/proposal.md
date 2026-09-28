# Proposal

## Why

Os analytics e relatórios atuais calculam métricas sobre dados operacionais, padrões de tempo, valores industriais, paradas e importações, mas ainda não informam de forma uniforme quando a base está incompleta, inconsistente ou sujeita a duplicidade. Isso pode produzir números matematicamente válidos, porém com confiabilidade limitada, especialmente em históricos e dados importados.

Esta change cria uma camada determinística de qualidade de dados para tornar cobertura, alertas e limitações visíveis sem corrigir ou apagar dados automaticamente.

## What Changes

- Criar diagnósticos sob demanda para cobertura de tempo padrão e valor industrial, consistência temporal, quantidades/taxas suspeitas, paradas impossíveis, cadastros incompletos, vigências conflitantes, lacunas históricas e possíveis duplicidades.
- Padronizar códigos estáveis, categorias e severidades (`INFO`, `ALERTA`, `CRITICO`) para problemas de qualidade.
- Expor resumo, cobertura, problemas, duplicidades e consulta por recurso em APIs paginadas e limitadas por período.
- Calcular qualidade por empresa, setor e período, respeitando autenticação, escopo setorial e permissões financeiras.
- Integrar o resumo de qualidade aos lotes de importação, analytics e relatório executivo sem duplicar as regras analíticas existentes.
- Centralizar limites configuráveis para duração, taxa, quantidade, cobertura, duplicidade e volume de consulta.
- Preservar contratos existentes e manter alertas não bloqueantes; erros de domínio continuam sendo tratados pelas validações atuais.

## Capabilities

### New Capabilities

- `qualidade-dados`: regras, severidades, códigos e resumo de cobertura e confiabilidade dos dados industriais.
- `diagnosticos-dados`: consulta paginada de problemas, duplicidades e diagnósticos por recurso, setor e período.
- `confiabilidade-analytics`: metadados de qualidade e integração compatível com analytics, importações e relatórios.

### Modified Capabilities

Nenhuma capability principal existente está registrada em `openspec/specs/`; as integrações serão descritas como deltas de comportamento nas novas capabilities.

## Impact

- Novos serviços de diagnóstico, DTOs, configurações, queries/projections e endpoints sob `/api/v1/empresas/{empresaId}/qualidade-dados`.
- Possível migration apenas se uma decisão posterior justificar persistir resultados ou índices; a primeira versão deve calcular sob demanda e não persistir alertas derivados sem necessidade.
- Alterações compatíveis nos responses de analytics e do relatório executivo para incluir metadados opcionais de qualidade, sem expor valores financeiros a usuários não autorizados.
- Integração de resumo de qualidade no fluxo de importação e nos modelos de relatório, reutilizando `AnalyticsService`, `DesempenhoService`, `FinanceiroIndustrialService`, `SectorScopeService` e as regras de vigência já existentes.
- Novos testes determinísticos de cobertura, histórico, duplicidade, inconsistência temporal, escopo, permissões, paginação, performance básica e regressão das TASKS 01–12.
