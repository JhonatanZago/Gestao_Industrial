import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { DashboardService } from '../../core/dashboard.service';
import { ReportDrilldownComponent } from './report-drilldown.component';

describe('ReportDrilldownComponent', () => {
  const reportPeriod = { label: 'Setembro / 2026', inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59' };
  let api: { employees: ReturnType<typeof vi.fn>; parts: ReturnType<typeof vi.fn>; employee: ReturnType<typeof vi.fn>; dashboard: ReturnType<typeof vi.fn>; temporalSeries: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    api = {
      employees: vi.fn().mockReturnValue(of({ content: [{ id: 3, codigo: '003', nome: 'Operador', quantidadeProduzida: 40 }], totalPages: 1, totalElements: 1 })),
      parts: vi.fn().mockReturnValue(of({ content: [{ id: 8, codigo: 'PEC-8', nome: 'Suporte', quantidadeProduzida: 25 }], totalPages: 1, totalElements: 1 })),
      employee: vi.fn().mockReturnValue(of({ funcionarioId: 3, codigo: '003', nome: 'Operador', quantidadeProduzida: 40, quantidadeRegistros: 2, pecas: [{ id: 8, codigo: 'PEC-8', quantidadeProduzida: 40 }] })),
      dashboard: vi.fn().mockReturnValue(of({ quantidadeProduzida: 100, funcionariosEnvolvidos: 1, porPeca: [{ id: 8, quantidadeProduzida: 25, quantidadeRegistros: 2 }], porFuncionario: [{ id: 3, codigo: '003', quantidadeProduzida: 25 }] })),
      temporalSeries: vi.fn().mockReturnValue(of([{ periodo: '2026-09-02', quantidadeProduzida: 40 }]))
    };
    TestBed.configureTestingModule({ imports: [ReportDrilldownComponent], providers: [{ provide: DashboardService, useValue: api }] });
  });

  async function render(mode: 'employees' | 'employee' | 'parts' | 'part', entityId: number | null = null) {
    const fixture = TestBed.createComponent(ReportDrilldownComponent);
    fixture.componentRef.setInput('mode', mode);
    fixture.componentRef.setInput('companyId', 12);
    fixture.componentRef.setInput('period', reportPeriod);
    fixture.componentRef.setInput('sectorId', 3);
    fixture.componentRef.setInput('sectorLabel', 'KTL Homologação');
    fixture.componentRef.setInput('entityId', entityId);
    fixture.componentRef.setInput('reportDashboard', { quantidadeProduzida: 100, funcionariosEnvolvidos: 1 });
    fixture.detectChanges();
    await fixture.whenStable();
    await new Promise(resolve => setTimeout(resolve, 0));
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance };
  }

  it('loads every employee page with the selected company, period and sector, supports search/sort, and opens a real employee id', async () => {
    api.employees.mockReturnValueOnce(of({ content: [{ id: 3, codigo: '003', nome: 'Operador', quantidadeProduzida: 40 }], totalPages: 2, totalElements: 2 })).mockReturnValueOnce(of({ content: [{ id: 4, codigo: '004', nome: 'Montador', quantidadeProduzida: 60 }], totalPages: 2, totalElements: 2 }));
    const { fixture, component } = await render('employees');
    expect(api.employees).toHaveBeenNthCalledWith(1, 12, { inicio: reportPeriod.inicio, fim: reportPeriod.fim, setorId: 3, page: 0, size: 100 });
    expect(api.employees).toHaveBeenNthCalledWith(2, 12, { inicio: reportPeriod.inicio, fim: reportPeriod.fim, setorId: 3, page: 1, size: 100 });
    expect(component.rows()).toHaveLength(2);
    component.changeSort('production-asc');
    expect(component.filteredRows[0].codigo).toBe('003');
    component.setSearch('Mont');
    expect(component.filteredRows.map(row => row.codigo)).toEqual(['004']);
    const navigation = vi.fn(); component.navigate.subscribe(navigation);
    component.openRow(component.rows()[0]);
    expect(navigation).toHaveBeenCalledWith({ screen: 'employee', id: 3 });
    expect(fixture.nativeElement.textContent).toContain('Ranking completo de funcionários');
  });

  it('loads part detail through real pecaId analytics filters and preserves context on the related employee list', async () => {
    const { fixture, component } = await render('part', 8);
    expect(api.dashboard).toHaveBeenCalledWith(12, { inicio: reportPeriod.inicio, fim: reportPeriod.fim, setorId: 3, pecaId: 8 });
    expect(api.temporalSeries).toHaveBeenCalledWith(12, { inicio: reportPeriod.inicio, fim: reportPeriod.fim, setorId: 3, pecaId: 8, granularidade: 'DIA' });
    expect(component.timeSeries()).toHaveLength(1);
    const navigation = vi.fn(); component.navigate.subscribe(navigation);
    component.back();
    expect(navigation).toHaveBeenCalledWith({ screen: 'parts' });
    expect(fixture.nativeElement.textContent).toContain('Evolução da produção');
  });

  it('loads the full part ranking, supports part search and exposes a skeleton while pending', async () => {
    const pending = new Subject<{ content: Array<{ id: number; codigo: string; nome: string; quantidadeProduzida: number }>; totalPages: number; totalElements: number }>();
    api.parts.mockReturnValueOnce(pending);
    const fixture = TestBed.createComponent(ReportDrilldownComponent);
    fixture.componentRef.setInput('mode', 'parts');
    fixture.componentRef.setInput('companyId', 12);
    fixture.componentRef.setInput('period', reportPeriod);
    fixture.componentRef.setInput('sectorId', 3);
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('LOADING');
    expect(fixture.nativeElement.querySelectorAll('.skeleton-card')).toHaveLength(4);
    pending.next({ content: Array.from({ length: 30 }, (_, index) => ({ id: index + 1, codigo: `PEC-${index + 1}`, nome: `Componente ${index + 1}`, quantidadeProduzida: 100 - index })), totalPages: 1, totalElements: 30 });
    pending.complete();
    await new Promise(resolve => setTimeout(resolve, 0));
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.rows()).toHaveLength(30);
    expect(component.visibleRows).toHaveLength(25);
    component.nextPage();
    expect(component.visibleRows).toHaveLength(5);
    expect(component.page()).toBe(1);
    component.setSearch('PEC-29');
    expect(component.filteredRows.map(row => row.codigo)).toEqual(['PEC-29']);
    expect(fixture.nativeElement.textContent).toContain('Ranking completo de peças');
  });

  it('uses employee detail plus temporal analytics and renders empty and retryable error states', async () => {
    const { component } = await render('employee', 3);
    expect(api.employee).toHaveBeenCalledWith(12, 3, { inicio: reportPeriod.inicio, fim: reportPeriod.fim, setorId: 3 });
    expect(component.employee()?.quantidadeProduzida).toBe(40);
    api.parts.mockReturnValueOnce(of({ content: [], totalPages: 0, totalElements: 0 }));
    const empty = await render('parts');
    expect(empty.component.state()).toBe('EMPTY');
    api.parts.mockReturnValueOnce(throwError(() => ({ status: 403 })));
    const failed = await render('parts');
    expect(failed.component.state()).toBe('ERROR');
    expect(failed.component.error()).toContain('Não foi possível carregar');
  });
});
