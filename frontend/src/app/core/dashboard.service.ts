import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { CoreDashboard, DashboardSummary, EmployeeAnalytics, Page, ProductionGroup, SimplifiedReport, TemporalPoint } from './models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);
  summary(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<DashboardSummary>(`/empresas/${empresaId}/analytics/resumo`, params); }
  dashboard(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<CoreDashboard>(`/empresas/${empresaId}/analytics/dashboard`, params); }
  sectorDashboard(empresaId: number, sectorId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<CoreDashboard>(`/empresas/${empresaId}/analytics/dashboard/setor/${sectorId}`, params); }
  sectors(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<Page<ProductionGroup>>(`/empresas/${empresaId}/analytics/setores`, params); }
  temporalSeries(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<TemporalPoint[]>(`/empresas/${empresaId}/analytics/serie-temporal`, params); }
  employees(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<Page<ProductionGroup>>(`/empresas/${empresaId}/analytics/funcionarios`, params); }
  employee(empresaId: number, employeeId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<EmployeeAnalytics>(`/empresas/${empresaId}/analytics/funcionarios/${employeeId}`, params); }
  parts(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<Page<ProductionGroup>>(`/empresas/${empresaId}/analytics/pecas`, params); }
  machines(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<unknown>(`/empresas/${empresaId}/analytics/maquinas`, params); }
  shifts(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<unknown>(`/empresas/${empresaId}/analytics/turnos`, params); }
  stops(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<unknown>(`/empresas/${empresaId}/analytics/paradas/motivos`, params); }
  compare(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<unknown>(`/empresas/${empresaId}/analytics/comparativo`, params); }
  simplifiedReport(empresaId: number, params: Record<string, string | number | boolean> = {}) { return this.api.get<SimplifiedReport>(`/empresas/${empresaId}/relatorios/simplificado`, params); }
}
