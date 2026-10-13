# Identidade de produção — V1

## Decisão

`DOCUMENTED_LIMITATION`.

O template XLSX V1 contém somente os seis campos de negócio definidos em
`docs/PADRAO_PLANILHA_PRODUCAO.md`: data, funcionário, peça, quantidade,
hora de início e hora de fim. Não existe no arquivo um identificador estável
de lançamento, sequência de origem ou timestamp de origem suficientemente
específico para distinguir duas produções perfeitamente idênticas.

V1, portanto, não afirma que valores de negócio iguais provam que duas
produções são a mesma produção. A implementação preserva a idempotência já
homologada por hash de arquivo e a reconciliação incremental existente, sem
apagar registros persistidos silenciosamente. Registros ignorados no retorno
da importação permanecem rastreáveis no histórico/resultado da importação.

Essa reconciliação por fingerprint técnico derivado dos campos disponíveis é
uma limitação de identidade da V1, não um identificador de negócio. Uma
produção legítima que replique exatamente os campos de outra produção pode
exigir intervenção/decisão operacional para não ser tratada como linha já
processada.

## Compatibilidade

O template V1 não foi alterado. Não foi adicionada coluna ID artificial.

## Melhoria V2

Adicionar, mediante decisão do negócio e atualização coordenada do template,
um identificador explícito de origem (ID do lançamento ou sequência estável)
e usá-lo como identidade de reconciliação. A mudança deve manter os arquivos
V1 legados e definir o comportamento para fontes sem identificador.
