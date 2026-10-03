import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Page } from './models';
import { environment } from '../../environments/environment';
import { ReportExportFormat } from './models';

export type Query = Record<string, string | number | boolean | undefined>;
export interface AssistantAnswer { answer?: string; status?: 'RESPONDIDO' | 'SEM_DADOS' | 'SEM_PERMISSAO' | 'PRECISA_CLARIFICACAO' | 'INDISPONIVEL'; period?: unknown; warnings?: Array<{ mensagem?: string; message?: string }>; sources?: Array<{ titulo?: string; escopo?: string }>; }
export interface AuditEvent { id?: number; ator?: string; dataHora?: string; acao?: string; tipoRecurso?: string; recursoId?: number; empresaId?: number; antes?: unknown; depois?: unknown; }
export interface ImportResult {
  id?: number;
  empresaId?: number;
  setorId?: number;
  setorCodigo?: string;
  setorNome?: string;
  nomeArquivo?: string;
  hashArquivo?: string;
  status?: string;
  totalLinhas?: number;
  linhasValidas?: number;
  linhasInvalidas?: number;
  linhasImportadas?: number;
  iniciadoEm?: string;
  finalizadoEm?: string;
  createdAt?: string;
  correlationId?: string;
  erroCodigo?: string;
  erroMensagem?: string;
}
export interface ImportLineResult {
  id?: number;
  numeroLinha: number;
  status?: string;
  codigoFuncionario?: string;
  codigoPeca?: string;
  codigoErro?: string;
  mensagem?: string;
}

@Injectable({ providedIn: 'root' })
export class ResourceService {
  private readonly api = inject(ApiService);
  private readonly http = inject(HttpClient);
  page<T>(resource: string, companyId: number, query: Query = {}) { return this.api.get<Page<T>>(`/empresas/${companyId}/${resource}`, clean(query)); }
  get<T>(resource: string, companyId: number, id: number) { return this.api.get<T>(`/empresas/${companyId}/${resource}/${id}`); }
  create<T>(resource: string, companyId: number, body: unknown) { return this.api.post<T>(`/empresas/${companyId}/${resource}`, body); }
  update<T>(resource: string, companyId: number, id: number, body: unknown) { return this.api.put<T>(`/empresas/${companyId}/${resource}/${id}`, body); }
  setActive<T>(resource: string, companyId: number, id: number, active: boolean) { return this.api.patch<T>(`/empresas/${companyId}/${resource}/${id}/${active ? 'ativar' : 'desativar'}`); }
  analytics<T>(companyId: number, path: string, query: Query = {}) { return this.api.get<T>(`/empresas/${companyId}/analytics/${path}`, clean(query)); }
  assistant(companyId: number, body: { question: string; conversationId?: string }) { return this.api.post<AssistantAnswer>(`/empresas/${companyId}/assistente/perguntar`, body); }
  audit(companyId: number, query: Query = {}) { return this.api.get<Page<AuditEvent>>(`/empresas/${companyId}/auditoria`, clean(query)); }
  auditDetail(companyId: number, type: string, id: number) { return this.api.get<AuditEvent[]>(`/empresas/${companyId}/auditoria/${type}/${id}`); }
  report<T>(companyId: number, name: 'producao' | 'paradas' | 'desempenho', query: Query) { return this.api.get<T>(`/empresas/${companyId}/relatorios/${name}`, clean(query)); }
  exportReport(companyId: number, format: ReportExportFormat, query: Query) {
    const params = new HttpParams({ fromObject: clean({ ...query, formato: format }) as Record<string, string> });
    return this.http.get(environment.apiBaseUrl + `/empresas/${companyId}/relatorios/executivo/exportar`, {
      params,
      observe: 'response',
      responseType: 'blob'
    });
  }
  validateImport(companyId: number, sectorId: number, file: File) { return this.upload<ImportResult>(`/empresas/${companyId}/setores/${sectorId}/importacoes/producao/validar`, file); }
  importProduction(companyId: number, sectorId: number, file: File) { return this.upload<ImportResult>(`/empresas/${companyId}/setores/${sectorId}/importacoes/producao`, file); }
  imports(companyId: number, sectorId: number, query: Query = {}) { return this.api.get<Page<ImportResult>>(`/empresas/${companyId}/setores/${sectorId}/importacoes/producao`, clean(query)); }
  importLines(companyId: number, sectorId: number, importId: number, query: Query = {}) { return this.api.get<Page<ImportLineResult>>(`/empresas/${companyId}/setores/${sectorId}/importacoes/producao/${importId}/linhas`, clean(query)); }
  uploadRagDocument(companyId: number, file: File, sectorId?: number) { const form = new FormData(); form.append('file', file); const suffix = sectorId ? `?setorId=${sectorId}` : ''; return this.api.post<RagDocument>(`/empresas/${companyId}/assistente/documentos${suffix}`, form); }
  ragDocuments(companyId: number) { return this.api.get<RagDocument[]>(`/empresas/${companyId}/assistente/documentos`); }
  importDetail(companyId: number, sectorId: number, importId: number) { return this.api.get<ImportResult>(`/empresas/${companyId}/setores/${sectorId}/importacoes/producao/${importId}`); }
  qualitySummary(companyId: number, query: Query = {}) { return this.api.get<unknown>(`/empresas/${companyId}/qualidade-dados/resumo`, clean(query)); }
  qualityProblems(companyId: number, query: Query = {}) { return this.api.get<Page<Record<string, unknown>>>(`/empresas/${companyId}/qualidade-dados/problemas`, clean(query)); }
  qualityDuplicates(companyId: number, query: Query = {}) { return this.api.get<Page<Record<string, unknown>>>(`/empresas/${companyId}/qualidade-dados/duplicidades`, clean(query)); }
  ragUpload(companyId: number, file: File, sectorId?: number) { const form = new FormData(); form.append('file', file); if (sectorId !== undefined) form.append('setorId', String(sectorId)); return this.api.post<RagDocument>(`/empresas/${companyId}/assistente/documentos`, form); }
  ragRemove(companyId: number, documentId: number) { return this.api.delete<void>(`/empresas/${companyId}/assistente/documentos/${documentId}`); }
  private upload<T>(path: string, file: File): Observable<T> { const form = new FormData(); form.append('file', file); return this.api.post<T>(path, form); }
}

export function clean(query: Query): Record<string, string> { return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== '').map(([key, value]) => [key, String(value)])); }

export function reportFilename(contentDisposition: string | null, fallback: string): string {
  if (!contentDisposition) return fallback;
  const encoded = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
  if (encoded) {
    try { return decodeURIComponent(encoded); } catch { return fallback; }
  }
  return contentDisposition.match(/filename="([^"]+)"/i)?.[1]
    ?? contentDisposition.match(/filename=([^;]+)/i)?.[1]?.trim()
    ?? fallback;
}

export interface RagDocument { id: number; empresaId: number; setorId?: number; fileName: string; contentHash: string; version: string; status: string; }

export function sanitizeAudit(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(sanitizeAudit);
  if (!value || typeof value !== 'object') return value;
  const blocked = /token|senha|password|secret|authorization|credential|api.?key/i;
  return Object.fromEntries(Object.entries(value as Record<string, unknown>).filter(([key]) => !blocked.test(key)).map(([key, child]) => [key, sanitizeAudit(child)]));
}
