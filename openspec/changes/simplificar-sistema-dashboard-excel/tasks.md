# Tasks

## 1. Baseline e inventario

- [x] 1.1 Executar backend e frontend tests/build atuais, registrar resultados e confirmar que a baseline esta reproduzivel antes das alteracoes.
- [x] 1.2 Mapear consumidores dos endpoints de importacao, analytics e relatorios, entidades, migrations, indices e regras de autorizacao; verificar o mapa no `docs/ANALISE_IMPACTO_SIMPLIFICACAO.md`.
- [x] 1.3 Classificar modulos como `CORE`, `MANTER`, `SIMPLIFICAR`, `OCULTAR_DO_FRONTEND`, `DESACOPLAR` ou `CANDIDATO_A_REMOCAO_FUTURA`, sem apagar codigo funcional; verificar o documento contra o inventario do repositorio.

## 2. Contrato e fixture da planilha

- [x] 2.1 Criar `docs/PADRAO_PLANILHA_PRODUCAO.md` documentando as seis colunas, tipos, exemplos, preservacao de zeros e rejeicoes; verificar que o documento nao exige `Setor` no XLSX.
- [x] 2.2 Criar `docs/templates/PRODUCAO_PADRAO.xlsx` com a fixture deterministica de Pintura a po e conferir por leitura automatizada que as tres linhas de dados, mais cabecalho, e seis colunas estao corretos.
- [x] 2.3 Ajustar testes do leitor para arquivo vazio, extensao invalida, cabecalho ausente/incompleto, celulas invalidas, formulas/conteudo inseguro, datas e horarios validos; verificar todos os casos em `ExcelImportReaderTests`.

## 3. Validacao e importacao por setor

- [x] 3.1 Ajustar leitura e normalizacao para o formato oficial, tratando codigos de funcionario/peca como texto e rejeitando quantidade fracionaria, quantidade nao positiva, data invalida e `fim <= inicio`; verificar cenarios unitarios de linha.
- [x] 3.2 Garantir validacao de empresa, setor ativo/autorizado, funcionarios e pecas ativos e referencias ausentes; verificar casos de setor inexistente, inativo, de outra empresa e sem autorizacao.
- [x] 3.3 Garantir que o setor do contexto seja aplicado a todas as linhas e que coluna `Setor` ou nome de arquivo nunca substitua esse contexto; verificar persistencia e resposta da importacao.
- [x] 3.4 Preservar o fluxo de validacao antes da persistencia e o comportamento all-or-nothing, com contagens e erros por linha/campo/valor/motivo sem stack trace; verificar a integracao de workbook misto.
- [x] 3.5 Implementar ou corrigir idempotencia por hash/conteudo + empresa + setor, duplicidade de linhas e reimportacao sem depender de filename; verificar que a segunda submissao nao cria registros.
- [x] 3.6 Verificar seguranca de upload, limite de tamanho/linhas, ZIP bomb, path traversal, macros/formulas e mensagens sanitizadas; executar a suite de falhas de importacao.

## 4. Persistencia e consultas core

- [x] 4.1 Garantir que o registro importado mantenha empresa, setor, funcionario, peca, quantidade, inicio, fim, duracao registrada, origem, importacao e linha de origem; verificar rastreabilidade em teste de integracao.
- [x] 4.2 Ajustar repositorios/consultas para filtros por periodo, setor, funcionario e peca, incluindo codigos com zeros e isolamento por empresa; verificar respostas paginadas e ausencia de vazamento entre escopos.
- [x] 4.3 Revisar indices e criar somente migrations incrementais necessarias para periodo/setor/funcionario/peca; verificar aplicacao em banco limpo e banco com historico, sem alterar migrations anteriores.

## 5. Dashboards e drill-downs

- [x] 5.1 Consolidar o resumo geral com total, setores com dados, funcionarios envolvidos, pecas produzidas, producao por setor, evolucao temporal e duracao registrada; verificar os valores da fixture.
- [x] 5.2 Consolidar a visao de setor com nome, total, funcionarios, pecas, evolucao, agrupamentos e duracao, aplicando os filtros principais; verificar que dados de outro setor nao aparecem.
- [x] 5.3 Implementar ou ajustar drill-down de funcionario e peca com codigo, setor, periodo, quantidade, evolucao, duracao e relacoes autorizadas, sem avaliacoes subjetivas; verificar `00147=200` e `PEC-001=270`.
- [x] 5.4 Isolar maquinas, turnos, paradas, qualidade, metas, financeiro e IA/RAG do caminho core; verificar que a importacao padrao funciona sem essas colunas ou recursos e que endpoints existentes continuam disponiveis.

## 6. Relatorio e documentacao operacional

- [x] 6.1 Implementar ou ajustar o relatorio simplificado para periodo, total, setor, funcionario, peca, evolucao, duracao e resumo executivo; verificar reconciliacao com os dashboards e ausencia de metricas inventadas.
- [x] 6.2 Criar `docs/ESCOPO_SIMPLIFICADO.md` e `docs/FLUXO_EXCEL_DASHBOARD.md` com escopo, telas futuras, endpoints reais, filtros, erros e dependencias; verificar que os comandos e rotas documentados existem.
- [x] 6.3 Documentar o contrato de API e o comportamento de reimportacao/erros, preservando frontend fora do escopo; verificar revisao cruzada com resources e DTOs atuais.

## 7. Teste E2E e aceite

- [x] 7.1 Criar cenario E2E autenticado que obtenha/crie Pintura a po, importe o template, confirme registros e consulte visao geral, setor, funcionario, peca e relatorio; verificar a cadeia completa sem erro.
- [x] 7.2 Executar a matriz obrigatoria de casos validos, vazios, extensao/cabecalho/data/codigo/quantidade/horario invalidos, setor ausente/nao autorizado, zeros a esquerda, duplicidade e reimportacao; verificar codigos e mensagens seguras.
- [x] 7.3 Verificar formalmente os totais da fixture de tres registros: total 350, funcionarios 200/150, pecas 270/80, duracao 185 minutos e nenhum stop inferido pelos 10 minutos de intervalo.
- [x] 7.4 Executar backend tests, frontend tests, builds, validacao OpenSpec e uma execucao final do sistema; somente marcar `CORE EXCEL -> DASHBOARD: GO` quando importacao, dashboards e relatorio estiverem reconciliados.
