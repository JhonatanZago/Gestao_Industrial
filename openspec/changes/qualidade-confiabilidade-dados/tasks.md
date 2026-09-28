# Tasks

## 1. Contratos, configuração e fundação

- [x] 1.1 Confirmar linha de base executando `./gradlew.bat clean test` e `./gradlew.bat build`; registrar o resultado antes de alterar código.
- [x] 1.2 Criar enums estáveis de categoria e severidade, modelo imutável de problema, DTOs de resumo/cobertura e DTOs de duplicidade; verificar serialização de nulos, datas, BigDecimal, acentos e detalhes.
- [x] 1.3 Criar `@ConfigurationProperties` para limites de período, paginação, duração, futuro, taxa, quantidade, cobertura, tolerância e volume; verificar defaults documentados e ausência de magic numbers nas regras.
- [x] 1.4 Adicionar `QUALIDADE_DADOS_VISUALIZAR` aos seeds sem conceder acesso financeiro implícito; verificar compatibilidade dos perfis existentes e atualização limpa do banco de teste.
- [x] 1.5 Documentar no README os códigos, severidades, comportamento não bloqueante, ausência de correção automática e endpoints planejados; verificar que a documentação não promete persistência de alertas derivados.

## 2. Núcleo de qualidade e cobertura

- [x] 2.1 Implementar fachada de qualidade que valide empresa, período, filtros e escopo setorial antes de executar regras; verificar rejeição de empresa/sector fora do contexto.
- [x] 2.2 Implementar resolução compartilhada de aplicabilidade histórica de tempo padrão, valor industrial e meta por data e contexto; verificar que ausência não é tratada como zero nem usa configuração futura retroativamente.
- [x] 2.3 Implementar cobertura de tempo padrão com registros totais, cobertos, descobertos, percentuais, quantidades e setores/peças afetados; verificar o cenário determinístico de 100 registros e 80 cobertos.
- [x] 2.4 Implementar cobertura financeira com registros totais, cobertos, descobertos, quantidade coberta, percentual e separação por moeda quando aplicável; verificar o cenário de 100 registros e 90 valores sem revelar valores indevidos.
- [x] 2.5 Implementar resumo `GET /api/v1/empresas/{empresaId}/qualidade-dados/resumo` com período obrigatório e setor opcional; verificar resultado geral, por setor, período inválido e ausência de scan ilimitado.
- [x] 2.6 Adicionar testes unitários e de integração para cobertura, lacuna histórica de padrão, ausência financeira, empresa, setor e compatibilidade com os números dos analytics existentes.

## 3. Regras temporais, paradas e cadastros

- [x] 3.1 Implementar regras para fim anterior ao início, duração zero não permitida, datas futuras, duração longa e combinação de duração curta com quantidade/taxa suspeita; verificar códigos e severidades configuráveis.
- [x] 3.2 Implementar regras de quantidade não positiva em legado, quantidade extrema e taxa de produção fora do limite; verificar que validações de criação existentes continuam bloqueantes e alertas não corrigem registros.
- [x] 3.3 Implementar diagnóstico de parada fora da produção, fim inválido, sobreposição, parada maior que duração bruta e tempo produtivo negativo; verificar caso crítico de 60 minutos brutos e 80 parados.
- [x] 3.4 Implementar diagnóstico de cadastro incompleto somente para campos compatíveis com o modelo atual, incluindo peça, máquina e contexto operacional; verificar ausência de falsos positivos para cadastros válidos incomuns.
- [x] 3.5 Implementar diagnóstico de vigências sobrepostas e lacunas de tempo padrão, valor e meta; verificar conflitos legados sem excluir nem encerrar automaticamente configurações.
- [x] 3.6 Cobrir as regras temporais, paradas, quantidades, cadastros e vigências com testes determinísticos e fixtures controladas para dados que as APIs atuais não permitem criar.

## 4. Duplicidades e consultas de diagnóstico

- [x] 4.1 Implementar agrupamento de duplicidade exata por empresa, funcionário, peça, setor, início, fim, quantidade, máquina e turno; verificar um grupo com dois registros idênticos e preservação de ambos.
- [x] 4.2 Implementar tolerância temporal opcional e configurável somente quando habilitada; verificar que registros fora da janela não são agrupados e que falsos positivos não bloqueiam operação.
- [x] 4.3 Implementar `GET /api/v1/empresas/{empresaId}/qualidade-dados/problemas` com filtros por período, categoria, severidade, setor, recurso e código; verificar filtros combinados, ordenação e paginação limitada.
- [x] 4.4 Implementar `GET /api/v1/empresas/{empresaId}/qualidade-dados/duplicidades` paginado; verificar grupo, registros relacionados, tamanho máximo e rejeição de consultas sem limite.
- [x] 4.5 Implementar `GET /api/v1/empresas/{empresaId}/qualidade-dados/recursos/{tipoRecurso}/{recursoId}`; verificar recurso inexistente, recurso de outra empresa, recurso fora do setor e múltiplos problemas no mesmo recurso.
- [x] 4.6 Verificar que consultas repetidas não persistem problemas nem criam `EventoAuditoria`, e que nenhuma rota de qualidade permite mutação dos dados diagnosticados.

## 5. Segurança e proteção financeira

- [x] 5.1 Integrar rotas ao filtro de autorização e exigir `QUALIDADE_DADOS_VISUALIZAR`; verificar token ausente, empresa estrangeira, usuário sem permissão e perfil administrador.
- [x] 5.2 Aplicar escopo setorial na origem das consultas de resumo, problemas, cobertura, duplicidades e recurso; verificar que gestor de Pintura não recebe contagens, nomes ou grupos de Laser.
- [x] 5.3 Aplicar proteção financeira a detalhes monetários, totais, moedas e diagnósticos sensíveis; verificar que usuário operacional pode saber que há registros sem valor, mas não reconstrói valores industriais.
- [x] 5.4 Verificar filtros por setor, funcionário, peça, máquina e turno contra a empresa autenticada; garantir que identificador de outra empresa não seja ignorado silenciosamente nem cause mistura.
- [x] 5.5 Adicionar testes de resposta para ausência de segredos, dados financeiros e dados de setor proibido; confirmar que diagnósticos não são eventos de auditoria e que correções futuras continuariam usando os serviços auditados.

## 6. Integração com analytics e relatórios

- [x] 6.1 Criar bloco opcional `qualidadeDados` nos responses analíticos compatíveis, mantendo fórmulas, arredondamento, filtros e payload legado; verificar equivalência numérica entre analytics e resumo de qualidade.
- [x] 6.2 Integrar qualidade ao relatório executivo com coberturas, alertas críticos, duplicidades e pontos objetivos por limiar; verificar período, setor, compatibilidade e ausência financeira.
- [x] 6.3 Garantir que relatório e analytics calculam qualidade sobre o escopo autorizado na origem, sem gerar resumo global para mascarar setores; verificar administrador versus gestor setorial.
- [x] 6.4 Adicionar testes de contrato para respostas antigas sem bloco, respostas com bloco, baixa cobertura e ausência de produção; confirmar que nenhum ranking subjetivo ou score opaco aparece.

## 7. Integração com importação e performance

- [x] 7.1 Implementar consulta de qualidade do lote importado com linhas importadas, linhas com alerta, duplicidades, sem padrão e sem valor; verificar lote válido com cobertura incompleta.
- [x] 7.2 Verificar que importação rejeitada ou revertida não cria diagnósticos de produção inexistente e não altera o status de conclusão; preservar correlação e auditoria já existentes.
- [x] 7.3 Implementar projections/agregações/Specifications e revisar índices efetivamente usados; verificar consultas sem N+1 e registrar decisão sobre migrations adicionais sem editar migrations antigas.
- [x] 7.4 Adicionar limites de período, página, grupos, registros analisáveis e volume; verificar erro claro antes de materialização ilimitada.
- [x] 7.5 Adicionar logs técnicos de duração, regra e escopo sem conteúdo integral de datasets; verificar teste de volume sintético e comportamento aceitável de memória/tempo sem benchmark frágil.

## 8. Integração final e entrega

- [x] 8.1 Executar suíte de integração cobrindo cobertura, duplicidade, temporal, histórico, paradas, importação, analytics, relatório, permissões, empresa, setor e financeiro; verificar todos os cenários obrigatórios do TASK_13.
- [x] 8.2 Executar `./gradlew.bat clean test` e confirmar regressão verde das TASKS 01–12 e dos novos testes de qualidade.
- [x] 8.3 Executar `./gradlew.bat build`, revisar contratos, documentação, configuração e migrations, e confirmar ausência de frontend, IA/RAG, scheduler pesado e correção automática.
- [x] 8.4 Executar `openspec validate qualidade-confiabilidade-dados --type change --strict`; verificar que proposal, três specs, design e tasks estão completos e coerentes.
