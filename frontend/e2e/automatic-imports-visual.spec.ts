import { test, expect, type Page } from '@playwright/test';
import path from 'node:path';

const email = process.env['E2E_EMAIL'];
const password = process.env['E2E_PASSWORD'];

async function login(page: Page) {
  if (!email || !password) throw new Error('E2E_EMAIL e E2E_PASSWORD são obrigatórios para o fluxo real.');
  await page.goto('/');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(password);
  await page.getByRole('button', { name: 'ENTRAR NO SISTEMA' }).click();
  await expect(page.locator('.app-frame')).toBeVisible();
}

test('captures the real Angular automatic imports page at desktop and mobile sizes', async ({ page }) => {
  await login(page);
  await page.setViewportSize({ width: 1440, height: 900 });
  const statusResponse = page.waitForResponse(response => response.url().includes('/importacoes/automaticas/status') && response.request().method() === 'GET');
  const historyResponse = page.waitForResponse(response => response.url().includes('/importacoes/producao') && response.request().method() === 'GET');
  await page.goto('/app/importacoes-automaticas');
  expect((await statusResponse).status()).toBe(200);
  expect((await historyResponse).status()).toBe(200);
  await expect(page.getByRole('heading', { name: 'Importações Automáticas' })).toBeVisible();
  await page.screenshot({ path: path.resolve('automatic-imports-1440x900.png'), fullPage: true });
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.locator('.app-frame')).toBeVisible();
  await page.screenshot({ path: path.resolve('automatic-imports-390x844.png'), fullPage: true });
});
