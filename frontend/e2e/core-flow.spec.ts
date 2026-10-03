import { expect, test } from '@playwright/test';

test('autentica e abre o fluxo real de importação sem mocks', async ({ page }) => {
  const email = process.env['E2E_EMAIL'];
  const password = process.env['E2E_PASSWORD'];
  if (!email || !password) throw new Error('E2E_EMAIL e E2E_PASSWORD são obrigatórios.');

  await page.goto('/');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(password);
  await page.getByRole('button', { name: 'ENTRAR NO SISTEMA' }).click();
  await expect(page.locator('.app-frame')).toBeVisible();

  const sectorsResponse = page.waitForResponse(response => response.url().includes('/api/v1/empresas/1/setores') && response.request().method() === 'GET');
  await page.getByRole('navigation', { name: /Navegação principal/i }).getByRole('button', { name: 'Importar Excel' }).click();
  expect((await sectorsResponse).status()).toBe(200);
  await expect(page.locator('.import-stepper li')).toHaveCount(4);
  await expect(page.getByRole('heading', { name: 'Selecione o setor' })).toBeVisible();
});
