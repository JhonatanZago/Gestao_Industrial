# TASK DE REFATORAÇÃO — Simplificação do Sistema para Excel → Setores → Dashboard

## Objetivo oficial

Refatorar o sistema existente para que o núcleo do produto seja:

```text
Excel padronizado + Setor selecionado
→ Importação
→ Validação
→ Produção
→ Agregação por setor
→ Dashboard Geral
→ Dashboard do Setor
→ Drill-down Funcionário/Peça
→ Relatório
```

O Backend V1 existente deve ser reaproveitado. Não apagar módulos em massa e não iniciar o novo frontend nesta task.

## 1. OpenSpec

Criar a change:

```text
simplificar-sistema-dashboard-excel
```

Registrar proposal, design, tasks e specs/deltas conforme o padrão OpenSpec já usado no projeto.

## 2. Análise antes da alteração

Antes de modificar código:

1. Ler projeto, OpenSpecs e documentação.
2. Executar testes e build atuais.
3. Mapear entidades, DTOs, services, repositories, controllers/resources e integrações.
4. Mapear dependências entre módulos.
5. Criar `docs/ANALISE_IMPACTO_SIMPLIFICACAO.md`.

Classificar cada módulo como:

```text
CORE
MANTER
SIMPLIFICAR
OCULTAR_DO_FRONTEND
DESACOPLAR
CANDIDATO_A_REMOCAO_FUTURA
```

Não remover código funcional apenas porque deixou de ser prioridade.

## 3. Padrão oficial Excel

Todas as planilhas dos setores serão padronizadas com estas seis colunas:

| Coluna | Obrigatória | Exemplo |
|---|---|---|
| Data | Sim | 29/09/2026 |
| Código Funcionário | Sim | 00147 |
| Código Peça | Sim | PEC-001 |
| Quantidade | Sim | 120 |
| Hora Início | Sim | 07:10 |
| Hora Fim | Sim | 08:20 |

A planilha NÃO precisa possuir `Setor`.

O setor é selecionado no upload:

```text
Setor: Pintura a pó
Arquivo: pintura_29-09-2026.xlsx
```

Todos os registros válidos importados recebem esse setor.

## 4. Regras de validação

Validar:

```text
Data → válida
Código Funcionário → texto obrigatório e preservar zeros à esquerda
Código Peça → texto obrigatório
Quantidade → inteiro > 0
Hora Início → horário válido
Hora Fim → horário válido e posterior ao início
```

Calcular:

```text
duracaoMinutos = Hora Fim - Hora Início
```

Não chamar duração de eficiência.

Não interpretar automaticamente intervalos entre registros como parada.

## 5. Setor dinâmico

Setores atuais são exemplos:

```text
Pintura a pó
Solda
KTL
Estamparia
Zincagem
Dobradeira
Laser
```

A lista não pode ser fechada.

Novos setores devem poder ser cadastrados e usados sem alterar importador, Dashboard ou frontend.

Se `Setor` atualmente for enum fixo, analisar impacto e criar migração segura para modelo dinâmico antes de alterar.

## 6. Endpoint de importação

O contrato deve receber conceitualmente:

```text
setorId
arquivo XLSX
```

Reutilizar endpoint existente quando adequado.

Validar setor existente/ativo, empresa/contexto e autorização quando essas regras existirem.

Nunca determinar setor apenas pelo nome do arquivo.

## 7. Fluxo da importação

```text
Selecionar setor
→ Selecionar XLSX
→ Validar arquivo
→ Validar cabeçalhos
→ Ler linhas
→ Validar dados
→ Normalizar
→ Persistir produção
→ Registrar resultado
→ Disponibilizar no Dashboard
```

O retorno deve informar:

```text
arquivo
setor
linhas lidas
linhas importadas
linhas com erro
status
```

Para erro por linha, quando possível:

```text
linha
campo
valor
motivo
```

Nunca retornar stack trace.

## 8. Duplicidade e reimportação

Definir explicitamente como evitar importação duplicada.

Avaliar mecanismos já existentes, como:

```text
hash do arquivo
identificador da importação
setor
empresa
origem
versionamento/reprocessamento
```

Não confiar somente no nome do arquivo.

## 9. Produção

Revisar o modelo atual para representar, no mínimo:

```text
id
data
funcionário/código
peça/código
quantidade
horaInicio
horaFim
duracao
setor
importação/origem
```

Reaproveitar entidades existentes e não duplicar domínio sem necessidade.

## 10. Funcionários e peças

Funcionário precisa suportar:

```text
produção total
peças produzidas
quantidade
período
setor
duração registrada
```

Peça precisa suportar:

```text
quantidade produzida
período
setor
evolução
funcionários relacionados quando autorizado
```

Não criar avaliação subjetiva de funcionário.

## 11. Recursos secundários

Máquina e Turno deixam de ser obrigatórios no novo Excel.

Se já existem, preservar sem bloquear a importação.

IA/RAG, metas, auditoria avançada, qualidade avançada e outros módulos não fazem parte do fluxo principal desta refatoração. Preservar/desacoplar quando necessário.

Falha em recurso secundário não deve derrubar:

```text
Excel → Produção → Dashboard
```

## 12. Analytics simplificado

Garantir consultas eficientes para:

```text
produção total
produção por setor
produção por dia/período
produção por funcionário
produção por peça
duração total registrada
comparação simples de períodos, se já suportada corretamente
```

Não inventar:

```text
OEE
eficiência industrial
lucro
faturamento
capacidade
tempo de parada
```

sem dados suficientes.

## 13. Dashboard Geral

Fornecer dados para:

```text
Produção total
Quantidade de setores com dados
Quantidade de funcionários envolvidos
Quantidade de códigos de peças produzidos
Produção por setor
Evolução da produção no período
```

Comparação de período e duração somente quando suportadas corretamente.

## 14. Dashboard do Setor

Ao selecionar setor, retornar:

```text
nome
produção total
funcionários envolvidos
peças produzidas
evolução temporal
produção por funcionário
produção por peça
duração registrada
```

Todos os números devem respeitar os mesmos filtros.

## 15. Drill-downs

Funcionário:

```text
código
setor
período
quantidade total
peças produzidas
registros/evolução
duração registrada
```

Peça:

```text
código
setor
período
quantidade
evolução
funcionários relacionados quando permitido
```

## 16. Filtros principais

Priorizar somente:

```text
data inicial
data final
setor
funcionário
peça
```

## 17. API mínima

Revisar endpoints existentes antes de criar novos. O backend precisa suportar conceitualmente:

```text
GET setores
POST importação de produção por setor
GET dashboard geral
GET dashboard por setor
GET produção por funcionário
GET produção por peça
GET relatório/resumo
```

Evitar endpoints duplicados.

## 18. Banco e performance

Agregações devem ocorrer no backend/banco, nunca carregando toda produção no navegador.

Revisar índices para:

```text
data
setor
funcionário
peça
```

Criar migrations incrementais quando necessário.

Não editar migrations históricas e não apagar tabelas nesta task.

## 19. Segurança

Preservar autenticação/autorização.

Garantir:

```text
usuário autenticado
empresa correta, se aplicável
setor autorizado, se aplicável
limite de upload
tipo/extensão permitidos
proteção contra arquivo malformado
path traversal
formula injection em exportações
```

## 20. Frontend

NÃO refazer o frontend completo nesta task.

Somente documentar o futuro mapa:

```text
Visão Geral
Setores
Importar Excel
Relatórios
```

Navegação contextual:

```text
Setor
├── Funcionários
└── Peças
```

O Dashboard será o produto principal.

## 21. Relatório

O resumo para o dono deve ser simplificado para:

```text
período
produção total
produção por setor
produção por funcionário
produção por peça
evolução
resumo executivo simples
```

Não inventar formato de exportação que o backend ainda não suporte.

## 22. Template oficial

Adicionar template de exemplo:

```text
docs/templates/PRODUCAO_PADRAO.xlsx
```

e:

```text
docs/PADRAO_PLANILHA_PRODUCAO.md
```

Documentar colunas, tipos, obrigatoriedade, exemplos e erros comuns.

## 23. Massa determinística

Para setor `Pintura a pó`:

```text
29/09/2026 | 00147 | PEC-001 | 120 | 07:00 | 08:00
29/09/2026 | 00147 | PEC-002 | 80  | 08:10 | 09:00
29/09/2026 | 00152 | PEC-001 | 150 | 07:00 | 08:15
```

Esperado:

```text
produção total = 350
00147 = 200
00152 = 150
PEC-001 = 270
PEC-002 = 80
duração registrada total = 185 min
```

Os 10 minutos entre 08:00 e 08:10 NÃO são automaticamente parada.

## 24. Testes obrigatórios

Cobrir:

```text
arquivo válido
arquivo vazio
extensão inválida
cabeçalho ausente/incorreto
data inválida
funcionário vazio
peça vazia
quantidade zero/negativa
hora inválida
hora fim <= hora início
setor inexistente
setor não autorizado
zeros à esquerda
duplicidade/reimportação
```

## 25. E2E principal

```text
Obter/cadastrar Pintura
→ importar PRODUCAO_PADRAO.xlsx
→ confirmar registros
→ consultar Dashboard Geral
→ confirmar total da Pintura
→ abrir Dashboard da Pintura
→ confirmar 00147/00152
→ confirmar PEC-001/PEC-002
→ consultar/gerar resumo
```

Os totais devem coincidir em todas as etapas.

## 26. Documentação

Criar:

```text
docs/ANALISE_IMPACTO_SIMPLIFICACAO.md
docs/ESCOPO_SIMPLIFICADO.md
docs/PADRAO_PLANILHA_PRODUCAO.md
docs/FLUXO_EXCEL_DASHBOARD.md
```

Objetivo registrado:

> Transformar dados padronizados das planilhas Excel de cada setor em informações consolidadas de produção e Dashboards.

## 27. Ordem obrigatória

```text
1. Ler projeto/OpenSpec/docs
2. Rodar testes/build inicial
3. Criar change OpenSpec
4. Criar análise de impacto
5. Definir/validar contrato Excel
6. Revisar Setor
7. Revisar Importação
8. Revisar Produção
9. Revisar Funcionário
10. Revisar Peça
11. Simplificar Analytics
12. Revisar Dashboard Geral
13. Revisar Dashboard Setor
14. Revisar drill-downs
15. Revisar relatório
16. Criar template/documentação
17. Criar testes
18. Executar E2E Excel → Dashboard
19. Rodar regressão
20. Build final
```

## 28. Critérios de aceite

A task só termina quando:

1. OpenSpec existir e validar.
2. Análise de impacto existir.
3. Nenhum módulo for removido sem justificativa.
4. Padrão Excel estiver documentado.
5. Setor for selecionado no upload.
6. Setor não depender do nome do arquivo.
7. Setores forem dinâmicos ou existir migração segura aprovada.
8. Máquina/Turno não forem obrigatórios.
9. Seis colunas oficiais forem validadas.
10. Zeros à esquerda forem preservados.
11. Horários/duração forem corretos.
12. Intervalos não forem chamados de parada.
13. Reimportação estiver tratada.
14. Produção estiver ligada ao setor.
15. Dashboard Geral usar dados importados.
16. Dashboard por setor funcionar.
17. Produção por funcionário funcionar.
18. Produção por peça funcionar.
19. Filtros principais funcionarem.
20. Agregações ocorrerem no backend.
21. Relatório usar a mesma fonte de dados.
22. Template oficial existir.
23. E2E Excel → Dashboard passar.
24. Testes relevantes anteriores continuarem passando.
25. Build passar.
26. Recursos secundários não bloquearem o core.
27. Frontend completo não tiver sido iniciado nesta task.

## 29. Entrega do Codex

Apresentar:

### Análise
Arquitetura e módulos encontrados.

### Classificação
Tabela com:

```text
CORE
MANTER
SIMPLIFICAR
OCULTAR_DO_FRONTEND
DESACOPLAR
CANDIDATO_A_REMOCAO_FUTURA
```

### OpenSpec
Change, specs, tasks e validação.

### Excel
Confirmar:

```text
Data
Código Funcionário
Código Peça
Quantidade
Hora Início
Hora Fim
```

### Importação
Fluxo, validações, erros e duplicidade.

### Dashboard
Endpoints reais e indicadores suportados.

### Banco
Migrations e índices alterados.

### Testes
Resultados reais, incluindo E2E.

### Build
Resultado real:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

### Resultado

```text
CORE EXCEL → DASHBOARD: GO
```

ou:

```text
CORE EXCEL → DASHBOARD: NO-GO
```

Não declarar GO se a importação não alimentar corretamente o Dashboard.

## 30. Próxima fase

Somente após `CORE EXCEL → DASHBOARD: GO`, iniciar o novo frontend Premium Dark focado em:

```text
Visão Geral
Setores
Importar Excel
Relatórios
```

com drill-down contextual de Funcionários e Peças.
