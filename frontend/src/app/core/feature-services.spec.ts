import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ResourceService, clean, reportFilename, sanitizeAudit } from './feature-services';

describe('ResourceService contract boundaries', () => {
  let service: ResourceService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [ResourceService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(ResourceService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('removes empty optional filters and preserves server pagination', () => { service.page('setores', 7, { nome: '', ativo: true, page: 1 }).subscribe(result => expect(result.totalPages).toBe(1)); const req = http.expectOne(r => r.url === '/api/v1/empresas/7/setores'); expect(req.request.params.has('nome')).toBe(false); expect(req.request.params.get('ativo')).toBe('true'); req.flush({ content: [{ id: 3 }], number: 1, size: 20, totalElements: 1, totalPages: 1 }); });
  it('uses only the assistant contract and keeps backend status', () => { service.assistant(7, { question: 'total do período' }).subscribe(answer => expect(answer.status).toBe('SEM_DADOS')); const req = http.expectOne('/api/v1/empresas/7/assistente/perguntar'); expect(req.request.method).toBe('POST'); req.flush({ status: 'SEM_DADOS', answer: null }); });
  it('creates the multipart validation request without a production fixture', () => { service.validateImport(7, 2, new File(['temporary-test-fixture'], 'valid.xlsx')).subscribe(); const req = http.expectOne('/api/v1/empresas/7/setores/2/importacoes/producao/validar'); expect(req.request.method).toBe('POST'); expect(req.request.body instanceof FormData).toBe(true); req.flush({ status: 'VALIDADA' }); });
  it('cleans query values deterministically', () => expect(clean({ a: '', b: false, c: 0, d: undefined })).toEqual({ b: 'false', c: '0' }));
  it('removes secrets before audit values reach the template', () => expect(sanitizeAudit({ actor: 'op', password: 'hidden', nested: { token: 'hidden', action: 'UPDATE' } })).toEqual({ actor: 'op', nested: { action: 'UPDATE' } }));
  it('downloads only the supported CSV/XLSX contracts as blobs and reads the server filename', () => {
    service.exportReport(7, 'CSV', { inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59', setorId: 3 }).subscribe(response => expect(response.body).toBeInstanceOf(Blob));
    const request = http.expectOne(r => r.url === '/api/v1/empresas/7/relatorios/executivo/exportar');
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    expect(request.request.params.get('formato')).toBe('CSV');
    expect(request.request.params.get('setorId')).toBe('3');
    request.flush(new Blob(['sep=;']), { headers: { 'Content-Type': 'text/csv; charset=UTF-8', 'Content-Disposition': 'attachment; filename="relatorio-executivo-2026-09.csv"' } });
    expect(reportFilename('attachment; filename="relatorio.csv"', 'fallback.csv')).toBe('relatorio.csv');
    expect(reportFilename("attachment; filename*=UTF-8''relat%C3%B3rio.xlsx", 'fallback.xlsx')).toBe('relatório.xlsx');
    expect(reportFilename(null, 'fallback.csv')).toBe('fallback.csv');
  });
});
