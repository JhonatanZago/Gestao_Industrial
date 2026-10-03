# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: import-flow.spec.ts >> SETOR → XLSX → VALIDAR → IMPORTAR → CONCLUIR → VER DADOS
- Location: e2e\import-flow.spec.ts:31:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.import-error-table tbody tr').first()
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" locator('.import-error-table tbody tr').first() with timeout 5000ms
  - waiting for locator('.import-error-table tbody tr').first()

```

```yaml
- complementary:
  - strong: METALÚRGICA
  - text: SISTEMA DE PRODUÇÃO
  - navigation "Navegação principal":
    - button "Visao geral"
    - button "Setores"
    - button "Importar Excel"
    - button "Relatorios"
  - text: U Usuário Sessão autorizada
  - button "Sair"
  - text: Backend operacional
- main:
  - heading "Importar Excel" [level=1]
  - paragraph: Importe os dados de produção e atualize os dashboards.
  - region "A planilha foi rejeitada":
    - list "Etapas da importação":
      - listitem: Setor Destino dos dados
      - listitem: Arquivo Planilha XLSX
      - listitem: 03 Validação Regras do sistema
      - listitem: 04 Concluído Dados processados
    - article:
      - text: ARQUIVO COM PROBLEMAS
      - heading "A planilha foi rejeitada" [level=2]
      - paragraph: Arquivo rejeitado por validação.
      - text: Erros críticos
      - strong: "9"
      - text: Linhas válidas
      - strong: "0"
      - text: Total analisado
      - strong: "9"
      - button "← Trocar arquivo"
    - complementary:
      - strong: Precisa do modelo correto?
      - paragraph: Baixe a planilha padrão utilizada pelo sistema.
      - link "Baixar modelo XLSX":
        - /url: /templates/PRODUCAO_PADRAO.xlsx
  - text: Sistema operacional v1.0 · API /api/v1
```

# Test source

```ts
  1  | import { expect, test, type Page } from '@playwright/test';
  2  | import path from 'node:path';
  3  | 
  4  | const email = process.env['E2E_EMAIL'];
  5  | const password = process.env['E2E_PASSWORD'];
  6  | const validFile = path.resolve('../tmp-homologacao-fixtures/importacao-e2e-2026-10-02.xlsx');
  7  | const invalidFile = path.resolve('../tmp-homologacao-fixtures/invalid-employee.xlsx');
  8  | 
  9  | async function login(page: Page) {
  10 |   if (!email || !password) throw new Error('E2E_EMAIL e E2E_PASSWORD são obrigatórios para o fluxo real.');
  11 |   await page.goto('/');
  12 |   await page.getByLabel('E-mail').fill(email);
  13 |   await page.getByLabel('Senha').fill(password);
  14 |   await page.getByRole('button', { name: 'ENTRAR NO SISTEMA' }).click();
  15 |   await expect(page.locator('.app-frame')).toBeVisible();
  16 | }
  17 | 
  18 | async function openImport(page: Page) {
  19 |   const sectorsResponse = page.waitForResponse(response => response.url().includes('/api/v1/empresas/1/setores') && response.request().method() === 'GET');
  20 |   await page.getByRole('navigation', { name: /Navegação principal/i }).getByRole('button', { name: 'Importar Excel' }).click();
  21 |   expect((await sectorsResponse).status()).toBe(200);
  22 |   await expect(page.getByRole('heading', { name: 'Selecione o setor' })).toBeVisible();
  23 | }
  24 | 
  25 | async function chooseKtl(page: Page) {
  26 |   await page.locator('button.import-sector-card').filter({ hasText: 'KTL Homologacao' }).click();
  27 |   await page.getByRole('button', { name: /Continuar/i }).click();
  28 |   await expect(page.getByRole('heading', { name: 'Envie a planilha' })).toBeVisible();
  29 | }
  30 | 
  31 | test('SETOR → XLSX → VALIDAR → IMPORTAR → CONCLUIR → VER DADOS', async ({ page }) => {
  32 |   test.setTimeout(120_000);
  33 |   await page.setViewportSize({ width: 1920, height: 1080 });
  34 |   await login(page);
  35 |   await openImport(page);
  36 |   await page.screenshot({ path: path.resolve('importacao-step1-1920.png'), fullPage: true });
  37 | 
  38 |   await chooseKtl(page);
  39 |   await page.locator('#import-file').setInputFiles(validFile);
  40 |   await expect(page.getByText('importacao-e2e-2026-10-02.xlsx')).toBeVisible();
  41 |   await page.screenshot({ path: path.resolve('importacao-step2-1920.png'), fullPage: true });
  42 | 
  43 |   const validationResponse = page.waitForResponse(response => response.url().endsWith('/importacoes/producao/validar') && response.request().method() === 'POST');
  44 |   await page.getByRole('button', { name: /Validar arquivo/i }).click();
  45 |   expect((await validationResponse).status()).toBe(200);
  46 |   await expect(page.getByRole('heading', { name: 'Arquivo válido' })).toBeVisible({ timeout: 30_000 });
  47 |   await expect(page.getByText('3', { exact: true }).first()).toBeVisible();
  48 |   await page.screenshot({ path: path.resolve('importacao-validation-1920.png'), fullPage: true });
  49 | 
  50 |   await page.getByRole('button', { name: /Confirmar importação/i }).click();
  51 |   await expect(page.getByRole('dialog', { name: 'Confirmar importação' })).toBeVisible();
  52 |   const importResponse = page.waitForResponse(response => /\/setores\/3\/importacoes\/producao$/.test(response.url()) && response.request().method() === 'POST');
  53 |   await page.getByRole('dialog').getByRole('button', { name: 'Importar dados' }).click();
  54 |   expect((await importResponse).status()).toBe(201);
  55 |   await expect(page.getByRole('heading', { name: 'Importação concluída' })).toBeVisible({ timeout: 30_000 });
  56 |   await expect(page.getByText('3', { exact: true }).first()).toBeVisible();
  57 |   await page.screenshot({ path: path.resolve('importacao-success-1920.png'), fullPage: true });
  58 | 
  59 |   const sectorResponse = page.waitForResponse(response => /\/analytics\/dashboard\/setor\/3/.test(response.url()));
  60 |   await page.getByRole('button', { name: /Ver setor/i }).click();
  61 |   expect((await sectorResponse).status()).toBe(200);
  62 |   const octoberResponse = page.waitForResponse(response => response.url().includes('/analytics/dashboard/setor/3') && response.url().includes('2026-10-01'));
  63 |   await page.locator('#period').selectOption({ label: 'Outubro / 2026' });
  64 |   expect((await octoberResponse).status()).toBe(200);
  65 |   await expect(page.locator('.sector-detail-kpi').first()).toContainText('1.200');
  66 | 
  67 |   await page.getByRole('navigation', { name: /Navegação principal/i }).getByRole('button', { name: 'Importar Excel' }).click();
  68 |   await page.getByRole('button', { name: 'Importar outra planilha' }).click();
  69 |   await chooseKtl(page);
  70 |   await page.locator('#import-file').setInputFiles(invalidFile);
  71 |   const rejectedResponse = page.waitForResponse(response => response.url().endsWith('/importacoes/producao/validar') && response.request().method() === 'POST');
  72 |   await page.getByRole('button', { name: /Validar arquivo/i }).click();
  73 |   expect((await rejectedResponse).status()).toBe(200);
  74 |   await expect(page.getByRole('heading', { name: 'A planilha foi rejeitada' })).toBeVisible({ timeout: 30_000 });
> 75 |   await expect(page.locator('.import-error-table tbody tr').first()).toBeVisible();
     |                                                                      ^ Error: expect(locator).toBeVisible() failed
  76 |   await page.screenshot({ path: path.resolve('importacao-error-1920.png'), fullPage: true });
  77 | 
  78 |   await page.getByRole('button', { name: /Trocar arquivo/i }).click();
  79 |   await page.getByRole('button', { name: /Voltar/i }).click();
  80 |   await page.setViewportSize({ width: 390, height: 844 });
  81 |   await expect(page.getByRole('heading', { name: 'Selecione o setor' })).toBeVisible();
  82 |   expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
  83 |   await page.screenshot({ path: path.resolve('importacao-mobile.png'), fullPage: true });
  84 | });
  85 | 
```