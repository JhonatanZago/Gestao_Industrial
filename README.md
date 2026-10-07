# 🏭 Sistema de Gestão e Inteligência de Produção Industrial

Sistema desenvolvido para **automatizar a coleta e análise dos dados de produção de uma indústria**, utilizando como fonte principal as planilhas Excel armazenadas no servidor da empresa.

A principal ideia do projeto é permitir que os funcionários continuem preenchendo normalmente as planilhas utilizadas nos setores produtivos. O sistema monitora esses arquivos, identifica alterações, importa automaticamente os novos dados e transforma essas informações em **dashboards, gráficos, indicadores e relatórios gerenciais**.

---

# 💡 Ideia principal do projeto

Atualmente, diversos dados de produção são registrados em planilhas Excel separadas por setor.

Essas planilhas possuem informações importantes sobre a operação, como:

- funcionário responsável;
- código da peça;
- quantidade produzida;
- horário de produção;
- máquina;
- turno;
- setor.

O problema é que analisar manualmente várias planilhas torna difícil acompanhar a produção da empresa de forma rápida e centralizada.

Por isso, o projeto propõe automatizar todo esse processo.

## Fluxo principal

```text
             SERVIDOR DA EMPRESA
                     │
                     ▼
             Planilhas Excel
                     │
                     │ alteração/salvamento
                     ▼
          Monitoramento automático
                     │
                     ▼
           Importação dos dados
                     │
                     ▼
              Validações
                     │
                     ▼
              Banco de Dados
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
   Dashboards     Gráficos    Relatórios
        │            │            │
        └────────────┼────────────┘
                     ▼
              Sistema Web
                     │
                     ▼
                  Gestor
```

A proposta é que o processo seja o mais automático possível.

O funcionário não precisa acessar o sistema toda vez que produzir alguma coisa. Ele continua utilizando a planilha normalmente.

Quando a planilha é alterada e salva no servidor:

```text
Funcionário altera a planilha
            ↓
Funcionário salva o arquivo
            ↓
Sistema detecta a alteração
            ↓
Arquivo é validado
            ↓
Novos registros são identificados
            ↓
Dados são importados
            ↓
Banco de dados é atualizado
            ↓
Dashboard é atualizado
            ↓
Gráficos são atualizados
            ↓
Relatórios passam a utilizar os novos dados
```

---

# 🎯 Objetivo

O principal objetivo do sistema é **transformar os arquivos Excel utilizados diariamente pela empresa em uma fonte automatizada de informações gerenciais**.

Em vez de um gestor precisar abrir diversas planilhas para descobrir como está a produção, o sistema centraliza essas informações e apresenta os dados de maneira visual.

O sistema deverá permitir responder perguntas como:

> Quantas peças foram produzidas hoje?

> Quanto o setor de Pintura produziu?

> Qual foi a produção do setor de KTL nesta semana?

> Quais peças tiveram maior produção?

> Qual funcionário produziu mais?

> Como está a produção deste mês em comparação com o mês anterior?

> Houve queda de produtividade em algum setor?

As respostas são obtidas a partir dos dados importados das planilhas.

---

# 📊 Transformação dos dados

O sistema não tem como objetivo apenas armazenar as informações do Excel.

O objetivo é transformar:

```text
DADOS BRUTOS
     ↓
INFORMAÇÃO
     ↓
INDICADORES
     ↓
GRÁFICOS
     ↓
RELATÓRIOS
     ↓
TOMADA DE DECISÃO
```

Por exemplo, uma planilha pode possuir registros como:

```text
Funcionário | Peça | Quantidade | Início | Fim
00147       | P01  | 120        | 08:00  | 10:00
00152       | P02  | 80         | 08:30  | 09:45
00147       | P03  | 150        | 10:15  | 12:00
```

Isoladamente, são apenas registros de produção.

Depois de processados pelo sistema, eles podem gerar informações como:

```text
Produção total do setor
→ 350 peças

Produção funcionário 00147
→ 270 peças

Peça mais produzida
→ P03

Produção por horário
→ gráfico

Produção por funcionário
→ gráfico

Produção por peça
→ gráfico

Resumo diário
→ relatório
```

---

# 📈 Dashboards e gráficos

Após a importação, os dados podem ser utilizados para gerar dashboards de acompanhamento da produção.

Entre os indicadores previstos estão:

- produção total;
- produção diária;
- produção semanal;
- produção mensal;
- produção por setor;
- produção por funcionário;
- produção por peça;
- produtividade;
- tempo de produção;
- comparações entre períodos;
- ranking de produção;
- evolução da produção.

Os gráficos facilitam a identificação de tendências e problemas que seriam difíceis de perceber analisando diretamente as planilhas.

---

# 📑 Relatórios gerenciais

Além dos dashboards, o sistema utiliza os dados importados para gerar relatórios.

Os relatórios poderão ser organizados por:

- setor;
- funcionário;
- peça;
- período;
- produção;
- produtividade.

Exemplo:

```text
RELATÓRIO DE PRODUÇÃO

Período:
01/10/2026 até 31/10/2026

Setor:
Pintura

Produção total:
42.350 peças

Funcionários:
18

Peça mais produzida:
ABC-001

Média diária:
1.366 peças

+ gráficos
+ indicadores
+ detalhamento da produção
```

O objetivo é permitir que responsáveis e gestores tenham uma visão consolidada da operação sem precisar montar relatórios manualmente no Excel.

---

# ⚙️ Importação automática

A automação é uma das partes mais importantes do projeto.

A ideia é que as planilhas estejam armazenadas em uma pasta compartilhada no servidor da empresa.

Por exemplo:

```text
SERVIDOR
│
└── Produção
    │
    ├── KTL
    │   └── producao.xlsx
    │
    ├── Pintura
    │   └── producao.xlsx
    │
    ├── Solda
    │   └── producao.xlsx
    │
    ├── Laser
    │   └── producao.xlsx
    │
    └── outros setores...
```

O sistema monitora essas fontes.

Quando uma planilha é salva:

```text
Excel
  ↓
Arquivo alterado
  ↓
Detecção
  ↓
Espera o arquivo terminar de salvar
  ↓
Validação
  ↓
Identificação da versão
  ↓
Identificação dos novos registros
  ↓
Importação
  ↓
Banco
  ↓
Dashboard / Gráficos / Relatórios
```

---

# 🔄 Importação incremental

O sistema deve evitar duplicações quando uma planilha é atualizada.

Exemplo:

### Primeira versão

```text
A
B
```

O sistema importa:

```text
A
B
```

### Segunda versão

Posteriormente o funcionário adiciona:

```text
A
B
C
```

O sistema deve reconhecer:

```text
A → já processado
B → já processado
C → novo
```

E importar apenas:

```text
C
```

O resultado no banco permanece:

```text
A
B
C
```

Esse comportamento é essencial porque as mesmas planilhas podem ser salvas várias vezes durante o dia.

---

# 🕕 Reconciliação

Além do monitoramento automático dos arquivos, o sistema possui uma estratégia de reconciliação.

Ela funciona como uma segunda camada de segurança.

Caso uma alteração não seja detectada no momento em que aconteceu:

```text
Alteração não detectada
        ↓
Reconciliação
        ↓
Verificação dos arquivos
        ↓
Nova versão encontrada
        ↓
Importação
```

Dessa forma, o sistema reduz o risco de uma alteração ficar permanentemente sem processamento.

---

# 🏭 Setores

O sistema foi projetado para trabalhar com diferentes setores da empresa, como:

- Pintura a Pó;
- KTL;
- Solda;
- Estamparia;
- Zincagem;
- Dobradeira;
- Laser.

A arquitetura permite adicionar novos setores futuramente sem precisar reconstruir toda a lógica de importação.

---

# 🧩 Visão geral da solução

```text
┌──────────────────────────────────────────┐
│             AMBIENTE INDUSTRIAL          │
│                                          │
│ Funcionários trabalham normalmente      │
│ utilizando Excel                        │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│            SERVIDOR DA EMPRESA           │
│                                          │
│ Planilhas dos setores                    │
│ KTL | Pintura | Solda | Laser | ...      │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│          IMPORTAÇÃO AUTOMÁTICA           │
│                                          │
│ Detecção                                 │
│ Estabilidade                             │
│ Validação                                │
│ SHA-256                                  │
│ Idempotência                             │
│ Histórico                                │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│               BANCO DE DADOS             │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│             BACKEND / ANALYTICS          │
│                                          │
│ Indicadores                              │
│ Agregações                               │
│ Comparações                              │
│ Relatórios                               │
└───────────────────┬──────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────┐
│                  FRONTEND                │
│                                          │
│ Dashboard                                │
│ Gráficos                                 │
│ Setores                                  │
│ Funcionários                             │
│ Peças                                    │
│ Relatórios                               │
│ Monitoramento das importações            │
└──────────────────────────────────────────┘
```

---

# 🛠️ Tecnologias

### Backend

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Web
- Bean Validation
- Gradle
- Apache POI

### Frontend

- Angular
- TypeScript
- HTML
- CSS

### Integração e dados

- Excel / XLSX
- Apache POI
- SHA-256
- monitoramento de arquivos
- API REST

### Desenvolvimento

- IntelliJ IDEA
- Git
- GitHub
- OpenSpec
- Codex

---

# 🔐 Segurança

O sistema foi projetado considerando:

- autenticação;
- autorização;
- diferentes perfis de acesso;
- isolamento das empresas;
- validação dos arquivos;
- rastreabilidade das importações;
- proteção dos endpoints;
- não exposição de credenciais;
- não exposição de caminhos internos do servidor.

---

# 🚀 Evolução futura

Com os dados industriais centralizados, novas funcionalidades poderão ser desenvolvidas.

Entre elas:

- relatórios gerenciais avançados;
- comparações automáticas;
- alertas de produtividade;
- análise de eficiência;
- detecção de anomalias;
- previsão de produção;
- Inteligência Artificial;
- consultas utilizando linguagem natural;
- RAG.

Um objetivo futuro é permitir perguntas como:

> "Quantas peças foram produzidas hoje no setor de Pintura?"

E o sistema utilizar os dados centralizados para gerar uma resposta acompanhada de indicadores e gráficos.

---

# 🗺️ Visão do projeto

A evolução geral pode ser resumida como:

```text
PLANILHAS
    ↓
IMPORTAÇÃO AUTOMÁTICA
    ↓
BANCO CENTRALIZADO
    ↓
DADOS TRATADOS
    ↓
INDICADORES
    ↓
DASHBOARDS
    ↓
GRÁFICOS
    ↓
RELATÓRIOS
    ↓
ANÁLISES
    ↓
INTELIGÊNCIA ARTIFICIAL
```

---

# 📌 Status do projeto

🚧 **Em desenvolvimento**

O projeto encontra-se em fase de desenvolvimento e homologação.

A automação está sendo preparada e validada inicialmente em ambiente local. A integração definitiva com a pasta compartilhada do servidor da empresa deverá ser homologada quando houver acesso ao ambiente real.

---

# 🧩 Papel de cada tecnologia

| Tecnologia | Responsabilidade no projeto |
|---|---|
| ☕ **Java 17** | Regras de negócio, validações e processamento dos dados |
| 🍃 **Spring Boot** | Estrutura e execução do backend |
| 🅰️ **Angular** | Interface web, dashboards, tabelas e relatórios |
| 🔷 **TypeScript** | Lógica e tipagem do frontend |
| 🔗 **REST API** | Comunicação entre frontend e backend |
| 🗄️ **Spring Data JPA** | Persistência e consultas ao banco de dados |
| 📄 **Apache POI** | Leitura e processamento das planilhas Excel |
| 🐘 **Gradle** | Dependências, testes e build do backend |
| 🌿 **Git/GitHub** | Versionamento e armazenamento do código |
| 📋 **OpenSpec** | Organização de requisitos, especificações e tasks |


# 👨‍💻 Autor

**Jhonatan Zago**

Estudante de Sistemas de Informação.

GitHub: `JhonatanZago`