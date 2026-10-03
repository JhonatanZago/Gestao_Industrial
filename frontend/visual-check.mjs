import { chromium } from 'playwright';

const browser = await chromium.launch({ headless: true });
const payload = {
  quantidadeProduzida: 1280, duracaoRegistradaMinutos: 742, setoresComDados: 4,
  funcionariosEnvolvidos: 18, codigosPecaProduzidos: 9,
  porSetor: [
    { id: 1, codigo: 'CORTE', nome: 'Corte', quantidadeProduzida: 420 },
    { id: 2, codigo: 'SOLDA', nome: 'Solda', quantidadeProduzida: 330 },
    { id: 3, codigo: 'USIN', nome: 'Usinagem', quantidadeProduzida: 290 },
    { id: 4, codigo: 'ACAB', nome: 'Acabamento', quantidadeProduzida: 240 }
  ],
  porFuncionario: [1, 2, 3, 4, 5].map((n) => ({ id: n, codigo: `FUNC-${n}`, nome: `Operador ${n}`, quantidadeProduzida: 220 - n * 17 })),
  porPeca: [1, 2, 3, 4, 5].map((n) => ({ id: n, codigo: `P-${n}`, nome: `Componente ${n}`, quantidadeProduzida: 190 - n * 14 })),
  evolucao: [
    { inicio: '01/09', quantidadeProduzida: 120 }, { inicio: '05/09', quantidadeProduzida: 210 }, { inicio: '10/09', quantidadeProduzida: 165 },
    { inicio: '15/09', quantidadeProduzida: 280 }, { inicio: '20/09', quantidadeProduzida: 235 }, { inicio: '25/09', quantidadeProduzida: 330 }, { inicio: '30/09', quantidadeProduzida: 290 }
  ]
};

for (const [width, file] of [[1366, 'visual-1366.png'], [1920, 'visual-1920.png']]) {
  const page = await browser.newPage({ viewport: { width, height: 768 } });
  await page.addInitScript(() => sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'visual-token', companyId: 12, permissions: ['ANALYTICS_VISUALIZAR', 'SETOR_VISUALIZAR', 'PRODUCAO_IMPORTAR', 'RELATORIO_VISUALIZAR'] })));
  await page.route('**/api/v1/empresas/12/analytics/dashboard**', async route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(payload) }));
  await page.goto('http://127.0.0.1:4202/', { waitUntil: 'networkidle' });
  await page.getByRole('button', { name: /^Atualizar$/i }).click();
  await page.locator('.stats-grid').waitFor({ state: 'visible', timeout: 5000 });
  await page.screenshot({ path: file, fullPage: true });
  await page.close();
}
await browser.close();
