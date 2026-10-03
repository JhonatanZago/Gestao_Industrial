import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpHeaders } from '@angular/common/http';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({ imports: [App], providers: [provideHttpClient(), provideHttpClientTesting()] }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render the product identity', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('h1')?.textContent).toContain('Gestão e inteligência');
  });

  it('should expose only authorized operational menu items', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', permissions: ['ANALYTICS_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance.visibleMenu().some(item => item.key === 'dashboard')).toBe(true);
    expect(fixture.componentInstance.visibleMenu().some(item => item.key === 'setores')).toBe(false);
    expect(fixture.componentInstance.visibleMenu().some(item => item.key === 'assistente')).toBe(true);
  });

  it('keeps production modules empty until the real API responds and validates test-only uploads', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.selectView('funcionarios');
    expect(app.rows()).toEqual([]);
    app.selectView('importacoes');
    const input = document.createElement('input');
    input.type = 'file';
    Object.defineProperty(input, 'files', { value: [new File(['fixture'], 'homologacao.xlsx')] });
    app.handleFile({ target: input } as unknown as Event);
    expect(app.importStatus()).toContain('Validar e importar');
  });

  it('rejects invalid extensions before upload and exposes explicit import states', () => {
    const fixture = TestBed.createComponent(App); const app = fixture.componentInstance; const input = document.createElement('input'); input.type = 'file';
    Object.defineProperty(input, 'files', { value: [new File(['fixture'], 'homologacao.csv')] }); app.handleFile({ target: input } as unknown as Event);
    expect(app.importState()).toBe('ERROR'); expect(app.importStatus()).toContain('XLSX');
  });

  it('uses semantic icons and only real dashboard aggregates', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['ANALYTICS_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.coreDashboard.set({ quantidadeProduzida: 350, setoresComDados: 1, funcionariosEnvolvidos: 2, codigosPecaProduzidos: 2, porSetor: [{ id: 7, nome: 'Pintura', quantidadeProduzida: 350 }] });
    expect(app.stats().map(item => item.icon)).toEqual(['production', 'sectors', 'people', 'parts']);
    expect(app.hasDashboardData()).toBe(true);
    expect(app.formatNumber(350)).toBe('350');
  });

  it('updates the shared period using the correct month boundaries', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.selectedPeriod.set('Agosto / 2026');
    expect(app.dashboardPeriod()).toEqual({ inicio: '2026-08-01T00:00:00', fim: '2026-08-31T23:59:59' });
  });

  it('exposes a safe correlation id for recoverable dashboard errors', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['ANALYTICS_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.refreshDashboard();
    const request = http.expectOne(item => item.url.includes('/analytics/dashboard'));
    request.flush({}, { status: 503, statusText: 'Unavailable', headers: new HttpHeaders({ 'X-Correlation-Id': 'cid-dashboard' }) });
    expect(app.dashboardError()).toContain('cid-dashboard');
  });

  it('keeps import validation errors actionable without exposing backend details', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.importSectorId.set(7);
    const file = new File(['fixture'], 'homologacao.xlsx');
    app.handleFile({ target: { files: [file] } } as unknown as Event);
    app.importSelectedFile();
    const request = http.expectOne(item => item.url.includes('/validar'));
    request.flush({}, { status: 422, statusText: 'Unprocessable Entity', headers: new HttpHeaders({ 'X-Correlation-Id': 'cid-import' }) });
    expect(app.importState()).toBe('ERROR');
    expect(app.importStatus()).toContain('cid-import');
  });
});
