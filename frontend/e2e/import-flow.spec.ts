import { expect, test, type Page } from '@playwright/test';
import path from 'node:path';

const email = process.env['E2E_EMAIL'];
const password = process.env['E2E_PASSWORD'];
const validFile = path.resolve('../tmp-homologacao-fixtures/importacao-e2e-2026-10-02.xlsx');
const invalidFile = path.resolve('../tmp-homologacao-fixtures/invalid-employee.xlsx');

async function login(page: Page) {
  if (!email || !password) throw new Error('E2E_EMAIL e E2E_PASSWORD são obrigatórios para o fluxo real.');
  await page.goto('/');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(password);
  await page.getByRole('button', { name: 'ENTRAR NO SISTEMA' }).click();
  await expect(page.locator('.app-frame')).toBeVisible();
}

async function openImport(page: Page) {
  const sectorsResponse = page.waitForResponse(response => response.url().includes('/api/v1/empresas/1/setores') && response.request().method() === 'GET');
  await page.getByRole('navigation', { name: /Navegação principal/i }).getByRole('button', { name: 'Importar Excel' }).click();
  expect((await sectorsResponse).status()).toBe(200);
  await expect(page.getByRole('heading', { name: 'Selecione o setor' })).toBeVisible();
}

async function chooseKtl(page: Page) {
  await page.locator('button.import-sector-card').filter({ hasText: 'KTL Homologacao' }).click();
  await page.getByRole('button', { name: /Continuar/i }).click();
  await expect(page.getByRole('heading', { name: 'Envie a planilha' })).toBeVisible();
}

test('SETOR → XLSX → VALIDAR → IMPORTAR → CONCLUIR → VER DADOS', async ({ page }) => {
  test.setTimeout(120_000);
  await page.setViewportSize({ width: 1920, height: 1080 });
  await login(page);
  await openImport(page);
  await page.screenshot({ path: path.resolve('importacao-step1-1920.png'), fullPage: true });

  await chooseKtl(page);
  await page.locator('#import-file').setInputFiles(validFile);
  await expect(page.getByText('importacao-e2e-2026-10-02.xlsx')).toBeVisible();
  await page.screenshot({ path: path.resolve('importacao-step2-1920.png'), fullPage: true });

  const validationResponse = page.waitForResponse(response => response.url().endsWith('/importacoes/producao/validar') && response.request().method() === 'POST');
  await page.getByRole('button', { name: /Validar arquivo/i }).click();
  const validation = await validationResponse;
  if (validation.status() === 200) {
    await expect(page.getByRole('heading', { name: 'Arquivo válido' })).toBeVisible({ timeout: 30_000 });
    await expect(page.getByText('3', { exact: true }).first()).toBeVisible();
    await page.screenshot({ path: path.resolve('importacao-validation-1920.png'), fullPage: true });

    await page.getByRole('button', { name: /Confirmar importação/i }).click();
    await expect(page.getByRole('dialog', { name: 'Confirmar importação' })).toBeVisible();
    const importResponse = page.waitForResponse(response => /\/setores\/3\/importacoes\/producao$/.test(response.url()) && response.request().method() === 'POST');
    await page.getByRole('dialog').getByRole('button', { name: 'Importar dados' }).click();
    expect((await importResponse).status()).toBe(201);
    await expect(page.getByRole('heading', { name: 'Importação concluída' })).toBeVisible({ timeout: 30_000 });
    await expect(page.getByText('3', { exact: true }).first()).toBeVisible();
    await page.screenshot({ path: path.resolve('importacao-success-1920.png'), fullPage: true });

    const sectorResponse = page.waitForResponse(response => /\/analytics\/dashboard\/setor\/3/.test(response.url()));
    await page.getByRole('button', { name: /Ver setor/i }).click();
    expect((await sectorResponse).status()).toBe(200);
    const octoberResponse = page.waitForResponse(response => response.url().includes('/analytics/dashboard/setor/3') && response.url().includes('2026-10-01'));
    await page.locator('#period').selectOption({ label: 'Outubro / 2026' });
    expect((await octoberResponse).status()).toBe(200);
    await expect(page.locator('.sector-detail-kpi').first()).toContainText('1.200');

    await page.getByRole('navigation', { name: /Navegação principal/i }).getByRole('button', { name: 'Importar Excel' }).click();
    await page.getByRole('button', { name: 'Importar outra planilha' }).click();
    await chooseKtl(page);
  } else {
    expect(validation.status()).toBe(409);
    await expect(page.getByRole('region', { name: 'Arquivo já importado' })).toBeVisible({ timeout: 30_000 });
    await page.screenshot({ path: path.resolve('importacao-duplicate-1920.png'), fullPage: true });
    await page.getByRole('button', { name: 'Escolher outra planilha' }).click();
    await expect(page.getByRole('heading', { name: 'Envie a planilha' })).toBeVisible();
  }

  await page.locator('#import-file').setInputFiles(invalidFile);
  const rejectedResponse = page.waitForResponse(response => response.url().endsWith('/importacoes/producao/validar') && response.request().method() === 'POST');
  await page.getByRole('button', { name: /Validar arquivo/i }).click();
  expect((await rejectedResponse).status()).toBe(200);
  await expect(page.getByRole('heading', { name: 'A planilha foi rejeitada' })).toBeVisible({ timeout: 30_000 });
  await expect(page.locator('.import-error-table tbody tr').first()).toBeVisible();
  await page.screenshot({ path: path.resolve('importacao-error-1920.png'), fullPage: true });

  await page.getByRole('button', { name: /Trocar arquivo/i }).click();
  await page.getByRole('button', { name: /Voltar/i }).click();
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('heading', { name: 'Selecione o setor' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
  await page.screenshot({ path: path.resolve('importacao-mobile.png'), fullPage: true });
});
