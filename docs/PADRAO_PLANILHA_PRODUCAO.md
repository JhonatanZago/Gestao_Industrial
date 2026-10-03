# Padrão da planilha de produção

O arquivo oficial é um `.xlsx` sem macros. O setor é escolhido na tela/API de importação e não deve ser colocado na planilha.

## Colunas obrigatórias

| Coluna | Tipo | Regra |
|---|---|---|
| `Data` | data | Data válida, preferencialmente `dd/MM/yyyy`. |
| `Código Funcionário` | texto | Obrigatório; preservar zeros à esquerda, por exemplo `00147`. |
| `Código Peça` | texto | Obrigatório; preservar o código original, por exemplo `PEC-001`. |
| `Quantidade` | inteiro | Obrigatório e maior que zero. Frações, zero e negativos são rejeitados. |
| `Hora Início` | hora | Hora válida no registro. |
| `Hora Fim` | hora | Hora válida e posterior à hora de início. |

O cabeçalho deve conter as seis colunas acima, aceitando apenas as variações de grafia já documentadas pelo importador. Colunas adicionais não participam da regra de negócio. `Setor`, nome do arquivo, máquina e turno não determinam o setor da importação.

## Regras de processamento

- O usuário seleciona um setor ativo e autorizado antes do upload.
- Cada linha válida recebe esse setor, a empresa da requisição e a origem `IMPORTACAO`.
- A duração registrada é `Hora Fim - Hora Início`; intervalos entre linhas não são paradas.
- Funcionários e peças precisam existir e estar ativos na empresa.
- A validação ocorre antes da persistência. Uma planilha com qualquer erro é rejeitada sem registros parciais.
- Reimportações idênticas são detectadas por conteúdo/contexto, não pelo nome do arquivo.
- Erros devem identificar linha, campo, valor seguro e motivo, sem stack trace ou caminho local.

## Exemplo mínimo

```text
Data       | Código Funcionário | Código Peça | Quantidade | Hora Início | Hora Fim
29/09/2026 | 00147              | PEC-001     | 120        | 07:00       | 08:00
29/09/2026 | 00147              | PEC-002     | 80         | 08:10       | 09:00
29/09/2026 | 00152              | PEC-001     | 150        | 07:00       | 08:15
```

Para a fixture completa de homologação, use `docs/templates/PRODUCAO_PADRAO.xlsx`.
