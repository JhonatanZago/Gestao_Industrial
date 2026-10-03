import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DashboardService } from '../core/dashboard.service';
import { AuthService } from '../core/auth.service';
import { CoreDashboard, EmployeeAnalytics, findByOperationalCode, ProductionGroup } from '../core/models';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="core-route" aria-live="polite">
      <a routerLink="/app/dashboard" class="back-link">Visão Geral</a>
      <p class="eyebrow">FLUXO EXCEL → DASHBOARD</p><h1>{{ title() }}</h1>
      @if (loading()) { <p>Carregando dados reais do Backend V1…</p> }
      @if (error()) { <p role="alert">{{ error() }}</p> }
      @if (dashboard(); as data) {
        <section class="core-grid"><article><span>Produção total</span><strong>{{ data.quantidadeProduzida ?? '-' }}</strong></article><article><span>Setores com dados</span><strong>{{ data.setoresComDados ?? '-' }}</strong></article><article><span>Funcionários</span><strong>{{ data.funcionariosEnvolvidos ?? '-' }}</strong></article><article><span>Peças</span><strong>{{ data.codigosPecaProduzidos ?? '-' }}</strong></article></section>
        <section class="core-panel"><h2>Detalhamento</h2><table><thead><tr><th>Código</th><th>Nome</th><th>Produção</th><th>Duração</th></tr></thead><tbody>@for (row of rows(); track row.codigo) { <tr><td>{{ row.codigo || '-' }}</td><td>{{ row.nome || '-' }}</td><td>{{ row.quantidadeProduzida ?? '-' }}</td><td>{{ row.duracaoBrutaMinutos ?? '-' }}</td></tr> }</tbody></table></section>
      }
      @if (employee(); as person) { <section class="core-panel"><h2>{{ person.nome || 'Funcionário' }}</h2><p>Código: <strong>{{ person.codigo }}</strong></p><p>Produção: <strong>{{ person.quantidadeProduzida ?? '-' }}</strong></p>@if (person.pecas?.length) { <h3>Peças relacionadas</h3><ul>@for (item of person.pecas; track item.codigo) { <li>{{ item.codigo }} — {{ item.quantidadeProduzida ?? '-' }}</li> }</ul> }</section> }
      @if (part(); as item) { <section class="core-panel"><h2>{{ item.nome || 'Peça' }}</h2><p>Código: <strong>{{ item.codigo }}</strong></p><p>Produção: <strong>{{ item.quantidadeProduzida ?? '-' }}</strong></p></section> }
    </main>
  `,
  styles: [`:host{display:block;min-height:100dvh;background:#0b1214;color:#e5f0ed;padding:3rem;box-sizing:border-box}.core-route{max-width:1100px;margin:auto}.back-link{color:#44d7a0}.eyebrow{color:#44d7a0;letter-spacing:.18em;font-size:.7rem;margin-top:3rem}h1{font-size:clamp(2rem,4vw,4rem);margin:.2rem 0 2rem}.core-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem}article,.core-panel{background:#101b1d;border:1px solid #1d3030;border-radius:12px;padding:1.2rem}article span{color:#91aaa4;font-size:.8rem;display:block}article strong{display:block;font-size:2rem;margin-top:.6rem}.core-panel{margin-top:1rem;overflow:auto}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:.75rem;border-bottom:1px solid #223534}th{color:#91aaa4;font-size:.75rem}@media(max-width:720px){:host{padding:1rem}.core-grid{grid-template-columns:repeat(2,1fr)}}`]
})
export class CorePageComponent {
  private readonly route = inject(ActivatedRoute); private readonly auth = inject(AuthService); private readonly api = inject(DashboardService);
  readonly loading = signal(false); readonly error = signal(''); readonly title = signal('Visão Geral'); readonly dashboard = signal<CoreDashboard | null>(null); readonly employee = signal<EmployeeAnalytics | null>(null); readonly part = signal<ProductionGroup | null>(null); readonly rows = signal<ProductionGroup[]>([]);
  constructor() { this.route.data.subscribe(data => { this.title.set(String(data['title'] ?? 'Visão Geral')); this.load(String(data['screen'] ?? 'dashboard')); }); }
  private load(screen: string) { const companyId = this.auth.companyId(); if (!companyId) { this.error.set('Sessão sem empresa autorizada.'); return; } const period = { inicio: '2026-09-01T00:00:00', fim: '2026-09-30T23:59:59' }; this.loading.set(true); this.error.set(''); const code = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (screen === 'employee') { this.api.employees(companyId, { ...period, size: 100 }).subscribe({ next: page => { const match = findByOperationalCode(page.content, code); if (!match?.id) { this.fail(); return; } this.api.employee(companyId, match.id, period).subscribe({ next: value => { this.employee.set(value); this.loading.set(false); }, error: () => this.fail() }); }, error: () => this.fail() }); return; }
    if (screen === 'part') { this.api.parts(companyId, { ...period, size: 100 }).subscribe({ next: page => { const match = findByOperationalCode(page.content, code); if (!match) { this.fail(); return; } this.part.set(match); this.loading.set(false); }, error: () => this.fail() }); return; }
    const sectorId = this.route.snapshot.paramMap.get('setorId'); const request = sectorId ? this.api.sectorDashboard(companyId, Number(sectorId), period) : this.api.dashboard(companyId, period); request.subscribe({ next: value => { this.dashboard.set(value); this.rows.set(value.porSetor ?? value.porFuncionario ?? value.porPeca ?? []); this.loading.set(false); }, error: () => this.fail() }); }
  private fail() { this.loading.set(false); this.error.set('Não foi possível carregar os dados autorizados. Tente novamente.'); }
}
