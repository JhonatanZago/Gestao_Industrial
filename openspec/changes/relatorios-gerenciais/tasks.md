# Tasks

## 1. Contratos, contexto e autorizacao

- [x] 1.1 Definir value objects, DTOs compostos e validadores de periodo/comparativo para relatorios; verificar compilacao e testes de contrato para intervalos invalidos e periodo anterior automatico.
- [x] 1.2 Adicionar ou confirmar a permissao `RELATORIO_VISUALIZAR`, mapear `/relatorios/**` no filtro de autorizacao e preservar empresa/setor/financeiro; verificar 401, 403, acesso por perfil e ausencia de vazamento cross-company.
- [x] 1.3 Centralizar limites de periodo, top/linhas, thresholds de insights e exportacao em configuracao; verificar validacao de limites e comportamento documentado sem magic numbers espalhados.

## 2. Facade de relatorios e consistencia analitica

- [x] 2.1 Implementar contexto autorizado e facade de relatorios que reutilize AnalyticsService, financeiro, desempenho e metas sem duplicar formulas; verificar por teste que valores-base coincidem com os endpoints analytics equivalentes.
- [x] 2.2 Implementar relatorio executivo sob demanda com identificacao, producao, comparativo, setores, funcionarios, pecas, maquinas, turnos, paradas, metas, desempenho, cobertura e secao financeira condicional; verificar contrato JSON e ausencia de detalhes historicos excessivos.
- [x] 2.3 Implementar pontos de atencao deterministas com thresholds centralizados; verificar cenarios de variacao de parada, atingimento de meta, cobertura de padrao e cobertura financeira sem julgamentos subjetivos.
- [x] 2.4 Criar testes deterministas com 10.000 pecas, 600 minutos brutos, 60 parados, meta 11.000 e valor R$ 8.500, verificando 90,91% de meta e 10% de parada; verificar tambem o comparativo 10.000/8.000, 8.500/7.000 e 60/80.

## 3. Relatorios operacionais

- [x] 3.1 Implementar `/relatorios/producao` com filtros de periodo, setor, funcionario, peca, maquina e turno e agregacoes compactas por dimensao; verificar filtros, top bounded e consistencia com analytics.
- [x] 3.2 Implementar `/relatorios/paradas` com totais, percentuais, ocorrencias e motivos; verificar duracoes, categorias, percentuais e periodos sem dados.
- [x] 3.3 Implementar `/relatorios/desempenho` com quantidade real/esperada, indice, cobertura e registros com/sem padrao; verificar cobertura parcial explicita e autorizacao.
- [x] 3.4 Criar testes HTTP de empresa, escopo setorial, filtros explicitos fora do escopo e menor privilegio para os relatorios operacionais; verificar que a filtragem ocorre antes das agregacoes.

## 4. Seguranca financeira e auditoria

- [x] 4.1 Omitir secao, campos e totais financeiros quando faltar `FINANCEIRO_INDUSTRIAL_VISUALIZAR`; verificar JSON sem valores mascarados ou inferiveis para administrador, gestor e operacional.
- [x] 4.2 Registrar auditoria tecnica com usuario, empresa, tipo, periodo, formato e timestamp, sem conteudo integral, secret ou credencial; verificar logs estruturados e ausencia de dados sensiveis.
- [x] 4.3 Documentar endpoints, permissoes, escopo, comparativos, limites e semantica de valor industrial como valor produzido, nao faturamento; verificar exemplos sem segredos e alinhamento com o contrato.

## 5. Exportacao CSV

- [x] 5.1 Implementar exporter CSV UTF-8 com separador, cabecalhos, quoting, quebras de linha, Content-Type e Content-Disposition sanitizado; verificar acentos, aspas, delimitadores e nome previsivel.
- [x] 5.2 Sanitizar campos textuais com prefixos `=`, `+`, `-` ou `@` contra CSV Injection sem alterar valores numericos; verificar casos de formula-like text e dados normais.
- [x] 5.3 Integrar exportacao CSV ao relatorio executivo e garantir paridade de filtros, escopo, permissoes e omissao financeira; verificar que gestor nao recebe setor proibido nem colunas financeiras.

## 6. Exportacao XLSX

- [x] 6.1 Implementar exporter XLSX com abas Resumo, Setores, Funcionarios, Pecas, Maquinas, Paradas, Metas e, quando autorizado, Financeiro; verificar workbook legivel e abas condicionais.
- [x] 6.2 Escrever datas, numeros, percentuais e monetarios em tipos de celula corretos, com limites de linhas e sem auto-size indiscriminado; verificar tipos via Apache POI e rejeicao de exportacao acima do limite.
- [x] 6.3 Integrar endpoint `exportar?formato=CSV|XLSX`, headers e tratamento de formato invalido; verificar 200, content types, filename seguro, 400 para formato/limite invalido e 403 por permissao.
- [x] 6.4 Criar testes de paridade JSON/CSV/XLSX, escopo setorial e ausencia financeira em abas, colunas, totais e celulas ocultas; verificar que nenhum exporter faz consulta fora do contexto autorizado.

## 7. Integracao e documentacao

- [x] 7.1 Adicionar testes de volume sintetico razoavel para relatorios e exportacoes; verificar tempo/memoria aceitaveis dentro dos limites definidos sem benchmark complexo.
- [x] 7.2 Atualizar README com endpoints, parametros, formatos, limites, headers, permissao financeira, escopo, CSV Injection e semantica de valor industrial; verificar comandos e exemplos executaveis sem segredos.
- [x] 7.3 Executar a regressao das TASKS 01-10 com `./gradlew.bat clean test`; verificar que autenticacao, autorizacao, analytics e importacao continuam verdes.
- [x] 7.4 Executar `./gradlew.bat build` e `openspec validate relatorios-gerenciais --type change --strict`; verificar build, contratos, artifacts e tasks completos.
