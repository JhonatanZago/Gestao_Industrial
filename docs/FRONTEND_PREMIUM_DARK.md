# Frontend Premium Dark

O shell operacional segue o fluxo `Visão Geral → Setores → Dashboard do Setor → Funcionário/Peça → Relatórios`.

Rotas disponíveis:

- `/app/dashboard`
- `/app/setores`
- `/app/setores/:setorId`
- `/app/setores/:setorId/funcionarios/:codigo`
- `/app/setores/:setorId/pecas/:codigo`
- `/app/importacoes`
- `/app/relatorios`

Os códigos são mantidos como strings. Em especial, `00147` não é convertido para `147`. A sidebar mostra somente as quatro áreas principais; funcionários e peças são acessados pelo contexto de setor.
