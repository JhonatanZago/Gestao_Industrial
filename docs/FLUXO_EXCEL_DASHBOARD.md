# Fluxo Excel → Dashboard

```text
Setor autorizado
      ↓
XLSX com 6 colunas
      ↓
Leitura segura e cabeçalho
      ↓
Validação de linha e referências
      ↓
Importação atômica
      ↓
Registro de produção rastreável
      ↓
Dashboard geral / setor
      ↓
Funcionário / peça / relatório simplificado
```

## Contratos

- O setor vem da rota e é aplicado a todas as linhas.
- A duração é `Hora Fim - Hora Início` por registro.
- Intervalos entre registros não são paradas.
- Erros retornam status, contagens e linha/campo/motivo sem stack trace.
- A mesma carga não pode gerar registros duplicados por reimportação do mesmo
  arquivo; a limitação para duas produções perfeitamente idênticas está
  documentada em `docs/IDENTIDADE_PRODUCAO_V1.md`.

## Dados da fixture

Para `Pintura a pó` em 29/09/2026, os três registros do template devem produzir:

- Total: 350.
- Funcionário `00147`: 200.
- Funcionário `00152`: 150.
- Peça `PEC-001`: 270.
- Peça `PEC-002`: 80.
- Duração registrada: 185 minutos.
- O intervalo de 10 minutos não é stop.
