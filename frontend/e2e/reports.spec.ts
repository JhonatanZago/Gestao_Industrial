import { expect, test, type Page } from '@playwright/test';

const email = process.env['E2E_EMAIL'];
const password = process.env['E2E_PASSWORD'];

async function login(page: Page) {
  if (!email || !password) throw new Error('E2E_EMAIL and E2E_PASSWORD are required for the real backend flow.');
  await page.goto('/');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(password);
  await page.getByRole('button', { name: 'ENTRAR NO SISTEMA' }).click();
  await expect(page.locator('.app-frame')).toBeVisible();
}

test('reports queries, filters, previews and exports production through the real API', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 });
  await login(page);
  await page.getByRole('navigation', { name: /Navega/ }).getByRole('button', { name: /Relat/ }).click();
  const reportResponse = page.waitForResponse(response => response.url().includes('/relatorios/simplificado') && response.request().method() === 'GET');
  expect((await reportResponse).status()).toBe(200);
  await expect(page.locator('.report-kpi')).toHaveCount(4);
  await expect(page.locator('.report-preview')).toContainText('Relatório de Produção');
  await page.screenshot({ path: 'relatorios-config-1920.png', fullPage: true });
  await page.locator('.report-generate-button').first().click();
  await expect(page.locator('.report-preview')).toContainText('Exportar CSV');
  await page.screenshot({ path: 'relatorios-preview-1920.png', fullPage: true });

  await page.locator('#report-sector').selectOption('3');
  const sectorReport = page.waitForResponse(response => response.url().includes('/relatorios/simplificado') && response.url().includes('setorId=3'));
  await page.locator('.report-generate-button').first().click();
  expect((await sectorReport).status()).toBe(200);
  await expect(page.locator('.report-preview-summary')).toContainText('31.905');
  await expect(page.locator('.report-rankings')).toBeVisible();
  await page.screenshot({ path: 'relatorios-setor-1920.png', fullPage: true });

  const employeeList = page.waitForResponse(response => response.url().includes('/analytics/funcionarios?') && response.status() === 200);
  await page.getByRole('button', { name: /Ver todos os funcion/ }).click();
  expect((await employeeList).status()).toBe(200);
  await expect(page.locator('app-report-drilldown').getByRole('heading', { name: 'Visão Geral de Funcionários' })).toBeVisible();
  await expect(page.locator('.ranking-panel tbody tr').first()).toBeVisible();
  await page.screenshot({ path: 'relatorios-funcionarios-1920.png', fullPage: true });
  await page.setViewportSize({ width: 1366, height: 768 });
  await page.screenshot({ path: 'relatorios-funcionarios-1366.png', fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
  await page.setViewportSize({ width: 1920, height: 1080 });
  const employeeDetail = page.waitForResponse(response => /\/analytics\/funcionarios\/\d+\?/.test(response.url()) && response.status() === 200);
  await page.locator('.ranking-panel tbody tr').first().click();
  expect((await employeeDetail).status()).toBe(200);
  await expect(page.locator('app-report-drilldown').getByRole('heading', { name: 'Visão do Funcionário' })).toBeVisible();
  await expect(page.locator('.related-panel')).toBeVisible();
  await page.screenshot({ path: 'relatorios-funcionario-detalhe-1920.png', fullPage: true });
  await page.getByRole('button', { name: 'Funcionários' }).click();
  await expect(page.locator('app-report-drilldown').getByRole('heading', { name: 'Visão Geral de Funcionários' })).toBeVisible();
  await page.getByRole('button', { name: 'Relatórios' }).click();
  await expect(page.locator('.report-preview')).toBeVisible();

  const partsList = page.waitForResponse(response => response.url().includes('/analytics/pecas?') && response.status() === 200);
  await page.getByRole('button', { name: /Ver todas as pe/ }).click();
  expect((await partsList).status()).toBe(200);
  await expect(page.locator('app-report-drilldown').getByRole('heading', { name: 'Visão Geral de Peças' })).toBeVisible();
  await expect(page.locator('.ranking-panel tbody tr').first()).toBeVisible();
  await page.screenshot({ path: 'relatorios-pecas-1920.png', fullPage: true });
  await page.setViewportSize({ width: 1366, height: 768 });
  await page.screenshot({ path: 'relatorios-pecas-1366.png', fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
  await page.setViewportSize({ width: 1920, height: 1080 });
  const partDetailData = page.waitForResponse(response => response.url().includes('/analytics/dashboard?') && response.url().includes('pecaId=') && response.status() === 200);
  await page.locator('.ranking-panel tbody tr').first().click();
  expect((await partDetailData).status()).toBe(200);
  await expect(page.locator('app-report-drilldown').getByRole('heading', { name: 'Visão da Peça' })).toBeVisible();
  await expect(page.locator('.related-panel')).toBeVisible();
  await page.screenshot({ path: 'relatorios-peca-detalhe-1920.png', fullPage: true });
  await page.getByRole('button', { name: 'Peças' }).click();
  await expect(page.locator('app-report-drilldown').getByRole('heading', { name: 'Visão Geral de Peças' })).toBeVisible();
  await page.getByRole('button', { name: 'Relatórios' }).click();
  await expect(page.locator('.report-preview')).toBeVisible();

  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Exportar CSV' }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('relatorio-executivo-2026-09-01-a-2026-09-30.csv');

  await page.setViewportSize({ width: 1366, height: 768 });
  await page.screenshot({ path: 'relatorios-1366.png', fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);

  await page.route(url => url.href.includes('/relatorios/simplificado'), route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ dashboard: { quantidadeProduzida: 0, setoresComDados: 0, funcionariosEnvolvidos: 0, codigosPecaProduzidos: 0, porSetor: [], porFuncionario: [], porPeca: [], evolucao: [] } })
  }));
  await page.setViewportSize({ width: 1920, height: 1080 });
  await page.locator('#period').selectOption({ label: 'Outubro / 2026' });
  await expect(page.locator('.report-state--empty')).toBeVisible();
  await page.screenshot({ path: 'relatorios-empty-1920.png', fullPage: true });

  await page.unrouteAll();
  await page.setViewportSize({ width: 1920, height: 1080 });
  await page.route(url => url.href.includes('/relatorios/simplificado'), route => route.fulfill({ status: 503, contentType: 'application/json', body: '{"code":"INTERNAL_ERROR"}' }));
  await page.locator('.refresh-button').click();
  await expect(page.locator('.report-state--error')).toBeVisible();
  await page.screenshot({ path: 'relatorios-error-1920.png', fullPage: true });
});
