import { test, expect } from '@playwright/test';
import path from 'node:path';

test('captures the real Angular automatic imports page at desktop and mobile sizes', async ({ page }) => {
  await page.addInitScript(() => sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'visual-session', companyId: 12, permissions: ['OPERACAO_MONITORAR'] })));
  await page.setViewportSize({ width: 1440, height: 900 });
  await page.goto('/app/importacoes-automaticas');
  await expect(page.getByRole('heading', { name: 'Importações Automáticas' })).toBeVisible();
  await page.screenshot({ path: path.resolve('automatic-imports-1440x900.png'), fullPage: true });
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.locator('.app-frame')).toBeVisible();
  await page.screenshot({ path: path.resolve('automatic-imports-390x844.png'), fullPage: true });
});
