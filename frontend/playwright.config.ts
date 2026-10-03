import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  fullyParallel: false,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: { baseURL: process.env['FRONTEND_BASE_URL'] ?? 'http://127.0.0.1:4200', trace: 'retain-on-failure', ...devices['Desktop Chrome'] },
  webServer: process.env['FRONTEND_BASE_URL'] ? undefined : { command: 'npm start -- --host 127.0.0.1', url: 'http://127.0.0.1:4200', reuseExistingServer: true, timeout: 120_000 }
});
