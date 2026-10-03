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
    expect(app.importStatus()).toContain('Pronto para validar');
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

  it('loads real sectors, requires an explicit selection and advances to the file step', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);

    app.selectView('importacoes');
    expect(app.importSectorLoading()).toBe(true);
    http.expectOne(request => request.url === '/api/v1/empresas/12/setores').flush({ content: [{ id: 3, codigo: 'HML-KTL', nome: 'KTL Homologacao', ativo: true }], number: 0, size: 100, totalElements: 1, totalPages: 1 });

    expect(app.importSectorId()).toBeNull();
    app.continueImport();
    expect(app.importState()).toBe('SECTOR');
    app.selectImportSector(3);
    app.continueImport();
    expect(app.importState()).toBe('FILE');
    expect(app.selectedImportSector()?.codigo).toBe('HML-KTL');
  });

  it('selects and removes an XLSX without sending a request', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const file = new File(['fixture'], 'producao.xlsx');

    app.handleFile({ target: { files: [file] } } as unknown as Event);
    expect(app.importState()).toBe('SELECTED');
    expect(app.formatFileSize(file.size)).toContain('B');
    app.removeImportFile();
    expect(app.selectedFileObject()).toBeNull();
    expect(app.importState()).toBe('FILE');
  });

  it('validates through the backend without importing before confirmation', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.importSectorId.set(3);
    app.handleFile({ target: { files: [new File(['fixture'], 'producao.xlsx')] } } as unknown as Event);

    app.validateSelectedFile();
    const validation = http.expectOne('/api/v1/empresas/12/setores/3/importacoes/producao/validar');
    expect(validation.request.method).toBe('POST');
    expect(validation.request.body instanceof FormData).toBe(true);
    validation.flush({ id: 90, setorId: 3, setorNome: 'KTL Homologacao', nomeArquivo: 'producao.xlsx', status: 'VALIDADA', totalLinhas: 3, linhasValidas: 3, linhasInvalidas: 0, linhasImportadas: 0 });

    expect(app.importState()).toBe('VALIDATED');
    expect(app.importValidation()?.linhasValidas).toBe(3);
    http.expectNone(request => request.url === '/api/v1/empresas/12/setores/3/importacoes/producao');
  });

  it('loads real line details when backend validation rejects the workbook', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.importSectorId.set(3);
    app.handleFile({ target: { files: [new File(['fixture'], 'invalida.xlsx')] } } as unknown as Event);

    app.validateSelectedFile();
    http.expectOne('/api/v1/empresas/12/setores/3/importacoes/producao/validar').flush({ id: 91, status: 'REJEITADA', totalLinhas: 2, linhasValidas: 1, linhasInvalidas: 1, erroCodigo: 'LINHAS_INVALIDAS' });
    const lines = http.expectOne(request => request.url === '/api/v1/empresas/12/setores/3/importacoes/producao/91/linhas');
    expect(lines.request.params.get('size')).toBe('100');
    lines.flush({ content: [{ id: 2, numeroLinha: 15, status: 'INVALIDA', codigoFuncionario: '00199', codigoErro: 'FUNCIONARIO_NAO_ENCONTRADO', mensagem: 'Funcionário não encontrado' }], number: 0, size: 100, totalElements: 1, totalPages: 1 });

    expect(app.importState()).toBe('VALIDATION_ERROR');
    expect(app.importLineErrors()[0].numeroLinha).toBe(15);
    expect(app.importLineErrors()[0].codigoErro).toBe('FUNCIONARIO_NAO_ENCONTRADO');
  });

  it('confirms once, imports atomically and exposes the real success result', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.sectorsOptions.set([{ id: 3, codigo: 'HML-KTL', nome: 'KTL Homologacao' }]);
    app.importSectorId.set(3);
    app.handleFile({ target: { files: [new File(['fixture'], 'producao.xlsx')] } } as unknown as Event);
    app.validateSelectedFile();
    http.expectOne('/api/v1/empresas/12/setores/3/importacoes/producao/validar').flush({ id: 90, status: 'VALIDADA', linhasValidas: 3, linhasInvalidas: 0 });

    app.openImportConfirmation();
    expect(app.showImportConfirmation()).toBe(true);
    app.confirmImport();
    const importing = http.expectOne('/api/v1/empresas/12/setores/3/importacoes/producao');
    expect(importing.request.method).toBe('POST');
    app.confirmImport();
    http.expectNone('/api/v1/empresas/12/setores/3/importacoes/producao');
    importing.flush({ id: 92, setorId: 3, setorNome: 'KTL Homologacao', status: 'CONCLUIDA', totalLinhas: 3, linhasValidas: 3, linhasInvalidas: 0, linhasImportadas: 3 });

    expect(app.importState()).toBe('SUCCESS');
    expect(app.importResult()?.linhasImportadas).toBe(3);
    expect(app.importBusy()).toBe(false);
    app.resetImport();
    expect(app.importState()).toBe('SECTOR');
    expect(app.importSectorId()).toBeNull();
  });

  it('maps backend duplicate protection to a dedicated recoverable state', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.importSectorId.set(3);
    app.handleFile({ target: { files: [new File(['fixture'], 'repetida.xlsx')] } } as unknown as Event);

    app.validateSelectedFile();
    http.expectOne('/api/v1/empresas/12/setores/3/importacoes/producao/validar').flush({ code: 'BUSINESS_RULE_VIOLATION', message: 'ARQUIVO_DUPLICADO' }, { status: 409, statusText: 'Conflict' });
    expect(app.importState()).toBe('DUPLICATE');
    expect(app.importStatus()).toContain('já foi processada');
  });

  it('renders the accessible stepper and official template download without a period control', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['PRODUCAO_IMPORTAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.selectView('importacoes');
    http.expectOne(request => request.url === '/api/v1/empresas/12/setores').flush({ content: [], number: 0, size: 100, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.querySelectorAll('.import-stepper li').length).toBe(4);
    expect(host.querySelector('.header-actions')).toBeNull();
    expect(host.querySelector<HTMLAnchorElement>('.import-template-card a')?.getAttribute('href')).toBe('/templates/PRODUCAO_PADRAO.xlsx');
  });

  it('loads the sector catalog and binds production only from the real analytics response', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['SETOR_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);

    app.selectView('setores');
    expect(app.sectorsLoading()).toBe(true);

    const catalog = http.expectOne(request => request.url === '/api/v1/empresas/12/setores');
    catalog.flush({ content: [{ id: 3, codigo: 'HML-KTL', nome: 'KTL Homologacao' }], number: 0, size: 100, totalElements: 1, totalPages: 1 });
    expect(app.sectorRecords()).toEqual([]);

    const dashboard = http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/dashboard');
    expect(dashboard.request.params.get('inicio')).toBe('2026-09-01T00:00:00');
    dashboard.flush({ quantidadeProduzida: 31905, setoresComDados: 1, funcionariosEnvolvidos: 20 });

    const sectors = http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/setores');
    expect(sectors.request.params.get('fim')).toBe('2026-09-30T23:59:59');
    sectors.flush({ content: [{ id: 3, codigo: 'HML-KTL', nome: 'KTL Homologacao', quantidadeProduzida: 31905, quantidadeRegistros: 182, duracaoBrutaMinutos: 15645 }], number: 0, size: 100, totalElements: 1, totalPages: 1 });

    expect(app.sectorsLoading()).toBe(false);
    expect(app.sectorRecords()[0].quantidadeProduzida).toBe(31905);
    expect(app.sectorOverviewStats().map(item => item.value)).toEqual(['1', '31.905', '20']);
  });

  it('refreshes sector analytics with the selected period and prevents concurrent refreshes', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['SETOR_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    app.activeView.set('setores');

    app.selectPeriod('Agosto / 2026');
    app.refreshCurrentView();

    const dashboard = http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/dashboard');
    const sectors = http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/setores');
    expect(dashboard.request.params.get('inicio')).toBe('2026-08-01T00:00:00');
    expect(sectors.request.params.get('fim')).toBe('2026-08-31T23:59:59');
    dashboard.flush({ quantidadeProduzida: 0, setoresComDados: 0, funcionariosEnvolvidos: 0 });
    sectors.flush({ content: [], number: 0, size: 100, totalElements: 0, totalPages: 0 });
    expect(app.refreshState()).toBe('ATUALIZADO');
  });

  it('opens sector detail by its real id and returns to the sector list', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['SETOR_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    const sector = { id: 3, codigo: 'HML-KTL', nome: 'KTL Homologacao', quantidadeProduzida: 31905 };

    app.loadSectorDashboard(sector);
    const detail = http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/dashboard/setor/3');
    expect(detail.request.method).toBe('GET');
    detail.flush({ quantidadeProduzida: 31905, funcionariosEnvolvidos: 20, codigosPecaProduzidos: 8, duracaoRegistradaMinutos: 15645, porFuncionario: [], porPeca: [], evolucao: [] });

    expect(app.activeView()).toBe('setor-detalhe');
    expect(app.title()).toBe('KTL Homologacao');
    expect(app.sectorDetailStats().map(item => item.value)).toEqual(['31.905', '20', '8', '15.645']);
    app.backToSectors();
    expect(app.activeView()).toBe('setores');
  });

  it('renders premium loading, empty and recoverable error states for sectors', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'test-token', companyId: 12, permissions: ['SETOR_VISUALIZAR'] }));
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);

    app.selectView('setores');
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('.sector-card--skeleton').length).toBe(3);

    http.expectOne(request => request.url === '/api/v1/empresas/12/setores').flush({ content: [], number: 0, size: 100, totalElements: 0, totalPages: 0 });
    http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/dashboard').flush({ quantidadeProduzida: 0, setoresComDados: 0, funcionariosEnvolvidos: 0 });
    http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/setores').flush({ content: [], number: 0, size: 100, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('.sector-state--empty')?.textContent).toContain('Nenhum setor encontrado');

    app.loadSectorOverview();
    const cancelledSectors = http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/setores');
    http.expectOne(request => request.url === '/api/v1/empresas/12/analytics/dashboard').flush({}, { status: 503, statusText: 'Unavailable' });
    expect(cancelledSectors.cancelled).toBe(true);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('.sector-state--error')?.textContent).toContain('Tentar novamente');
  });
});
