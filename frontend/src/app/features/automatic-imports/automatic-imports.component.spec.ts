import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AutomaticImportsComponent } from './automatic-imports.component';

const status = { enabled: true, monitoramentoAtivo: true, status: 'ATIVO', ultimaVerificacao: '2026-10-05T17:42:00-03:00', ultimaImportacao: null, reconciliacao: { enabled: true, cron: '0 0 18 * * *', timezone: 'America/Sao_Paulo' }, fontes: [{ setorId: 3, setor: 'KTL', habilitada: true, acessivel: true, status: 'ONLINE', ultimoCheck: '2026-10-05T17:42:00-03:00', ultimaImportacao: null }] };
const page = { content: [{ id: 41, empresaId: 12, setorId: 3, setorCodigo: 'KTL', setorNome: 'KTL', nomeArquivo: 'producao.xlsx', hashArquivo: 'abcdef1234567890', origem: 'FILE_WATCHER', status: 'CONCLUIDA', totalLinhas: 10, linhasValidas: 10, linhasInvalidas: 0, linhasImportadas: 9, linhasIgnoradas: 1, iniciadoEm: '2026-10-05T17:40:00-03:00', finalizadoEm: '2026-10-05T17:41:00-03:00', createdAt: '2026-10-05T17:40:00-03:00', correlationId: null, erroCodigo: null, erroMensagem: null }], number: 0, size: 20, totalElements: 1, totalPages: 1 };

describe('AutomaticImportsComponent', () => {
  beforeEach(async () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ companyId: 12, token: 'test', permissions: ['OPERACAO_MONITORAR'] }));
    await TestBed.configureTestingModule({ imports: [AutomaticImportsComponent], providers: [provideHttpClient(), provideHttpClientTesting()] }).compileComponents();
  });
  afterEach(() => sessionStorage.clear());

  it('loads the real status and paginated history contract', () => {
    const fixture = TestBed.createComponent(AutomaticImportsComponent); const http = TestBed.inject(HttpTestingController); fixture.detectChanges();
    http.expectOne('/api/v1/empresas/12/importacoes/automaticas/status').flush(status);
    const history = http.expectOne(request => request.url === '/api/v1/empresas/12/importacoes/producao' && request.params.get('page') === '0' && request.params.get('size') === '20'); history.flush(page);
    expect(fixture.componentInstance.status()?.status).toBe('ATIVO'); expect(fixture.componentInstance.history()[0].origem).toBe('FILE_WATCHER');
  });
  it('sends only backend-supported filters and keeps origin as presentation', () => {
    const fixture = TestBed.createComponent(AutomaticImportsComponent); const component = fixture.componentInstance; const http = TestBed.inject(HttpTestingController); fixture.detectChanges();
    http.expectOne('/api/v1/empresas/12/importacoes/automaticas/status').flush({ ...status, enabled: false, monitoramentoAtivo: false, status: 'DESATIVADO', fontes: [] }); http.expectOne(request => request.url.endsWith('/importacoes/producao')).flush({ ...page, content: [] });
    component.sectorId.set('3'); component.statusFilter.set('REJEITADA'); component.applyFilters(); const request = http.expectOne(item => item.url.endsWith('/importacoes/producao')); expect(request.request.params.get('setorId')).toBe('3'); expect(request.request.params.get('status')).toBe('REJEITADA'); expect(request.request.params.has('origem')).toBe(false); request.flush({ ...page, content: [] });
    expect(component.globalLabel('DESATIVADO')).toContain('desativado');
  });
  it('opens a sanitized detail using the existing history endpoint', () => {
    const fixture = TestBed.createComponent(AutomaticImportsComponent); const component = fixture.componentInstance; const http = TestBed.inject(HttpTestingController); fixture.detectChanges();
    http.expectOne('/api/v1/empresas/12/importacoes/automaticas/status').flush(status); http.expectOne(request => request.url.endsWith('/importacoes/producao')).flush(page); component.openDetail(page.content[0]); const request = http.expectOne('/api/v1/empresas/12/importacoes/producao/41'); request.flush(page.content[0]); expect(component.shortHash(page.content[0].hashArquivo)).toBe('abcdef1234…');
  });
});
