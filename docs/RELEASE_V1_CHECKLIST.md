# Release V1 — checklist de prontidão

Data da validação local: 2026-10-07

## LOCAL — concluído

- Backend: suite completa aprovada, `clean build` aprovado.
- Frontend: suite aprovada, production build aprovado.
- Playwright autenticado: 10 passed, 0 failed, 0 skipped.
- Usuário E2E: exclusivo de homologação local; credenciais somente em
  variáveis de ambiente do processo.
- Login, visão geral, setores, importação manual, centro de importações
  automáticas e relatórios validados com autenticação real.
- Fluxos desktop e mobile executados; sem overflow horizontal global.
- Logs finais: nenhum HTTP 401, 403 ou 500 inesperado; nenhum console error
  crítico observado.
- OpenSpec strict: válido.
- Registros perfeitamente idênticos: limitação de identidade documentada em
  `docs/IDENTIDADE_PRODUCAO_V1.md`; template V1 preservado.

## Bundle Angular

`DOCUMENTED` — o build permanece funcional, mas o bundle inicial excede o
warning budget de 500 kB em 132.74 kB (total 632.74 kB). A configuração foi
inspecionada e não foi aumentado o budget para ocultar o warning. A redução
segura requer otimização V2; não é bloqueador funcional da RC local.

## SERVIDOR REAL — pendente externo

- `REAL SERVER: PENDING_EXTERNAL_VALIDATION`
- `REAL SMB: PENDING_EXTERNAL_VALIDATION`
- Não foram usadas credenciais, arquivos ou caminhos da empresa.

## Classificação

- `LOCAL RELEASE CANDIDATE = YES`
- `READY_FOR_REAL_SERVER_HOMOLOGATION = YES`
- `PRODUCTION READY = NO`

A homologação no servidor/SMB real continua obrigatória antes de produção.
