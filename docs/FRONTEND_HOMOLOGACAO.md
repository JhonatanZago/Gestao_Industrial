# Homologação do frontend

## Critérios

- Login real com `auth/login` e `auth/me`; token somente em `sessionStorage`.
- Sidebar com exatamente Visão Geral, Setores, Importar Excel e Relatórios.
- Dashboard geral e de setor usando dados reais, sem KPIs inventados.
- Importação XLSX com setor dinâmico, bloqueio de clique duplo/reimportação e resposta real por estado.
- Relatório simplificado reconciliado com o dashboard.
- 401, 403, erro recuperável e `correlationId` sem exposição de segredo.
- Código `00147` preservado como string.
- Massa oficial: total 350, funcionários 200/150, peças 270/80 e duração 185 minutos.

## Comandos

```powershell
cd frontend
npm test -- --watch=false
npm run build
cd ..
openspec validate "frontend-premium-dark-excel-dashboard" --type change --strict
.\gradlew.bat test
```

O resultado `GO` só deve ser emitido após a suíte frontend, build, backend, acessibilidade e E2E real concluírem sem erro.

## Status desta execução

**NO-GO temporário:** o E2E foi descoberto e o Chromium foi instalado, mas não foi possível executar o fluxo real porque `E2E_EMAIL` e `E2E_PASSWORD` não estão configurados no ambiente. Nenhuma credencial foi inventada e nenhum mock foi usado.
