import { expect, test, type Page } from '@playwright/test';
import path from 'node:path';

const email = process.env['E2E_EMAIL'];
const password = process.env['E2E_PASSWORD'];

test.setTimeout(60_000);

async function login(page: Page) {
  if (!email || !password) throw new Error('E2E_EMAIL e E2E_PASSWORD são obrigatórios para validar dados reais.');
  await page.goto('/');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(password);
  await page.getByRole('button', { name: 'ENTRAR NO SISTEMA' }).click();
  await expect(page.locator('.app-frame')).toBeVisible();
}

async function openSectors(page: Page) {
  const analyticsResponse = page.waitForResponse(response => response.url().includes('/analytics/setores') && response.request().method() === 'GET');
  await page.getByRole('navigation', { name: /Navegação principal/i }).getByRole('button', { name: 'Setores' }).click();
  expect((await analyticsResponse).status()).toBe(200);
  await expect(page.locator('button.sector-card')).toHaveCount(3);
}

test('setores usa dados reais, atualiza período e permite navegar por todos os cards', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 });
  await login(page);
  await openSectors(page);

  expect((await page.locator('button.sector-card').allTextContents()).join(' ')).not.toContain('- peças');
  await expect(page.getByText('41.710', { exact: true })).toBeVisible();

  for (const sectorName of ['KTL Homologacao', 'Pintura Homologacao', 'Solda Homologacao']) {
    const detailResponse = page.waitForResponse(response => /\/analytics\/dashboard\/setor\/\d+/.test(response.url()));
    await page.locator('button.sector-card').filter({ hasText: sectorName }).click();
    expect((await detailResponse).status()).toBe(200);
    await expect(page.locator('.sector-detail-view')).toBeVisible();
    await expect(page.getByRole('heading', { name: sectorName })).toBeVisible();
    if (sectorName === 'KTL Homologacao') {
      const employeeResponse = page.waitForResponse(response => /\/analytics\/funcionarios\/\d+/.test(response.url()));
      await page.locator('button.sector-detail-ranking-row').first().click();
      expect((await employeeResponse).status()).toBe(200);
      await expect(page.locator('.employee-detail-panel')).toBeVisible();
      await page.locator('.employee-detail-panel').getByRole('button', { name: 'Fechar' }).click();
    }
    await page.getByRole('button', { name: /Voltar para setores/i }).click();
    await expect(page.locator('button.sector-card')).toHaveCount(3);
  }

  const periodResponse = page.waitForResponse(response => response.url().includes('/analytics/setores') && response.url().includes('2026-08-01'));
  await page.locator('#period').selectOption({ label: 'Agosto / 2026' });
  expect((await periodResponse).status()).toBe(200);

  const restoreResponse = page.waitForResponse(response => response.url().includes('/analytics/setores') && response.url().includes('2026-09-01'));
  await page.locator('#period').selectOption({ label: 'Setembro / 2026' });
  expect((await restoreResponse).status()).toBe(200);

  const refreshResponse = page.waitForResponse(response => response.url().includes('/analytics/setores') && response.url().includes('2026-09-01'));
  await page.locator('.refresh-button').click();
  expect((await refreshResponse).status()).toBe(200);
  await expect(page.locator('.refresh-button')).toContainText('Atualizado');
});

test('setores permanece responsivo e gera as capturas finais', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 });
  await login(page);
  await openSectors(page);
  await page.waitForTimeout(500);
  await page.screenshot({ path: path.resolve('setores-final-1920.png'), fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);

  const detailResponse = page.waitForResponse(response => /\/analytics\/dashboard\/setor\/\d+/.test(response.url()));
  await page.locator('button.sector-card').filter({ hasText: 'KTL Homologacao' }).click();
  expect((await detailResponse).status()).toBe(200);
  await page.waitForTimeout(600);
  await page.screenshot({ path: path.resolve('setor-detalhe-final-1920.png'), fullPage: true });
  await page.getByRole('button', { name: /Voltar para setores/i }).click();

  await page.setViewportSize({ width: 1366, height: 768 });
  await expect(page.locator('button.sector-card')).toHaveCount(3);
  await page.waitForTimeout(300);
  await page.screenshot({ path: path.resolve('setores-final-1366.png'), fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
});
