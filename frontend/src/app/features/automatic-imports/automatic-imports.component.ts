import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { AutomaticImportsService, AutomaticImportStatus, MonitoredSource, ProductionImport } from '../../core/automatic-imports.service';
import { UiIconComponent } from '../../shared/ui-icon.component';

@Component({ selector: 'app-automatic-imports', standalone: true, imports: [CommonModule, UiIconComponent], templateUrl: './automatic-imports.component.html', styleUrl: './automatic-imports.component.scss' })
export class AutomaticImportsComponent {
  private readonly auth = inject(AuthService); private readonly service = inject(AutomaticImportsService);
  readonly status = signal<AutomaticImportStatus | null>(null); readonly history = signal<ProductionImport[]>([]); readonly totalElements = signal(0); readonly totalPages = signal(0); readonly page = signal(0); readonly pageSize = 20; readonly loading = signal(true); readonly historyLoading = signal(true); readonly error = signal(''); readonly statusError = signal(''); readonly historyError = signal(''); readonly detail = signal<ProductionImport | null>(null); readonly detailLoading = signal(false);
  readonly sectorId = signal(''); readonly statusFilter = signal(''); readonly fileFilter = signal(''); readonly startDate = signal(''); readonly endDate = signal('');
  constructor() { this.reload(); }
  reload() { const companyId = this.auth.companyId(); if (!companyId) { this.statusError.set('Não foi possível consultar o monitoramento.'); this.historyError.set('Não foi possível consultar o histórico de importações.'); this.loading.set(false); this.historyLoading.set(false); return; } this.loading.set(true); this.historyLoading.set(true); this.error.set(''); this.statusError.set(''); this.historyError.set(''); this.service.status(companyId).pipe(finalize(() => this.loading.set(false))).subscribe({ next: response => this.status.set(response), error: () => this.statusError.set('Não foi possível consultar o monitoramento.') }); this.service.history(companyId, this.query()).pipe(finalize(() => this.historyLoading.set(false))).subscribe({ next: response => this.setHistory(response), error: () => this.historyError.set('Não foi possível consultar o histórico de importações.') }); }
  applyFilters() { this.page.set(0); this.loadHistoryOnly(); }
  goToPage(next: number) { if (next < 0 || next >= this.totalPages()) return; this.page.set(next); this.loadHistoryOnly(); }
  openDetail(item: ProductionImport) { const companyId = this.auth.companyId(); if (!companyId) return; this.detailLoading.set(true); this.detail.set(null); this.service.detail(companyId, item.id).pipe(finalize(() => this.detailLoading.set(false))).subscribe({ next: value => this.detail.set(value), error: () => this.detail.set(item) }); }
  closeDetail() { this.detail.set(null); }
  sourceStatus(source: MonitoredSource) { return source.status; }
  sourceLabel(status: string) { return ({ ONLINE: 'Online', PROCESSANDO: 'Processando', ERRO: 'Erro', INDISPONIVEL: 'Indisponível', INATIVA: 'Inativa' } as Record<string, string>)[status] ?? status; }
  globalLabel(status: string) { return ({ ATIVO: 'Monitoramento ativo', DESATIVADO: 'Monitoramento desativado', ATENCAO: 'Monitoramento em atenção' } as Record<string, string>)[status] ?? status; }
  originLabel(origin: string) { return ({ MANUAL: 'Manual', FILE_WATCHER: 'Automática', RECONCILIACAO: 'Reconciliação' } as Record<string, string>)[origin] ?? origin; }
  importStatusLabel(status: string) { return ({ CONCLUIDA: 'Sucesso', REJEITADA: 'Rejeitada', FALHOU: 'Erro', PROCESSANDO: 'Processando', IMPORTANDO: 'Processando', VALIDANDO: 'Validando', RECEBIDA: 'Recebida', VALIDADA: 'Validada' } as Record<string, string>)[status] ?? status; }
  date(value: string | null | undefined) { return value ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)) : '—'; }
  shortHash(value: string | null | undefined) { return value ? `${value.slice(0, 10)}…` : '—'; }
  pageStart() { return this.totalElements() ? this.page() * this.pageSize + 1 : 0; }
  pageEnd() { return Math.min((this.page() + 1) * this.pageSize, this.totalElements()); }
  private setHistory(response: { content: ProductionImport[]; totalElements: number; totalPages: number }) { this.history.set(response.content); this.totalElements.set(response.totalElements); this.totalPages.set(response.totalPages); }
  private loadHistoryOnly() { const companyId = this.auth.companyId(); if (!companyId) return; this.historyLoading.set(true); this.historyError.set(''); this.service.history(companyId, this.query()).pipe(finalize(() => this.historyLoading.set(false))).subscribe({ next: response => this.setHistory(response), error: () => this.historyError.set('Não foi possível consultar o histórico de importações.') }); }
  private query(): Record<string, string | number | undefined> { const query: Record<string, string | number | undefined> = { page: this.page(), size: this.pageSize, setorId: this.sectorId() ? Number(this.sectorId()) : undefined, status: this.statusFilter() || undefined, nomeArquivo: this.fileFilter() || undefined }; if (this.startDate()) query['dataInicio'] = `${this.startDate()}T00:00:00-03:00`; if (this.endDate()) query['dataFim'] = `${this.endDate()}T23:59:59-03:00`; return query; }
}
