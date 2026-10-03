export interface Page<T> { content: T[]; number: number; size: number; totalElements: number; totalPages: number; }
export interface ApiError { code?: string; message?: string; correlationId?: string; fieldErrors?: Record<string, string>; }
export interface DashboardSummary {
  inicio?: string; fim?: string; quantidadeProduzida?: number; quantidadeRegistros?: number;
  duracaoBrutaMinutos?: number; tempoParadoMinutos?: number; tempoProdutivoMinutos?: number;
  taxaProducaoBruta?: number; taxaProducaoProdutiva?: number; percentualTempoParado?: number;
}

export interface ProductionGroup {
  id?: number;
  codigo?: string;
  nome?: string;
  setorCodigo?: string;
  setorNome?: string;
  quantidadeProduzida?: number;
  quantidadeRegistros?: number;
  duracaoBrutaMinutos?: number;
  tempoParadoMinutos?: number;
  tempoProdutivoMinutos?: number;
}

export interface TemporalPoint { periodo?: string; inicio?: string; fim?: string; quantidadeProduzida?: number; quantidadeRegistros?: number; }

export interface CoreDashboard {
  inicio?: string;
  fim?: string;
  quantidadeProduzida?: number;
  duracaoRegistradaMinutos?: number;
  setoresComDados?: number;
  funcionariosEnvolvidos?: number;
  codigosPecaProduzidos?: number;
  porSetor?: ProductionGroup[];
  porFuncionario?: ProductionGroup[];
  porPeca?: ProductionGroup[];
  evolucao?: TemporalPoint[];
}

export interface SimplifiedReport { dashboard?: CoreDashboard; resumoOperacional?: string; }

export type ReportExportFormat = 'CSV' | 'XLSX';

export interface EmployeeAnalytics {
  funcionarioId?: number;
  codigo?: string;
  nome?: string;
  quantidadeProduzida?: number;
  quantidadeRegistros?: number;
  duracaoBrutaMinutos?: number;
  tempoParadoMinutos?: number;
  tempoProdutivoMinutos?: number;
  pecas?: ProductionGroup[];
  setores?: ProductionGroup[];
}

export function findByOperationalCode<T extends { codigo?: string }>(items: T[], code: string): T | undefined {
  return items.find(item => String(item.codigo ?? '') === code);
}
