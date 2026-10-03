# ACCEPTANCE CHECKLIST
Build/testes verdes NÃO aprovam visual.

- [ ] Referência final correta foi analisada
- [ ] Sidebar 220px ±6 e branding sem sobreposição
- [ ] Header compacto
- [ ] 4 KPIs dark em uma linha (1920px)
- [ ] Cores dos KPIs somente como accents
- [ ] Grid principal ~65/35
- [ ] Grid secundário 3 colunas
- [ ] Grid inferior ~68/32
- [ ] Inter/system UI; sem fonte tech global
- [ ] Cards com profundidade discreta; sem neon
- [ ] Gráfico com gradient fill/tooltip
- [ ] Setores compactos
- [ ] Rankings refinados
- [ ] Donut consistente
- [ ] Empty state sem tabela vazia gigante
- [ ] Dados reais; nenhuma métrica hardcoded da imagem
- [ ] Backend/autenticação/rotas preservados
- [ ] Screenshot 1920x1080
- [ ] Screenshot 1366x768
- [ ] Pelo menos 3 ciclos implementar→screenshot→comparar→corrigir
- [ ] npm test PASS
- [ ] npm run build PASS

Se item visual essencial falhar: `DESIGN CONTRACT: NO-GO`.
Somente com alta fidelidade: `DESIGN CONTRACT: GO`.
