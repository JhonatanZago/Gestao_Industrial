import { CommonModule } from '@angular/common';
import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs';
import { AuthService } from './core/auth.service';
import { DashboardService } from './core/dashboard.service';
import { AssistantAnswer, ImportLineResult, ImportResult, RagDocument, reportFilename, ResourceService } from './core/feature-services';
import { CoreDashboard, DashboardSummary, EmployeeAnalytics, ProductionGroup, ReportExportFormat, SimplifiedReport } from './core/models';
import { UiChartComponent } from './shared/ui-chart.component';
import { UiIconComponent } from './shared/ui-icon.component';
import { ReportDrilldownComponent, ReportDrilldownMode, ReportDrilldownNavigation } from './features/reports/report-drilldown.component';
import { AutomaticImportsComponent } from './features/automatic-imports/automatic-imports.component';

type ViewKey = 'dashboard' | 'setores' | 'setor-detalhe' | 'importacoes' | 'importacoes-automaticas' | 'relatorios' | 'relatorios-funcionarios' | 'relatorios-funcionario' | 'relatorios-pecas' | 'relatorios-peca' | 'funcionarios' | 'pecas' | 'assistente';
interface MenuItem { key: ViewKey; label: string; icon: string; permission?: string; }
interface SectorVisual { color: string; soft: string; icon: string; }
interface SectorOption { id: number; nome: string; codigo?: string; descricao?: string | null; ativo?: boolean; }
type ImportState = 'SECTOR' | 'FILE' | 'SELECTED' | 'VALIDATING' | 'VALIDATED' | 'VALIDATION_ERROR' | 'IMPORTING' | 'SUCCESS' | 'DUPLICATE' | 'ERROR';
type ReportState = 'IDLE' | 'LOADING' | 'SUCCESS' | 'ERROR';

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule, UiChartComponent, UiIconComponent, ReportDrilldownComponent, AutomaticImportsComponent], templateUrl: './app.html', styleUrl: './app.scss' })
export class App {
  private readonly auth = inject(AuthService);
  private readonly dashboardApi = inject(DashboardService);
  private readonly resourceApi = inject(ResourceService);

  readonly isLoggedIn = signal(this.auth.hasSession());
  readonly collapsed = signal(false);
  readonly mobileOpen = signal(false);
  readonly activeView = signal<ViewKey>('dashboard');
  readonly reportEntityId = signal<number | null>(null);
  readonly reportReloadToken = signal(0);
  readonly showPassword = signal(false);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly selectedPeriod = signal('Setembro / 2026');
  readonly refreshState = signal<'ATUALIZAR' | 'ATUALIZANDO' | 'ATUALIZADO'>('ATUALIZAR');
  readonly periodOptions = ['Outubro / 2026', 'Setembro / 2026', 'Agosto / 2026'];
  readonly question = signal('');
  readonly assistantAnswer = signal('');
  readonly login = { username: '', password: '' };

  readonly menu: MenuItem[] = [
    { key: 'dashboard', label: 'Visao geral', icon: 'dashboard', permission: 'ANALYTICS_VISUALIZAR' },
    { key: 'setores', label: 'Setores', icon: 'sectors', permission: 'SETOR_VISUALIZAR' },
    { key: 'importacoes', label: 'Importar Excel', icon: 'import', permission: 'PRODUCAO_IMPORTAR' },
    { key: 'importacoes-automaticas', label: 'Importações Automáticas', icon: 'refresh', permission: 'OPERACAO_MONITORAR' },
    { key: 'relatorios', label: 'Relatorios', icon: 'report', permission: 'RELATORIO_VISUALIZAR' }
  ];

  // The assistant remains available only for the legacy test/session shape; it is not part of the production sidebar.
  readonly visibleMenu = computed(() => {
    const items = this.menu.filter(item => !item.permission || this.auth.hasPermission(item.permission));
    return this.auth.companyId() ? items : [...items, { key: 'assistente' as ViewKey, label: 'Assistente', icon: 'IA' }];
  });
  readonly title = computed(() => this.activeView() === 'dashboard' ? 'Visão Geral' : this.activeView() === 'setor-detalhe' ? (this.selectedSector()?.nome || this.selectedSector()?.codigo || 'Detalhes do setor') : this.activeView() === 'relatorios-funcionarios' ? 'Visão Geral de Funcionários' : this.activeView() === 'relatorios-funcionario' ? 'Visão do Funcionário' : this.activeView() === 'relatorios-pecas' ? 'Visão Geral de Peças' : this.activeView() === 'relatorios-peca' ? 'Visão da Peça' : (this.menu.find(item => item.key === this.activeView())?.label ?? 'Visão Geral'));
  readonly subtitle = computed(() => this.activeView() === 'dashboard' ? 'Acompanhe a produção da empresa em tempo real.' : this.activeView() === 'setores' ? 'Acompanhe a produção por área da empresa.' : this.activeView() === 'setor-detalhe' ? 'Dados reais do período selecionado.' : this.activeView() === 'importacoes' ? 'Importe os dados de produção e atualize os dashboards.' : this.activeView() === 'importacoes-automaticas' ? 'Acompanhe a sincronização automática das planilhas de produção.' : this.isReportDrilldown() ? `${this.selectedPeriod()} · ${this.reportScopeLabel()}` : 'Gere e analise relatórios da produção industrial.');
  readonly headerIcon = computed(() => this.activeView().startsWith('relatorios') ? 'reports' : this.activeView() === 'importacoes' ? 'import' : this.activeView() === 'importacoes-automaticas' ? 'refresh' : this.activeView() === 'setores' || this.activeView() === 'setor-detalhe' ? 'sectors' : 'dashboard');
  readonly reportDrilldownMode = computed<ReportDrilldownMode>(() => this.activeView() === 'relatorios-funcionarios' ? 'employees' : this.activeView() === 'relatorios-funcionario' ? 'employee' : this.activeView() === 'relatorios-pecas' ? 'parts' : 'part');
  readonly reportContextPeriod = computed(() => ({ label: this.selectedPeriod(), ...this.dashboardPeriod() }));
  readonly reportCompanyId = computed(() => this.auth.companyId() ?? 0);
  readonly isReportDrilldown = computed(() => this.activeView().startsWith('relatorios-'));
  readonly dashboard = signal<DashboardSummary | null>(null);
  readonly coreDashboard = signal<CoreDashboard | null>(null);
  readonly sectorDashboard = signal<CoreDashboard | null>(null);
  readonly sectorOverviewDashboard = signal<CoreDashboard | null>(null);
  readonly selectedSector = signal<ProductionGroup | null>(null);
  readonly sectorsLoading = signal(false);
  readonly sectorsError = signal('');
  readonly sectorDetailLoading = signal(false);
  readonly sectorDetailError = signal('');
  readonly employeeDetailLoading = signal(false);
  readonly employeeDetailError = signal('');
  readonly report = signal<SimplifiedReport | null>(null);
  readonly reportError = signal('');
  readonly reportState = signal<ReportState>('IDLE');
  readonly reportFeedback = signal('Configure os filtros e gere o relat\u00f3rio.');
  readonly exportingFormat = signal<ReportExportFormat | null>(null);
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
      { label: 'Produção total', value: this.formatNumber(data.quantidadeProduzida), unit: 'unidades produzidas', icon: 'production' },
      { label: 'Setores com dados', value: this.formatNumber(data.setoresComDados), unit: 'setores autorizados', icon: 'sectors' },
      { label: 'Funcionários envolvidos', value: this.formatNumber(data.funcionariosEnvolvidos), unit: 'códigos identificados', icon: 'people' },
      { label: 'Peças produzidas', value: this.formatNumber(data.codigosPecaProduzidos), unit: 'códigos identificados', icon: 'parts' }
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
  readonly distributionItems = computed(() => this.topSectors().map((sector, index) => ({ sector, percent: this.sectorShare(sector.quantidadeProduzida), color: ['#14e2a4', '#2ea8ef', '#ff9848', '#ef5b62', '#a15cf6', '#f5c84c', '#9aa9b0'][index] })));
  readonly hasDashboardData = computed(() => !!this.coreDashboard() && (this.coreDashboard()?.quantidadeProduzida !== undefined || this.topSectors().length > 0));
  readonly sectorOverviewStats = computed(() => {
    const data = this.sectorOverviewDashboard();
    if (!data) return [];
    return [
      { label: 'Setores com produção', value: this.formatNumber(data.setoresComDados), unit: 'no período selecionado', icon: 'sectors', accent: 'blue' },
      { label: 'Produção total', value: this.formatNumber(data.quantidadeProduzida), unit: 'peças produzidas', icon: 'production', accent: 'green' },
      { label: 'Funcionários', value: this.formatNumber(data.funcionariosEnvolvidos), unit: 'envolvidos na produção', icon: 'people', accent: 'orange' }
    ];
  });
  readonly sectorDetailStats = computed(() => {
    const data = this.sectorDashboard();
    if (!data) return [];
    return [
      { label: 'Produção total', value: this.formatNumber(data.quantidadeProduzida), unit: 'peças produzidas', icon: 'production', accent: 'green' },
      { label: 'Funcionários', value: this.formatNumber(data.funcionariosEnvolvidos), unit: 'envolvidos no setor', icon: 'people', accent: 'orange' },
      { label: 'Peças diferentes', value: this.formatNumber(data.codigosPecaProduzidos), unit: 'códigos produzidos', icon: 'parts', accent: 'purple' },
      ...(data.duracaoRegistradaMinutos === undefined || data.duracaoRegistradaMinutos === null ? [] : [{ label: 'Duração registrada', value: this.formatNumber(data.duracaoRegistradaMinutos), unit: 'minutos registrados', icon: 'report', accent: 'blue' }])
    ];
  });
  readonly sectorEmployees = computed(() => (this.sectorDashboard()?.porFuncionario ?? []).slice(0, 5));
  readonly sectorParts = computed(() => (this.sectorDashboard()?.porPeca ?? []).slice(0, 5));
  readonly viewLoading = computed(() => this.activeView() === 'setores' ? this.sectorsLoading() : this.activeView() === 'setor-detalhe' ? this.sectorDetailLoading() : this.activeView() === 'relatorios' || this.isReportDrilldown() ? this.reportLoading() || this.loading() : this.loading());
  readonly selectedFile = signal('');
  readonly selectedFileObject = signal<File | null>(null);
  readonly importStatus = signal('');
  readonly importState = signal<ImportState>('SECTOR');
  readonly importBusy = signal(false);
  readonly importDragActive = signal(false);
  readonly importSectorLoading = signal(false);
  readonly importSectorError = signal('');
  readonly importValidation = signal<ImportResult | null>(null);
  readonly importResult = signal<ImportResult | null>(null);
  readonly importLineErrors = signal<ImportLineResult[]>([]);
  readonly importLinesLoading = signal(false);
  readonly showImportConfirmation = signal(false);
  readonly importSectorId = signal<number | null>(null);
  readonly reportSectorId = signal<number | null>(null);
  readonly sectorsOptions = signal<SectorOption[]>([]);
  readonly selectedImportSector = computed(() => this.sectorsOptions().find(sector => sector.id === this.importSectorId()) ?? null);
  readonly selectedReportSector = computed(() => this.sectorsOptions().find(sector => sector.id === this.reportSectorId()) ?? null);
  readonly reportDashboard = computed(() => this.report()?.dashboard ?? null);
  readonly reportHasData = computed(() => {
    const dashboard = this.reportDashboard();
    return !!dashboard && (Number(dashboard.quantidadeProduzida ?? 0) > 0 || (dashboard.porSetor?.length ?? 0) > 0 || (dashboard.evolucao?.length ?? 0) > 0);
  });
  readonly reportStats = computed(() => {
    const data = this.reportDashboard();
    if (!data) return [];
    return [
      { label: 'Produção total', value: this.formatNumber(data.quantidadeProduzida), unit: 'peças produzidas', icon: 'production' },
      { label: 'Setores', value: this.formatNumber(data.setoresComDados), unit: 'com produção no período', icon: 'sectors' },
      { label: 'Funcionários', value: this.formatNumber(data.funcionariosEnvolvidos), unit: 'envolvidos na produção', icon: 'people' },
      { label: 'Peças', value: this.formatNumber(data.codigosPecaProduzidos), unit: 'códigos produzidos', icon: 'parts' }
    ];
  });
  readonly reportSectors = computed(() => [...(this.reportDashboard()?.porSetor ?? [])].sort((left, right) => Number(right.quantidadeProduzida ?? 0) - Number(left.quantidadeProduzida ?? 0)));
  readonly reportEmployees = computed(() => (this.reportDashboard()?.porFuncionario ?? []).slice(0, 5));
  readonly reportParts = computed(() => (this.reportDashboard()?.porPeca ?? []).slice(0, 5));
  readonly reportScopeLabel = computed(() => this.selectedReportSector()?.nome ?? 'Todos os setores');
  readonly reportCtaLabel = computed(() => this.reportState() === 'LOADING' ? 'Gerando relat\u00f3rio...' : this.reportState() === 'SUCCESS' ? 'Relat\u00f3rio gerado' : this.reportState() === 'ERROR' ? 'Tentar novamente' : 'Gerar relat\u00f3rio');
  readonly reportLoading = computed(() => this.reportState() === 'LOADING');
  readonly importStep = computed(() => {
    const state = this.importState();
    if (state === 'SECTOR') return 1;
    if (state === 'FILE' || state === 'SELECTED' || state === 'ERROR') return 2;
    if (state === 'SUCCESS') return 4;
    return 3;
  });
  readonly apiStatus = signal('Nenhuma consulta executada');
  readonly assistantState = signal<'IDLE' | 'LOADING' | AssistantAnswer['status']>('IDLE');
  readonly ragFile = signal<File | null>(null);
  readonly ragStatus = signal('');
  readonly ragDocuments = signal<RagDocument[]>([]);
  private dashboardRequestId = 0;
  private sectorsRequestId = 0;
  private sectorDetailRequestId = 0;
  private readonly sectorPalette: SectorVisual[] = [
    { color: '#14e2a4', soft: 'rgba(20,226,164,.12)', icon: 'production' },
    { color: '#2ea8ef', soft: 'rgba(46,168,239,.12)', icon: 'sectors' },
    { color: '#ff9848', soft: 'rgba(255,152,72,.12)', icon: 'report' },
    { color: '#a15cf6', soft: 'rgba(161,92,246,.12)', icon: 'parts' },
    { color: '#ef5b82', soft: 'rgba(239,91,130,.12)', icon: 'production' }
  ];

  constructor() {
    if (this.isLoggedIn()) { this.restoreReportRoute(); this.restoreAutomaticRoute(); }
  }

  async submitLogin() {
    this.loading.set(true);
    this.error.set('');
    try {
      await this.auth.login(this.login.username, this.login.password);
      this.isLoggedIn.set(true);
      if (!this.restoreReportRoute()) this.refreshDashboard();
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
    this.reportEntityId.set(null);
    this.activeView.set('dashboard');
  }

  selectView(view: ViewKey) {
    this.activeView.set(view);
    this.mobileOpen.set(false);
    if (view === 'dashboard') this.setViewUrl('/app/dashboard');
    if (view === 'setores') {
      this.selectedSector.set(null);
      this.employeeDetail.set(null);
      this.setViewUrl('/app/setores');
      if (!this.sectorsOptions().length) this.loadSectors();
      this.loadSectorOverview();
      return;
    }
    if (view === 'importacoes') {
      this.setViewUrl('/app/importacoes');
      if (!this.sectorsOptions().length) this.loadSectors();
    }
    if (view === 'importacoes-automaticas') this.setViewUrl('/app/importacoes-automaticas');
    if (view === 'relatorios') {
      this.setViewUrl(this.reportUrl('relatorios'));
      if (!this.sectorsOptions().length) this.loadSectors();
    }
    if (view === 'relatorios') this.loadReport();
    if (view === 'assistente') this.loadRagDocuments();
    if (['funcionarios', 'pecas'].includes(view)) this.loadModule(view);
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
    this.importSectorLoading.set(true);
    this.importSectorError.set('');
    this.resourceApi.page<SectorOption>('setores', companyId, { size: 100, sort: 'nome,asc' }).subscribe({
      next: page => {
        this.sectorsOptions.set(page.content.filter(sector => sector.ativo !== false));
        this.importSectorLoading.set(false);
      },
      error: error => {
        console.error('[Importação] Falha ao carregar setores autorizados.', error);
        this.importSectorError.set('Não foi possível carregar os setores autorizados. Verifique a conexão e tente novamente.');
        this.importSectorLoading.set(false);
      }
    });
  }

  selectImportSector(sectorId: number) {
    if (this.importBusy()) return;
    this.importSectorId.set(sectorId);
    this.importStatus.set('');
  }

  continueImport() {
    if (!this.selectedImportSector()) return;
    this.importState.set('FILE');
    this.importStatus.set('');
  }

  backToImportSector() {
    if (this.importBusy()) return;
    this.removeImportFile();
    this.importState.set('SECTOR');
  }

  handleFile(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.selectImportFile(file);
  }

  handleFileDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.importDragActive.set(false);
    const file = event.dataTransfer?.files?.[0];
    if (file) this.selectImportFile(file);
  }

  handleImportDrag(event: DragEvent, active: boolean) {
    event.preventDefault();
    event.stopPropagation();
    if (!this.importBusy()) this.importDragActive.set(active);
  }

  private selectImportFile(file: File) {
    this.importValidation.set(null);
    this.importResult.set(null);
    this.importLineErrors.set([]);
    this.showImportConfirmation.set(false);
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      this.selectedFileObject.set(null);
      this.selectedFile.set('');
      this.importState.set('ERROR');
      this.importStatus.set('Formato não suportado. Selecione uma planilha Excel no formato .XLSX.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.selectedFileObject.set(null);
      this.selectedFile.set('');
      this.importState.set('ERROR');
      this.importStatus.set('Arquivo acima do limite permitido pelo servidor (10 MB).');
      return;
    }
    this.selectedFileObject.set(file);
    this.selectedFile.set(file.name);
    this.importState.set('SELECTED');
    this.importStatus.set('Pronto para validar');
  }

  removeImportFile() {
    if (this.importBusy()) return;
    this.selectedFileObject.set(null);
    this.selectedFile.set('');
    this.importValidation.set(null);
    this.importResult.set(null);
    this.importLineErrors.set([]);
    this.showImportConfirmation.set(false);
    this.importStatus.set('');
    if (this.importState() !== 'SECTOR') this.importState.set('FILE');
  }

  validateSelectedFile() {
    if (this.importBusy()) return;
    const companyId = this.auth.companyId();
    const sectorId = this.importSectorId();
    const file = this.selectedFileObject();
    if (!companyId || !sectorId || !file) {
      this.importState.set('ERROR');
      this.importStatus.set('Selecione um arquivo XLSX e um setor.');
      return;
    }
    this.importBusy.set(true);
    this.importState.set('VALIDATING');
    this.importStatus.set('Validando planilha no backend...');
    this.importLineErrors.set([]);
    this.resourceApi.validateImport(companyId, sectorId, file).subscribe({
      next: validation => {
        this.importValidation.set(validation);
        this.importBusy.set(false);
        if ((validation.status ?? '').toUpperCase() === 'VALIDADA') {
          this.importState.set('VALIDATED');
          this.importStatus.set('Arquivo válido e pronto para confirmação.');
          return;
        }
        this.importState.set('VALIDATION_ERROR');
        this.importStatus.set(validation.erroMensagem ?? validation.erroCodigo ?? 'A planilha contém problemas de validação.');
        if (validation.id) this.loadImportLineErrors(companyId, sectorId, validation.id);
      },
      error: error => this.handleImportApiError(error, 'Não foi possível validar a planilha.')
    });
  }

  importSelectedFile() { this.validateSelectedFile(); }

  private loadImportLineErrors(companyId: number, sectorId: number, importId: number) {
    this.importLinesLoading.set(true);
    this.resourceApi.importLines(companyId, sectorId, importId, { size: 100, sort: 'id,asc' }).subscribe({
      next: page => { this.importLineErrors.set(page.content); this.importLinesLoading.set(false); },
      error: error => { console.error('[Importação] Falha ao consultar erros por linha.', error); this.importLinesLoading.set(false); }
    });
  }

  openImportConfirmation() {
    if (this.importState() === 'VALIDATED' && !this.importBusy()) this.showImportConfirmation.set(true);
  }

  closeImportConfirmation() { if (!this.importBusy()) this.showImportConfirmation.set(false); }

  confirmImport() {
    if (this.importBusy() || !this.showImportConfirmation()) return;
    const companyId = this.auth.companyId();
    const sectorId = this.importSectorId();
    const file = this.selectedFileObject();
    if (!companyId || !sectorId || !file) return;
    this.showImportConfirmation.set(false);
    this.importBusy.set(true);
    this.importState.set('IMPORTING');
    this.importStatus.set('Importando dados...');
    this.resourceApi.importProduction(companyId, sectorId, file).subscribe({
      next: result => {
        this.importResult.set(result);
        this.importBusy.set(false);
        if ((result.status ?? '').toUpperCase() !== 'CONCLUIDA') {
          this.importState.set('ERROR');
          this.importStatus.set(result.erroMensagem ?? result.erroCodigo ?? 'A importação não foi concluída.');
          return;
        }
        this.importState.set('SUCCESS');
        this.importStatus.set('Os dados foram processados com sucesso.');
        this.coreDashboard.set(null);
        this.sectorOverviewDashboard.set(null);
        this.sectorDashboard.set(null);
      },
      error: error => this.handleImportApiError(error, 'Não foi possível importar os dados.')
    });
  }

  private handleImportApiError(error: unknown, fallback: string) {
    const apiError = error as { status?: number; code?: string; correlationId?: string; backendMessage?: string; headers?: { get(name: string): string | null } };
    this.importBusy.set(false);
    this.showImportConfirmation.set(false);
    if (apiError.status === 409 || apiError.backendMessage === 'ARQUIVO_DUPLICADO') {
      this.importState.set('DUPLICATE');
      this.importStatus.set('Esta planilha já foi processada anteriormente para o setor selecionado.');
      return;
    }
    const messages: Record<number, string> = {
      400: 'A requisição de importação é inválida. Revise o arquivo e tente novamente.',
      401: 'Sua sessão expirou. Entre novamente para continuar.',
      403: 'Seu usuário não possui permissão para importar neste setor.',
      404: 'O setor ou recurso solicitado não foi encontrado.',
      413: 'O arquivo excede o limite de 10 MB permitido pelo servidor.',
      422: 'O arquivo foi rejeitado pelas regras de validação.',
      500: 'O servidor não conseguiu processar a importação. Nenhum dado parcial foi mantido.'
    };
    this.importState.set('ERROR');
    const message = messages[apiError.status ?? 0] ?? fallback;
    const correlationId = apiError.correlationId ?? apiError.headers?.get('X-Correlation-Id') ?? undefined;
    this.importStatus.set(correlationId ? `${message} [${correlationId}]` : message);
  }

  resetImport() {
    if (this.importBusy()) return;
    this.importSectorId.set(null);
    this.selectedFileObject.set(null);
    this.selectedFile.set('');
    this.importValidation.set(null);
    this.importResult.set(null);
    this.importLineErrors.set([]);
    this.importStatus.set('');
    this.showImportConfirmation.set(false);
    this.importState.set('SECTOR');
  }

  goToImportedSector() {
    const sector = this.selectedImportSector();
    if (sector) this.loadSectorDashboard(sector);
  }

  goToDashboardAfterImport() {
    this.selectView('dashboard');
    this.refreshDashboard();
  }

  importSectorVisual(sector: SectorOption): SectorVisual {
    const index = Math.max(0, this.sectorsOptions().findIndex(item => item.id === sector.id));
    return this.sectorPalette[index % this.sectorPalette.length];
  }

  formatFileSize(bytes: number) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} KB`;
    return `${(bytes / 1024 / 1024).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} MB`;
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

  loadSectorOverview() {
    const companyId = this.auth.companyId();
    if (!companyId || this.sectorsLoading()) return;
    const requestId = ++this.sectorsRequestId;
    const params = { ...this.dashboardPeriod(), size: 100 };
    this.sectorsLoading.set(true);
    this.sectorsError.set('');
    this.refreshState.set('ATUALIZANDO');
    forkJoin({
      summary: this.dashboardApi.dashboard(companyId, this.dashboardPeriod()),
      sectors: this.dashboardApi.sectors(companyId, params)
    }).subscribe({
      next: ({ summary, sectors }) => {
        if (requestId !== this.sectorsRequestId) return;
        const collator = new Intl.Collator('pt-BR', { sensitivity: 'base' });
        this.sectorOverviewDashboard.set(summary);
        this.sectorRecords.set([...sectors.content].sort((left, right) => collator.compare(left.nome ?? left.codigo ?? '', right.nome ?? right.codigo ?? '')));
        this.sectorsLoading.set(false);
        this.refreshState.set('ATUALIZADO');
      },
      error: error => {
        if (requestId !== this.sectorsRequestId) return;
        console.error('[Setores] Falha ao carregar visão analítica.', error);
        this.sectorOverviewDashboard.set(null);
        this.sectorRecords.set([]);
        this.sectorsError.set('Não foi possível carregar os setores. Verifique a conexão com o servidor e tente novamente.');
        this.sectorsLoading.set(false);
        this.refreshState.set('ATUALIZAR');
      }
    });
  }

  rankingShare(value: unknown, rows: Array<{ quantidadeProduzida?: number }>) {
    const maximum = Math.max(...rows.map(row => Number(row.quantidadeProduzida ?? 0)), 1);
    return Math.min(66, Math.round((Number(value ?? 0) / maximum) * 66));
  }

  distributionGradient() {
    let cursor = 0;
    return `conic-gradient(${this.distributionItems().map(item => { const start = cursor; cursor += item.percent; return `${item.color} ${start}% ${cursor}%`; }).join(', ')})`;
  }

  selectPeriod(period: string) {
    this.selectedPeriod.set(period);
    if (this.isReportDrilldown()) { this.replaceReportUrl(); this.loadReport(); return; }
    this.refreshCurrentView();
  }

  openReportDrilldown(screen: 'employees' | 'parts') {
    this.navigateReport({ screen });
  }

  navigateReport(destination: ReportDrilldownNavigation) {
    if (!this.auth.hasPermission('RELATORIO_VISUALIZAR')) return;
    const views: Record<ReportDrilldownNavigation['screen'], ViewKey> = {
      relatorios: 'relatorios', employees: 'relatorios-funcionarios', employee: 'relatorios-funcionario',
      parts: 'relatorios-pecas', part: 'relatorios-peca'
    };
    this.activeView.set(views[destination.screen]);
    this.reportEntityId.set(destination.id ?? null);
    this.mobileOpen.set(false);
    this.setViewUrl(this.reportUrl(destination.screen, destination.id));
    if (destination.screen === 'relatorios' && !this.report()) this.loadReport();
    if (destination.screen !== 'relatorios' && !this.sectorsOptions().length) this.loadSectors();
  }

  onReportDrilldownLoadState(state: 'LOADING' | 'SUCCESS' | 'EMPTY' | 'ERROR') {
    this.refreshState.set(state === 'LOADING' ? 'ATUALIZANDO' : state === 'ERROR' ? 'ATUALIZAR' : 'ATUALIZADO');
  }

  loadSectorDashboard(sector: ProductionGroup) {
    if (!sector.id) return;
    this.selectedSector.set(sector);
    this.employeeDetail.set(null);
    this.employeeDetailError.set('');
    this.activeView.set('setor-detalhe');
    this.mobileOpen.set(false);
    this.setViewUrl(`/app/setores/${sector.id}`);
    this.fetchSectorDashboard(sector.id);
  }

  private fetchSectorDashboard(sectorId: number) {
    const companyId = this.auth.companyId();
    if (!companyId || this.sectorDetailLoading()) return;
    const requestId = ++this.sectorDetailRequestId;
    this.sectorDetailLoading.set(true);
    this.sectorDetailError.set('');
    this.refreshState.set('ATUALIZANDO');
    this.dashboardApi.sectorDashboard(companyId, sectorId, this.dashboardPeriod()).subscribe({
      next: data => {
        if (requestId !== this.sectorDetailRequestId) return;
        this.sectorDashboard.set(data);
        this.sectorDetailLoading.set(false);
        this.refreshState.set('ATUALIZADO');
      },
      error: error => {
        if (requestId !== this.sectorDetailRequestId) return;
        console.error(`[Setores] Falha ao carregar detalhe do setor ${sectorId}.`, error);
        this.sectorDashboard.set(null);
        this.sectorDetailError.set('Não foi possível carregar os dados deste setor. Tente novamente.');
        this.sectorDetailLoading.set(false);
        this.refreshState.set('ATUALIZAR');
      }
    });
  }

  refreshCurrentView() {
    if (this.viewLoading()) return;
    if (this.activeView() === 'setores') { this.loadSectorOverview(); return; }
    if (this.activeView() === 'setor-detalhe') {
      const sectorId = this.selectedSector()?.id;
      if (sectorId) this.fetchSectorDashboard(sectorId);
      return;
    }
    if (this.activeView() === 'relatorios') { this.loadReport(); return; }
    if (this.isReportDrilldown()) { this.reportReloadToken.update(value => value + 1); this.loadReport(); return; }
    this.refreshDashboard();
  }

  backToSectors() {
    this.activeView.set('setores');
    this.selectedSector.set(null);
    this.sectorDashboard.set(null);
    this.employeeDetail.set(null);
    this.setViewUrl('/app/setores');
    if (!this.sectorRecords().length && !this.sectorsLoading()) this.loadSectorOverview();
  }

  loadEmployeeDetail(employee: ProductionGroup) {
    const companyId = this.auth.companyId();
    const sectorId = this.selectedSector()?.id;
    if (!companyId || !sectorId || !employee.id || this.employeeDetailLoading()) return;
    this.employeeDetailLoading.set(true);
    this.employeeDetailError.set('');
    this.dashboardApi.employee(companyId, employee.id, this.dashboardPeriod()).subscribe({
      next: detail => {
        this.employeeDetail.set(detail);
        this.employeeDetailLoading.set(false);
        this.setViewUrl(`/app/setores/${sectorId}/funcionarios/${encodeURIComponent(employee.codigo ?? String(employee.id))}`);
      },
      error: error => {
        console.error(`[Setores] Falha ao carregar funcionário ${employee.id}.`, error);
        this.employeeDetail.set(null);
        this.employeeDetailError.set('Não foi possível carregar os detalhes deste funcionário.');
        this.employeeDetailLoading.set(false);
      }
    });
  }

  closeEmployeeDetail() {
    this.employeeDetail.set(null);
    this.employeeDetailError.set('');
    const sectorId = this.selectedSector()?.id;
    if (sectorId) this.setViewUrl(`/app/setores/${sectorId}`);
  }

  sectorVisual(sector: ProductionGroup): SectorVisual {
    const index = this.sectorRecords().findIndex(item => item.id === sector.id);
    const fallbackIndex = Math.abs(Number(sector.id ?? 0));
    return this.sectorPalette[(index >= 0 ? index : fallbackIndex) % this.sectorPalette.length];
  }

  sectorOverviewShare(value: unknown) {
    const total = Number(this.sectorOverviewDashboard()?.quantidadeProduzida ?? 0);
    return total > 0 ? Math.min(100, Math.round((Number(value ?? 0) / total) * 100)) : 0;
  }

  detailRankingShare(value: unknown, rows: Array<{ quantidadeProduzida?: number }>) {
    const maximum = Math.max(...rows.map(row => Number(row.quantidadeProduzida ?? 0)), 1);
    return Math.min(100, Math.round((Number(value ?? 0) / maximum) * 100));
  }

  private setViewUrl(path: string) {
    if (typeof history !== 'undefined' && `${location.pathname}${location.search}` !== path) history.pushState({}, '', path);
  }

  private reportUrl(screen: ReportDrilldownNavigation['screen'] | 'relatorios', id?: number) {
    const suffix = screen === 'relatorios' ? '' : screen === 'employees' ? '/funcionarios' : screen === 'employee' ? `/funcionarios/${id ?? ''}` : screen === 'parts' ? '/pecas' : `/pecas/${id ?? ''}`;
    const params = new URLSearchParams({ period: this.selectedPeriod() });
    if (this.reportSectorId() !== null) params.set('setorId', String(this.reportSectorId()));
    return `/app/relatorios${suffix}?${params.toString()}`;
  }

  private replaceReportUrl() {
    if (typeof history !== 'undefined' && this.isReportDrilldown()) history.replaceState({}, '', this.reportUrl(this.routeReportScreen(), this.reportEntityId() ?? undefined));
  }

  private routeReportScreen(): ReportDrilldownNavigation['screen'] {
    const view = this.activeView();
    return view === 'relatorios-funcionarios' ? 'employees' : view === 'relatorios-funcionario' ? 'employee' : view === 'relatorios-pecas' ? 'parts' : view === 'relatorios-peca' ? 'part' : 'relatorios';
  }

  private restoreReportRoute() {
    const match = location.pathname.match(/^\/app\/relatorios(?:\/(funcionarios|pecas)(?:\/(\d+))?)?\/?$/);
    if (!match) return false;
    if (!this.auth.hasPermission('RELATORIO_VISUALIZAR')) { this.activeView.set('dashboard'); this.setViewUrl('/app/dashboard'); return true; }
    const params = new URLSearchParams(location.search);
    const period = params.get('period');
    if (period && this.periodOptions.includes(period)) this.selectedPeriod.set(period);
    const sectorId = Number(params.get('setorId'));
    this.reportSectorId.set(Number.isInteger(sectorId) && sectorId > 0 ? sectorId : null);
    this.reportEntityId.set(match[2] ? Number(match[2]) : null);
    const section = match[1];
    const view: ViewKey = section === 'funcionarios' ? (match[2] ? 'relatorios-funcionario' : 'relatorios-funcionarios') : section === 'pecas' ? (match[2] ? 'relatorios-peca' : 'relatorios-pecas') : 'relatorios';
    this.activeView.set(view);
    if (view === 'relatorios') this.loadReport();
    else { this.loadReport(); if (!this.sectorsOptions().length) this.loadSectors(); }
    return true;
  }

  @HostListener('window:popstate')
  onPopState() {
    if (this.restoreReportRoute()) return;
    if (this.restoreAutomaticRoute()) return;
    const detailMatch = location.pathname.match(/^\/app\/setores\/(\d+)/);
    if (detailMatch) {
      const sectorId = Number(detailMatch[1]);
      const sector = this.sectorRecords().find(item => item.id === sectorId) ?? { id: sectorId, nome: `Setor ${sectorId}` };
      this.selectedSector.set(sector);
      this.activeView.set('setor-detalhe');
      this.fetchSectorDashboard(sectorId);
      return;
    }
    if (location.pathname === '/app/setores') {
      this.activeView.set('setores');
      this.selectedSector.set(null);
      if (!this.sectorRecords().length) this.loadSectorOverview();
    }
  }

  private restoreAutomaticRoute() {
    if (location.pathname !== '/app/importacoes-automaticas') return false;
    if (!this.auth.hasPermission('OPERACAO_MONITORAR')) { this.activeView.set('dashboard'); this.setViewUrl('/app/dashboard'); return true; }
    this.activeView.set('importacoes-automaticas');
    return true;
  }

  loadReport() {
    const companyId = this.auth.companyId();
    if (!companyId || this.reportLoading()) return;
    this.loading.set(true);
    this.report.set(null);
    this.reportError.set('');
    this.reportState.set('LOADING');
    this.reportFeedback.set('Consultando dados reais de produ\u00e7\u00e3o...');
    const sectorId = this.reportSectorId();
    this.dashboardApi.simplifiedReport(companyId, { ...this.dashboardPeriod(), ...(sectorId ? { setorId: sectorId } : {}) }).pipe(finalize(() => {
      if (this.reportState() === 'LOADING') {
        this.reportState.set('ERROR');
        this.reportFeedback.set('N\u00e3o foi poss\u00edvel gerar o relat\u00f3rio.');
        this.refreshState.set('ATUALIZAR');
      }
    })).subscribe({
      next: data => { this.report.set(data); this.loading.set(false); this.reportState.set('SUCCESS'); this.reportFeedback.set('Relat\u00f3rio atualizado com dados do per\u00edodo selecionado.'); this.refreshState.set('ATUALIZADO'); },
      error: error => { this.report.set(null); this.reportError.set(this.safeError('Não foi possível gerar o relatório.', error)); this.loading.set(false); }
    });
  }

  changeReportSector(value: string) {
    this.reportSectorId.set(value ? Number(value) : null);
    this.report.set(null);
    this.reportError.set('');
    this.reportState.set('IDLE');
    this.reportFeedback.set('Filtro atualizado. Gere o relat\u00f3rio para consultar os dados.');
  }

  reportShare(value: unknown) {
    const total = Number(this.reportDashboard()?.quantidadeProduzida ?? 0);
    return total > 0 ? Math.min(100, Math.round(Number(value ?? 0) / total * 100)) : 0;
  }

  reportBar(value: unknown) {
    const maximum = Math.max(...this.reportSectors().map(item => Number(item.quantidadeProduzida ?? 0)), 1);
    return Math.min(100, Math.round(Number(value ?? 0) / maximum * 100));
  }

  reportRankingShare(value: unknown, rows: ProductionGroup[]) {
    const maximum = Math.max(...rows.map(item => Number(item.quantidadeProduzida ?? 0)), 1);
    return Math.min(100, Math.round(Number(value ?? 0) / maximum * 100));
  }

  exportReport(format: ReportExportFormat) {
    const companyId = this.auth.companyId();
    if (!companyId || this.exportingFormat() || !this.reportHasData()) return;
    const query = { ...this.dashboardPeriod(), setorId: this.reportSectorId() ?? undefined, top: 100 };
    this.exportingFormat.set(format);
    this.reportFeedback.set(`Preparando arquivo ${format === 'XLSX' ? 'Excel' : 'CSV'}...`);
    this.resourceApi.exportReport(companyId, format, query).subscribe({
      next: response => {
        const contentType = response.headers.get('Content-Type') ?? '';
        const validType = format === 'CSV' ? contentType.toLowerCase().includes('text/csv') : contentType.toLowerCase().includes('spreadsheetml.sheet');
        if (!validType) {
          this.reportError.set(`O servidor retornou um formato inesperado para ${format === 'CSV' ? 'CSV' : 'Excel'}.`);
          this.reportFeedback.set('Falha ao validar o arquivo exportado.');
          this.exportingFormat.set(null);
          return;
        }
        const period = this.dashboardPeriod();
        const fallback = `relatorio-executivo-${period.inicio.slice(0, 10)}-a-${period.fim.slice(0, 10)}.${format.toLowerCase()}`;
        const filename = reportFilename(response.headers.get('Content-Disposition'), fallback);
        const url = URL.createObjectURL(response.body ?? new Blob());
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = filename;
        anchor.click();
        URL.revokeObjectURL(url);
        this.reportError.set('');
        this.reportFeedback.set(`${format === 'XLSX' ? 'Excel' : 'CSV'} exportado: ${filename}`);
        this.exportingFormat.set(null);
      },
      error: error => {
        this.reportError.set(this.safeError('Não foi possível exportar o relatório.', error));
        this.reportFeedback.set('Falha ao baixar o arquivo. Tente novamente.');
        this.exportingFormat.set(null);
      }
    });
  }

  dashboardPeriod() {
    const [monthName, yearText] = this.selectedPeriod().split(' / ');
    const months: Record<string, number> = { Janeiro: 1, Fevereiro: 2, Março: 3, Abril: 4, Maio: 5, Junho: 6, Julho: 7, Agosto: 8, Setembro: 9, Outubro: 10, Novembro: 11, Dezembro: 12 };
    const month = months[monthName] ?? 9;
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
