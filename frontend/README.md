# Frontend — Metalúrgica ERP

Frontend Angular standalone do fluxo `Excel por setor → Importação → Dashboard → Relatório`. O Backend V1 é a fonte de verdade: o cliente não cria regras, permissões, KPIs ou endpoints.

## Executar

```powershell
cd frontend
npm install
npm start
```

O frontend usa `/api` como base relativa e o login real é `POST /api/v1/auth/login`, seguido de `GET /api/v1/auth/me`.

## Importação XLSX

1. Abra Importar Excel e selecione um setor autorizado.
2. Arraste ou selecione um arquivo `.xlsx` com as seis colunas do template oficial.
3. Confirme uma vez; o fluxo mostra `SELECTED`, `VALIDATING`, `SENDING`, `SUCCESS`, `PARTIAL`, `DUPLICATE` ou `ERROR`.
4. Erros por linha e mensagens do backend são exibidos sem stack trace, token ou credenciais.

O cliente bloqueia clique duplo e reimportação do mesmo arquivo para o mesmo setor. Em caso de falha, a operação pode ser repetida manualmente depois de corrigida.

## Validar

```powershell
npm test -- --watch=false
npm run build
```

Os testes unitários usam respostas contratadas; a homologação final deve usar o backend e a massa oficial. O token fica no `sessionStorage` e não é escrito em logs.

## Estrutura

`src/app/core` concentra autenticação, contratos e serviços; `shared` concentra primitives visuais; `pages` contém rotas core lazy; o shell Premium Dark mantém as quatro áreas principais: Visão Geral, Setores, Importar Excel e Relatórios.
