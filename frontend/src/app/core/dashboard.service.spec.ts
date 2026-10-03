import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { findByOperationalCode } from './models';
import { QueryCoordinator } from './query-coordinator';
import { of } from 'rxjs';

describe('DashboardService drill-down contract', () => {
  let service: DashboardService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [DashboardService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(DashboardService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('keeps the selected entity id and filters unchanged', () => { service.employee(12, 44, { inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59', setorId: 3 }).subscribe(); const req = http.expectOne('/api/v1/empresas/12/analytics/funcionarios/44?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:59&setorId=3'); expect(req.request.method).toBe('GET'); req.flush({ production: 10 }); });
  it('supports temporary test fixtures without embedding production records', () => { service.parts(12, { page: 0, size: 20 }).subscribe(value => expect(value).toEqual({ content: [] })); http.expectOne('/api/v1/empresas/12/analytics/pecas?page=0&size=20').flush({ content: [] }); });
  it('uses the homologated core dashboard contract and preserves totals', () => { service.dashboard(12, { inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59' }).subscribe(value => expect(value.quantidadeProduzida).toBe(350)); const req = http.expectOne('/api/v1/empresas/12/analytics/dashboard?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:59'); expect(req.request.method).toBe('GET'); req.flush({ quantidadeProduzida: 350, funcionariosEnvolvidos: 2, codigosPecaProduzidos: 2 }); });
  it('loads a sector dashboard and simplified report from the real endpoints', () => { service.sectorDashboard(12, 7, { inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59' }).subscribe(); http.expectOne('/api/v1/empresas/12/analytics/dashboard/setor/7?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:59').flush({ quantidadeProduzida: 350 }); service.simplifiedReport(12, { inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59' }).subscribe(); http.expectOne('/api/v1/empresas/12/relatorios/simplificado?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:59').flush({ dashboard: { quantidadeProduzida: 350 } }); });
  it('preserves leading zero operational codes', () => { expect(findByOperationalCode([{ codigo: '00147' }, { codigo: '00152' }], '00147')?.codigo).toBe('00147'); expect(findByOperationalCode([{ codigo: '00147' }], '147')).toBeUndefined(); });
  it('cancels obsolete filter requests through the shared coordinator', async () => { const coordinator = new QueryCoordinator((query: string) => of(query), 0); const values: string[] = []; coordinator.results.subscribe(value => values.push(value)); coordinator.next('old'); coordinator.next('new'); await new Promise(resolve => setTimeout(resolve, 10)); expect(values).toEqual(['new']); });
});
