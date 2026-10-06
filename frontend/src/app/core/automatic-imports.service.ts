import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Page } from './models';
import { clean, Query } from './feature-services';

export interface AutomaticImportSummary { id: number; arquivo: string; origem: string; resultado: string; iniciadoEm: string | null; finalizadoEm: string | null; linhasLidas: number; linhasImportadas: number; linhasIgnoradas: number; }
export interface MonitoredSource { setorId: number; setor: string; habilitada: boolean; acessivel: boolean; status: string; ultimoCheck: string | null; ultimaImportacao: AutomaticImportSummary | null; }
export interface AutomaticImportStatus { enabled: boolean; monitoramentoAtivo: boolean; status: string; ultimaVerificacao: string | null; ultimaImportacao: AutomaticImportSummary | null; reconciliacao: { enabled: boolean; cron: string | null; timezone: string | null }; fontes: MonitoredSource[]; }
export interface ProductionImport { id: number; empresaId: number; setorId: number; setorCodigo: string | null; setorNome: string | null; nomeArquivo: string; hashArquivo: string | null; origem: string; status: string; totalLinhas: number; linhasValidas: number; linhasInvalidas: number; linhasImportadas: number; linhasIgnoradas: number; iniciadoEm: string | null; finalizadoEm: string | null; createdAt: string | null; correlationId: string | null; erroCodigo: string | null; erroMensagem: string | null; }

@Injectable({ providedIn: 'root' })
export class AutomaticImportsService {
  private readonly api = inject(ApiService);
  status(companyId: number) { return this.api.get<AutomaticImportStatus>(`/empresas/${companyId}/importacoes/automaticas/status`); }
  history(companyId: number, query: Query = {}) { return this.api.get<Page<ProductionImport>>(`/empresas/${companyId}/importacoes/producao`, clean(query)); }
  detail(companyId: number, importId: number) { return this.api.get<ProductionImport>(`/empresas/${companyId}/importacoes/producao/${importId}`); }
}
