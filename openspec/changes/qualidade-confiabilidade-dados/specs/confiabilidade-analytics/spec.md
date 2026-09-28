# Spec Delta

## Purpose

Integrar métricas de qualidade aos cálculos analíticos, importações e relatórios existentes de forma compatível, preservando fórmulas, escopo, permissões financeiras e transparência sobre dados incompletos.

## ADDED Requirements

### Requirement: Integrar qualidade aos analytics sem duplicar métricas

Os responses analíticos principais MUST poder incluir bloco opcional `qualidadeDados` com cobertura de tempo padrão, cobertura de valor, registros com alertas e registros críticos. Os valores-base de produção, desempenho e finanças MUST continuar usando as regras existentes e os metadados de qualidade MUST ser derivados da mesma empresa, período, filtros e escopo. A ausência do bloco para clientes que não o solicitarem MUST manter compatibilidade do contrato atual.

#### Scenario: Analytics com cobertura parcial

- **WHEN** o resumo analítico é consultado para um período em que 80% dos registros têm padrão e 90% têm valor
- **THEN** o bloco de qualidade MUST refletir 80% e 90%, enquanto quantidade, duração, tempo produtivo e valor seguem os valores-base já definidos

#### Scenario: Analytics sem produção

- **WHEN** o período não possui registros autorizados
- **THEN** o response MUST preservar métricas vazias/zero conforme o contrato existente e retornar cobertura sem inventar registros ou alertas

### Requirement: Integrar qualidade ao relatório executivo

O relatório executivo MUST poder incluir uma seção de qualidade dos dados com coberturas, alertas críticos e possíveis duplicidades, além de mensagens objetivas baseadas em limiares centralizados. A seção MUST respeitar os mesmos períodos, filtros, empresa e setores do relatório e MUST omitir qualquer valor financeiro não autorizado.

#### Scenario: Relatório evidencia cobertura baixa

- **WHEN** a cobertura de tempo padrão do período é 60% e o limiar configurado é cruzado
- **THEN** o relatório pode incluir uma mensagem mensurável sobre os 40% sem padrão, sem inventar causa ou classificar pessoas/setores subjetivamente

#### Scenario: Relatório sem permissão financeira

- **WHEN** o usuário possui permissão de relatório, mas não possui permissão financeira
- **THEN** a seção de qualidade não pode revelar valores unitários, totais, moedas ou campos derivados que permitam reconstruir o financeiro

### Requirement: Resumir qualidade após importação

Após uma importação concluída, o sistema MUST poder retornar ou consultar resumo do lote com linhas importadas, linhas com alerta, possíveis duplicidades, registros sem tempo padrão e registros sem valor. Ausência de configuração de padrão ou valor MUST gerar diagnóstico não bloqueante, salvo regra de negócio explícita, e a falha/rollback do lote MUST não ser apresentada como concluída.

#### Scenario: Lote válido com cobertura incompleta

- **WHEN** um lote importa produções válidas, mas parte delas não possui padrão ou valor aplicável
- **THEN** o resumo do lote MUST informar as contagens de qualidade sem rejeitar automaticamente as linhas válidas

#### Scenario: Lote rejeitado

- **WHEN** a validação do arquivo rejeita o lote antes de persistir produções
- **THEN** o resultado MUST preservar o status de rejeição e MUST NOT criar diagnósticos de produção persistida para linhas que não existem

### Requirement: Preservar segurança e compatibilidade nas integrações

Qualidade MUST respeitar isolamento por empresa, escopo setorial e permissões existentes em analytics, importação e relatórios. Novos campos MUST ser opcionais ou disponibilizados em endpoint próprio; nenhuma API anterior pode começar a misturar empresas, vazar Laser para gestor de Pintura ou conceder acesso financeiro por causa do diagnóstico.

#### Scenario: Mesma base, escopo diferente

- **WHEN** administrador e gestor consultam o mesmo período, mas o gestor possui somente um setor
- **THEN** os valores de qualidade do gestor MUST ser calculados sobre sua base autorizada, e não sobre o resumo global posteriormente mascarado

### Requirement: Executar diagnósticos com limites previsíveis

As consultas MUST exigir período ou aplicar janela máxima configurada, limitar paginação e volume de duplicidades, preferir agregações/projections/consultas específicas e evitar carregar toda a base ou executar N+1. Falhas técnicas MUST ser registradas sem logar datasets completos.

#### Scenario: Consulta acima do limite

- **WHEN** o período, tamanho de página ou volume solicitado excede o limite configurado
- **THEN** a API MUST rejeitar claramente a solicitação sem construir uma resposta ilimitada em memória

#### Scenario: Diagnóstico reproduzível

- **WHEN** a mesma base, configuração, empresa, setor e período são analisados novamente
- **THEN** os códigos, contagens, severidades e detalhes MUST ser determinísticos, salvo alterações reais nos dados ou na configuração
