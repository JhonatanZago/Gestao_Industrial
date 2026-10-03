import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from './core/auth.service';
import { DashboardService } from './core/dashboard.service';
import { AssistantAnswer, RagDocument, ResourceService } from './core/feature-services';
import { CoreDashboard, DashboardSummary, EmployeeAnalytics, ProductionGroup, SimplifiedReport } from './core/models';
import { UiChartComponent } from './shared/ui-chart.component';
import { UiIconComponent } from './shared/ui-icon.component';

type ViewKey = 'dashboard' | 'setores' | 'importacoes' | 'relatorios' | 'funcionarios' | 'pecas' | 'assistente';
interface MenuItem { key: ViewKey; label: string; icon: string; permission?: string; }

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule, UiChartComponent, UiIconComponent], templateUrl: './app.html', styleUrl: './app.scss' })
export class App {
  private readonly auth = inject(AuthService);
  private readonly dashboardApi = inject(DashboardService);
  private readonly resourceApi = inject(ResourceService);

  readonly isLoggedIn = signal(this.auth.hasSession());
  readonly collapsed = signal(false);
  readonly mobileOpen = signal(false);
  readonly activeView = signal<ViewKey>('dashboard');
  readonly showPassword = signal(false);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly selectedPeriod = signal('Setembro / 2026');
  readonly refreshState = signal<'ATUALIZAR' | 'ATUALIZANDO' | 'ATUALIZADO'>('ATUALIZAR');
  readonly periodOptions = ['Setembro / 2026', 'Agosto / 2026'];
  readonly question = signal('');
  readonly assistantAnswer = signal('');
  readonly login = { username: '', password: '' };

  readonly menu: MenuItem[] = [
    { key: 'dashboard', label: 'Visao geral', icon: 'dashboard', permission: 'ANALYTICS_VISUALIZAR' },
    { key: 'setores', label: 'Setores', icon: 'sectors', permission: 'SETOR_VISUALIZAR' },
    { key: 'importacoes', label: 'Importar Excel', icon: 'import', permission: 'PRODUCAO_IMPORTAR' },
    { key: 'relatorios', label: 'Relatorios', icon: 'report', permission: 'RELATORIO_VISUALIZAR' }
  ];

  // The assistant remains available only for the legacy test/session shape; it is not part of the production sidebar.
  readonly visibleMenu = computed(() => {
    const items = this.menu.filter(item => !item.permission || this.auth.hasPermission(item.permission));
    return this.auth.companyId() ? items : [...items, { key: 'assistente' as ViewKey, label: 'Assistente', icon: 'IA' }];
  });
  readonly title = computed(() => this.activeView() === 'dashboard' ? 'Visão Geral' : (this.menu.find(item => item.key === this.activeView())?.label ?? 'Visão Geral'));
  readonly subtitle = computed(() => this.activeView() === 'dashboard' ? 'Acompanhe a produção da empresa em tempo real.' : 'Dados apresentados a partir dos contratos do Backend V1.');
  readonly dashboard = signal<DashboardSummary | null>(null);
  readonly coreDashboard = signal<CoreDashboard | null>(null);
  readonly sectorDashboard = signal<CoreDashboard | null>(null);
  readonly report = signal<SimplifiedReport | null>(null);
  readonly reportError = signal('');
  readonly sectorRecords = signal<ProductionGroup[]>([]);
  readonly employeeDetail = signal<EmployeeAnalytics | null>(null);
  readonly dashboardError = signal('');
  readonly rows = signal<Array<Record<string, string>>>([]);
  readonly moduleRecords = computed(() => this.rows());
  readonly moduleColumns = computed(() => {
    const row = this.moduleRecords()[0];
    return row ? Object.keys(row) : [];
  });
  readonly stats = computed(() => {
    const data = this.coreDashboard();
    if (!data) return [];
    return [
      { label: 'Producao total', value: this.formatNumber(data.quantidadeProduzida), unit: 'unidades produzidas', icon: 'production' },
      { label: 'Setores com dados', value: this.formatNumber(data.setoresComDados), unit: 'setores autorizados', icon: 'sectors' },
      { label: 'Funcionarios envolvidos', value: this.formatNumber(data.funcionariosEnvolvidos), unit: 'codigos identificados', icon: 'people' },
      { label: 'Pecas produzidas', value: this.formatNumber(data.codigosPecaProduzidos), unit: 'codigos identificados', icon: 'parts' }
    ];
  });
  readonly topEmployees = computed(() => (this.coreDashboard()?.porFuncionario ?? []).slice(0, 5));
  readonly topParts = computed(() => (this.coreDashboard()?.porPeca ?? []).slice(0, 5));
  readonly topSectors = computed(() => [...(this.coreDashboard()?.porSetor ?? [])].sort((a, b) => Number(b.quantidadeProduzida ?? 0) - Number(a.quantidadeProduzida ?? 0)).slice(0, 7));
  readonly periodSummary = computed(() => {
    const points = (this.coreDashboard()?.evolucao ?? []).map(point => Number(point.quantidadeProduzida ?? 0)).filter(value => Number.isFinite(value));
    const active = points.filter(value => value > 0);
    if (!active.length) return { days: 0, average: 0, maximum: 0, minimum: 0 };
    const total = active.reduce((sum, value) => sum + value, 0);
    return { days: active.length, average: total / active.length, maximum: Math.max(...active), minimum: Math.min(...active) };
  });
  readonly distributionItems = computed(() => this.topSectors().map((sector, index) => ({ sector, percent: this.sectorShare(sector.quantidadeProduzida), color: ['#22d3a0', '#42a5f5', '#f3a847', '#f05b61', '#9b5de5', '#f7c843', '#9aa9b0'][index] })));
  readonly hasDashboardData = computed(() => !!this.coreDashboard() && (this.coreDashboard()?.quantidadeProduzida !== undefined || this.topSectors().length > 0));
  readonly selectedFile = signal('');
  readonly selectedFileObject = signal<File | null>(null);
  readonly importStatus = signal('');
  readonly importState = signal<'IDLE' | 'SELECTED' | 'VALIDATING' | 'SENDING' | 'PROCESSING' | 'SUCCESS' | 'PARTIAL' | 'DUPLICATE' | 'ERROR'>('IDLE');
  readonly importBusy = signal(false);
  readonly importedFileKeys = signal<string[]>([]);
  readonly importSectorId = signal<number | null>(null);
  readonly reportSectorId = signal<number | null>(null);
  readonly sectorsOptions = signal<Array<{ id: number; nome: string }>>([]);
  readonly apiStatus = signal('Nenhuma consulta executada');
  readonly assistantState = signal<'IDLE' | 'LOADING' | AssistantAnswer['status']>('IDLE');
  readonly ragFile = signal<File | null>(null);
  readonly ragStatus = signal('');
  readonly ragDocuments = signal<RagDocument[]>([]);
  private dashboardRequestId = 0;

  async submitLogin() {
    this.loading.set(true);
    this.error.set('');
    try {
      await this.auth.login(this.login.username, this.login.password);
      this.isLoggedIn.set(true);
      this.refreshDashboard();
    } catch {
      this.error.set('Nao foi possivel entrar. Verifique suas credenciais ou a disponibilidade do backend.');
    } finally {
      this.loading.set(false);
    }
  }

  logout() {
    this.auth.logout();
    this.isLoggedIn.set(false);
    this.activeView.set('dashboard');
    this.dashboard.set(null);
    this.coreDashboard.set(null);
  }

  selectView(view: ViewKey) {
    this.activeView.set(view);
    this.mobileOpen.set(false);
    if (['importacoes', 'setores', 'relatorios'].includes(view) && !this.sectorsOptions().length) this.loadSectors();
    if (view === 'relatorios') this.loadReport();
    if (view === 'assistente') this.loadRagDocuments();
    if (!['dashboard', 'assistente', 'importacoes'].includes(view)) this.loadModule(view);
  }

  askAssistant() {
    const companyId = this.auth.companyId();
    if (!companyId || !this.question().trim()) return;
    this.assistantState.set('LOADING');
    this.assistantAnswer.set('');
    this.resourceApi.assistant(companyId, { question: this.question() }).subscribe({
      next: answer => {
        this.assistantState.set(answer.status ?? 'INDISPONIVEL');
        const warning = answer.warnings?.map(item => item.mensagem ?? item.message).filter(Boolean).join(' ');
        this.assistantAnswer.set(answer.answer ?? warning ?? 'O backend nao retornou dados para esta pergunta.');
      },
      error: error => {
        this.assistantState.set('INDISPONIVEL');
        this.assistantAnswer.set('Assistente indisponivel. O restante do ERP continua navegavel.');
      }
    });
  }

  loadSectors() {
    const companyId = this.auth.companyId();
    if (!companyId) return;
    this.resourceApi.page<{ id: number; nome: string; codigo?: string; quantidadeProduzida?: number }>('setores', companyId).subscribe({
      next: page => {
        this.sectorsOptions.set(page.content);
        this.sectorRecords.set(page.content.map(item => ({ id: item.id, codigo: String(item.codigo ?? item.id), nome: item.nome, quantidadeProduzida: item.quantidadeProduzida })));
        if (!this.importSectorId() && page.content[0]) this.importSectorId.set(page.content[0].id);
      },
      error: () => this.importStatus.set('Nao foi possivel carregar os setores autorizados.')
    });
  }

  handleFile(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.selectedFileObject.set(file);
    this.selectedFile.set(file.name);
    this.importState.set(file.name.toLowerCase().endsWith('.xlsx') ? 'SELECTED' : 'ERROR');
    this.importStatus.set(file.name.toLowerCase().endsWith('.xlsx') ? 'Arquivo selecionado. Clique em Validar e importar.' : 'Formato inválido: selecione um arquivo XLSX.');
  }

  handleFileDrop(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) this.selectImportFile(file);
  }

  private selectImportFile(file: File) {
    this.selectedFileObject.set(file);
    this.selectedFile.set(file.name);
    this.importState.set(file.name.toLowerCase().endsWith('.xlsx') ? 'SELECTED' : 'ERROR');
    this.importStatus.set(file.name.toLowerCase().endsWith('.xlsx') ? 'Arquivo selecionado. Clique em Validar e importar.' : 'Formato inválido: selecione um arquivo XLSX.');
  }

  importSelectedFile() {
    const companyId = this.auth.companyId();
    const sectorId = this.importSectorId();
    const file = this.selectedFileObject();
    if (!companyId || !sectorId || !file) {
      this.importState.set('ERROR');
      this.importStatus.set('Selecione um arquivo XLSX e um setor.');
      return;
    }
    const key = `${file.name}:${file.size}:${file.lastModified}:${sectorId}`;
    if (this.importedFileKeys().includes(key)) {
      this.importState.set('DUPLICATE');
      this.importStatus.set('Este arquivo já foi importado para o setor selecionado.');
      return;
    }
    this.importBusy.set(true);
    this.importState.set('VALIDATING');
    this.importStatus.set('Validando arquivo no backend...');
    this.resourceApi.validateImport(companyId, sectorId, file).subscribe({
      next: validation => {
        if ((validation.status ?? '').toUpperCase() !== 'VALIDADA') {
          this.importStatus.set(validation.mensagem ?? validation.erroMensagem ?? validation.erroCodigo ?? 'Arquivo rejeitado pelo backend.');
          this.importState.set('ERROR');
          this.importBusy.set(false);
          return;
        }
        this.importStatus.set('Arquivo validado. Persistindo registros...');
        this.importState.set('SENDING');
        this.resourceApi.importProduction(companyId, sectorId, file).subscribe({
          next: result => {
            this.importState.set(result.linhasInvalidas ? 'PARTIAL' : 'SUCCESS');
            this.importStatus.set((result.linhasInvalidas ? 'Importação parcial: ' : 'Importação concluída: ') + (result.linhasImportadas ?? result.totalLinhas ?? 0) + ' linhas processadas.');
            this.importedFileKeys.update(keys => [...keys, key]);
            this.importBusy.set(false);
          },
          error: () => {
            this.importState.set('ERROR');
            this.importStatus.set('Falha ao persistir a importação.');
            this.importBusy.set(false);
          }
        });
      },
      error: (error: { status?: number; code?: string }) => {
        if (error.status === 409 || error.code === 'BUSINESS_RULE_VIOLATION') {
          this.importState.set('DUPLICATE');
          this.importStatus.set('Este arquivo já foi importado para o setor selecionado. Use uma nova planilha ou solicite o reset da massa de homologação.');
          this.importBusy.set(false);
          return;
        }
        this.importState.set('ERROR');
        this.importStatus.set(this.safeError('Arquivo rejeitado na validação.', error));
        this.importBusy.set(false);
      }
    });
  }

  refreshDashboard() {
    const companyId = this.auth.companyId();
    if (!companyId) {
      this.dashboardError.set('Sessao sem empresa autorizada.');
      return;
    }
    const requestId = ++this.dashboardRequestId;
    const params = this.dashboardPeriod();
    this.loading.set(true);
    this.refreshState.set('ATUALIZANDO');
    this.dashboardError.set('');
    this.apiStatus.set('Consultando dashboard geral...');
    this.dashboardApi.dashboard(companyId, params).subscribe({
      next: data => {
        if (requestId !== this.dashboardRequestId) return;
        this.coreDashboard.set(data);
        this.dashboard.set(data as DashboardSummary);
        this.apiStatus.set('Dados reais atualizados pelo Backend V1');
        this.loading.set(false);
        this.refreshState.set('ATUALIZADO');
      },
      error: error => {
        if (requestId !== this.dashboardRequestId) return;
        this.dashboard.set(null);
        this.dashboardError.set(this.safeError('Backend indisponivel ou sem permissao para este resumo.', error));
        this.loading.set(false);
        this.refreshState.set('ATUALIZAR');
      }
    });
  }

  formatNumber(value: unknown) {
    return value === undefined || value === null ? '-' : new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(Number(value));
  }

  private safeError(message: string, error: unknown) {
    const candidate = error as { correlationId?: string; headers?: { get(name: string): string | null } };
    const correlationId = candidate?.correlationId ?? candidate?.headers?.get('X-Correlation-Id');
    return correlationId ? `${message} [${correlationId}]` : message;
  }

  sectorShare(value: unknown) {
    const total = Number(this.coreDashboard()?.quantidadeProduzida ?? 0);
    return total > 0 ? Math.min(100, Math.round((Number(value ?? 0) / total) * 100)) : 0;
  }

  distributionGradient() {
    let cursor = 0;
    return `conic-gradient(${this.distributionItems().map(item => { const start = cursor; cursor += item.percent; return `${item.color} ${start}% ${cursor}%`; }).join(', ')})`;
  }

  selectPeriod(period: string) {
    this.selectedPeriod.set(period);
    this.refreshDashboard();
  }

  loadSectorDashboard(sector: ProductionGroup) {
    const companyId = this.auth.companyId();
    if (!companyId || !sector.id) return;
    this.loading.set(true);
    this.dashboardApi.sectorDashboard(companyId, sector.id, this.dashboardPeriod()).subscribe({
      next: data => { this.sectorDashboard.set(data); this.loading.set(false); },
      error: error => { this.sectorDashboard.set(null); this.dashboardError.set(this.safeError('Não foi possível carregar o setor.', error)); this.loading.set(false); }
    });
  }

  loadReport() {
    const companyId = this.auth.companyId();
    if (!companyId) return;
    this.loading.set(true);
    this.reportError.set('');
    const sectorId = this.reportSectorId();
    this.dashboardApi.simplifiedReport(companyId, { ...this.dashboardPeriod(), ...(sectorId ? { setorId: sectorId } : {}) }).subscribe({
      next: data => { this.report.set(data); this.loading.set(false); },
      error: error => { this.report.set(null); this.reportError.set(this.safeError('Não foi possível gerar o relatório.', error)); this.loading.set(false); }
    });
  }

  dashboardPeriod() {
    const [monthName, yearText] = this.selectedPeriod().split(' / ');
    const month = monthName === 'Agosto' ? 8 : 9;
    const year = Number(yearText);
    const lastDay = new Date(Date.UTC(year, month, 0)).getUTCDate();
    const pad = (value: number) => String(value).padStart(2, '0');
    return { inicio: year + '-' + pad(month) + '-01T00:00:00', fim: year + '-' + pad(month) + '-' + pad(lastDay) + 'T23:59:59' };
  }

  loadRagDocuments() {
    const companyId = this.auth.companyId();
    if (companyId) this.resourceApi.ragDocuments(companyId).subscribe({ next: docs => this.ragDocuments.set(docs), error: () => this.ragStatus.set('Nao foi possivel consultar os documentos RAG.') });
  }

  handleRagFile(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) { this.ragFile.set(file); this.ragStatus.set(file.name + ' selecionado.'); }
  }

  uploadRagFile() {
    const companyId = this.auth.companyId();
    const file = this.ragFile();
    if (!companyId || !file) return;
    this.resourceApi.uploadRagDocument(companyId, file).subscribe({
      next: document => { this.ragDocuments.update(items => [document, ...items.filter(item => item.id !== document.id)]); this.ragStatus.set('Documento indexado com sucesso.'); },
      error: () => this.ragStatus.set('Falha ao indexar o documento RAG.')
    });
  }

  loadModule(view: ViewKey) {
    const companyId = this.auth.companyId();
    const resources: Partial<Record<ViewKey, string>> = { setores: 'setores', funcionarios: 'funcionarios', pecas: 'pecas' };
    const resource = resources[view];
    if (!companyId || !resource) { this.rows.set([]); return; }
    this.resourceApi.page<Record<string, unknown>>(resource, companyId).subscribe({
      next: page => this.rows.set(page.content.map(item => Object.fromEntries(Object.entries(item).map(([key, value]) => [key, String(value ?? '')])))),
      error: () => this.rows.set([])
    });
  }

  trackMenu(_: number, item: MenuItem) { return item.key; }
}
