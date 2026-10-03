import { test, expect } from '@playwright/test';

for (const viewport of [{ width: 1920, height: 1080 }, { width: 1366, height: 768 }, { width: 900, height: 900 }, { width: 390, height: 844 }]) {
  test(`mantém o login utilizável em ${viewport.width}x${viewport.height}`, async ({ page }) => {
    await page.setViewportSize(viewport);
    await page.goto('/');
    await expect(page.getByLabel('E-mail')).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1)).toBe(true);
  });
}
