import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { DashboardService } from '../../core/dashboard.service';
import { CoreDashboard, EmployeeAnalytics, ProductionGroup, TemporalPoint } from '../../core/models';
import { UiChartComponent } from '../../shared/ui-chart.component';
import { UiIconComponent } from '../../shared/ui-icon.component';

export type ReportDrilldownMode = 'employees' | 'employee' | 'parts' | 'part';
export interface ReportDrilldownNavigation { screen: 'relatorios' | 'employees' | 'employee' | 'parts' | 'part'; id?: number; }

@Component({
  selector: 'app-report-drilldown',
  standalone: true,
  imports: [CommonModule, FormsModule, UiChartComponent, UiIconComponent],
  templateUrl: './report-drilldown.component.html',
  styleUrl: './report-drilldown.component.scss'
})
export class ReportDrilldownComponent implements OnChanges {
  @Input({ required: true }) mode!: ReportDrilldownMode;
  @Input({ required: true }) companyId!: number;
  @Input({ required: true }) period!: { label: string; inicio: string; fim: string };
  @Input() sectorId: number | null = null;
  @Input() entityId: number | null = null;
  @Input() sectorLabel = 'Todos os setores';
  @Input() reportDashboard: CoreDashboard | null = null;
  @Input() reloadToken = 0;
  @Output() navigate = new EventEmitter<ReportDrilldownNavigation>();
  @Output() loadState = new EventEmitter<'LOADING' | 'SUCCESS' | 'EMPTY' | 'ERROR'>();

  private readonly api = inject(DashboardService);
  readonly state = signal<'LOADING' | 'SUCCESS' | 'EMPTY' | 'ERROR'>('LOADING');
  readonly error = signal('');
  readonly rows = signal<ProductionGroup[]>([]);
  readonly relatedRows = signal<ProductionGroup[]>([]);
  readonly employee = signal<EmployeeAnalytics | null>(null);
  readonly detailDashboard = signal<CoreDashboard | null>(null);
  readonly scopeDashboard = signal<CoreDashboard | null>(null);
  readonly timeSeries = signal<TemporalPoint[]>([]);
  readonly search = signal('');
  readonly sort = signal<'production-desc' | 'production-asc' | 'code'>('production-desc');
  readonly page = signal(0);
  readonly pageSize = 25;
  readonly trendLimit = 10;
  private requestId = 0;

  ngOnChanges(changes: SimpleChanges) {
    if (['mode', 'companyId', 'period', 'sectorId', 'entityId', 'reloadToken'].some(key => key in changes)) void this.load();
  }

  get isEmployeeList() { return this.mode === 'employees'; }
  get isEmployeeDetail() { return this.mode === 'employee'; }
  get isPartList() { return this.mode === 'parts'; }
  get isPartDetail() { return this.mode === 'part'; }
  get entityNoun() { return this.isEmployeeList || this.isEmployeeDetail ? 'funcionário' : 'peça'; }
  get title() { return this.isEmployeeList ? 'Visão Geral de Funcionários' : this.isEmployeeDetail ? 'Visão do Funcionário' : this.isPartList ? 'Visão Geral de Peças' : 'Visão da Peça'; }

  get filteredRows() {
    const needle = this.search().trim().toLocaleLowerCase('pt-BR');
    const rows = this.rows().filter(row => !needle || `${row.codigo ?? ''} ${row.nome ?? ''}`.toLocaleLowerCase('pt-BR').includes(needle));
    return rows.sort((a, b) => this.sort() === 'code'
      ? String(a.codigo ?? '').localeCompare(String(b.codigo ?? ''), 'pt-BR', { numeric: true })
      : this.sort() === 'production-asc'
        ? Number(a.quantidadeProduzida ?? 0) - Number(b.quantidadeProduzida ?? 0)
        : Number(b.quantidadeProduzida ?? 0) - Number(a.quantidadeProduzida ?? 0));
  }

  get visibleRows() {
    const start = this.page() * this.pageSize;
    return this.filteredRows.slice(start, start + this.pageSize);
  }

  get totalPages() { return Math.max(1, Math.ceil(this.filteredRows.length / this.pageSize)); }
  get pageEnd() { return Math.min((this.page() + 1) * this.pageSize, this.filteredRows.length); }
  get totalProduction() { return this.isEmployeeDetail ? Number(this.employee()?.quantidadeProduzida ?? 0) : this.isEmployeeList || this.isPartList ? Number(this.reportDashboard?.quantidadeProduzida ?? 0) : Number(this.detailDashboard()?.quantidadeProduzida ?? 0); }
  get recordCount() { return this.isEmployeeDetail ? Number(this.employee()?.quantidadeRegistros ?? 0) : Number(this.detailDashboard()?.porPeca?.[0]?.quantidadeRegistros ?? 0); }
  get distinctParts() { return this.relatedRows().length; }
  get entitiesCount() { return this.rows().length; }
  get averageProduction() { return this.entitiesCount ? this.totalProduction / this.entitiesCount : 0; }
  get leader() { return this.rows()[0] ?? null; }
  get topChartRows() { return [...this.rows()].sort((a, b) => Number(b.quantidadeProduzida ?? 0) - Number(a.quantidadeProduzida ?? 0)).slice(0, this.trendLimit); }
  get detailEntity() { return this.rows().find(row => row.id === this.entityId) ?? null; }
  get participation() { const total = Number(this.scopeDashboard()?.quantidadeProduzida ?? 0); return total > 0 ? this.totalProduction / total * 100 : null; }

  async load() {
    if (!this.companyId || !this.period || !this.mode) return;
    const requestId = ++this.requestId;
    this.state.set('LOADING'); this.loadState.emit('LOADING'); this.error.set(''); this.rows.set([]); this.relatedRows.set([]); this.employee.set(null); this.detailDashboard.set(null); this.scopeDashboard.set(null); this.timeSeries.set([]);
    const filters = { inicio: this.period.inicio, fim: this.period.fim, ...(this.sectorId !== null ? { setorId: this.sectorId } : {}) };
    try {
      if (this.isEmployeeList || this.isPartList) {
        const all = await this.fetchAllGroups(this.isEmployeeList ? 'employees' : 'parts', filters);
        if (requestId !== this.requestId) return;
        this.rows.set(all); this.page.set(0); this.state.set(all.length ? 'SUCCESS' : 'EMPTY'); this.loadState.emit(this.state());
        return;
      }

      const id = this.entityId;
      if (!id) throw new Error('Identificador indisponível.');
      if (this.isEmployeeDetail) {
        const detailParams = { ...filters, funcionarioId: id };
        const [employee, series, denominator, parts] = await Promise.all([
          firstValueFrom(this.api.employee(this.companyId, id, filters)),
          firstValueFrom(this.api.temporalSeries(this.companyId, { ...detailParams, granularidade: 'DIA' })),
          firstValueFrom(this.api.dashboard(this.companyId, filters)),
          this.fetchAllGroups('parts', detailParams)
        ]);
        if (requestId !== this.requestId) return;
        this.employee.set(employee); this.timeSeries.set(series); this.detailDashboard.set(denominator); this.scopeDashboard.set(denominator); this.relatedRows.set(parts);
        this.state.set(employee.quantidadeRegistros ? 'SUCCESS' : 'EMPTY'); this.loadState.emit(this.state());
      } else {
        const partFilters = { ...filters, pecaId: id };
        const [dashboard, series, scope, employees] = await Promise.all([
          firstValueFrom(this.api.dashboard(this.companyId, partFilters)),
          firstValueFrom(this.api.temporalSeries(this.companyId, { ...partFilters, granularidade: 'DIA' })),
          firstValueFrom(this.api.dashboard(this.companyId, filters)),
          this.fetchAllGroups('employees', partFilters)
        ]);
        if (requestId !== this.requestId) return;
        this.detailDashboard.set(dashboard); this.scopeDashboard.set(scope); this.timeSeries.set(series); this.relatedRows.set(employees);
        this.state.set(Number(dashboard.quantidadeProduzida ?? 0) > 0 ? 'SUCCESS' : 'EMPTY'); this.loadState.emit(this.state());
      }
    } catch {
      if (requestId !== this.requestId) return;
      this.error.set(`Não foi possível carregar os dados de ${this.entityNoun}.`); this.state.set('ERROR'); this.loadState.emit('ERROR');
    }
  }

  private async fetchAllGroups(kind: 'employees' | 'parts', filters: Record<string, string | number>) {
    const fetchPage = kind === 'employees' ? this.api.employees.bind(this.api) : this.api.parts.bind(this.api);
    const first = await firstValueFrom(fetchPage(this.companyId, { ...filters, page: 0, size: 100 }));
    const all = [...(first.content ?? [])];
    for (let page = 1; page < Number(first.totalPages ?? 1); page++) {
      const result = await firstValueFrom(fetchPage(this.companyId, { ...filters, page, size: 100 }));
      all.push(...(result.content ?? []));
    }
    return all;
  }

  openRow(row: ProductionGroup) {
    if (row.id == null) return;
    this.navigate.emit({ screen: this.isEmployeeList ? 'employee' : 'part', id: row.id });
  }

  back() {
    if (this.isEmployeeDetail) this.navigate.emit({ screen: 'employees' });
    else if (this.isPartDetail) this.navigate.emit({ screen: 'parts' });
    else this.navigate.emit({ screen: 'relatorios' });
  }

  openReports() { this.navigate.emit({ screen: 'relatorios' }); }
  openList() { this.navigate.emit({ screen: this.isEmployeeDetail ? 'employees' : 'parts' }); }
  setSearch(value: string) { this.search.set(value); this.page.set(0); }
  changeSort(value: string) { if (value === 'production-desc' || value === 'production-asc' || value === 'code') this.sort.set(value); this.page.set(0); }
  previousPage() { this.page.update(value => Math.max(0, value - 1)); }
  nextPage() { this.page.update(value => Math.min(this.totalPages - 1, value + 1)); }
  fmt(value: unknown) { return new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(Number(value ?? 0)); }
  fmtAverage(value: unknown) { return new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2, minimumFractionDigits: 1 }).format(Number(value ?? 0)); }
  share(value: unknown, total = this.totalProduction) { return total > 0 ? Number(value ?? 0) / total * 100 : 0; }
  chartLabel(row: ProductionGroup) { return row.nome || row.codigo || '—'; }
  trackRow(_: number, row: ProductionGroup) { return row.id ?? row.codigo; }
}
