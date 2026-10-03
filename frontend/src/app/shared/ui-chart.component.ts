import { Component, computed, input, signal } from '@angular/core';
import { TemporalPoint } from '../core/models';

@Component({
  selector: 'ui-line-chart',
  standalone: true,
  template: `
    <div class="chart" role="img" [attr.aria-label]="label()">
      <div class="chart-y-labels">
        @for (value of axisLabels(); track $index) { <span>{{ value }}</span> }
      </div>
      <svg viewBox="0 0 700 230" preserveAspectRatio="none">
        <defs>
          <linearGradient id="area-gradient" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0" stop-color="#14e2a4" stop-opacity=".5" />
            <stop offset=".68" stop-color="#14e2a4" stop-opacity=".13" />
            <stop offset="1" stop-color="#14e2a4" stop-opacity="0" />
          </linearGradient>
        </defs>
        <g class="grid-lines">
          <line x1="0" x2="700" y1="40" y2="40" />
          <line x1="0" x2="700" y1="95" y2="95" />
          <line x1="0" x2="700" y1="150" y2="150" />
          <line x1="0" x2="700" y1="205" y2="205" />
        </g>
        @if (points().length) {
          <path class="area" [attr.d]="areaPath()" />
          <path class="line" [attr.d]="linePath()" />
          @if (hoveredPoint(); as activePoint) {
            <line class="hover-line" [attr.x1]="activePoint.x" [attr.x2]="activePoint.x" y1="28" y2="210" />
          }
          @for (point of plottedPoints(); track $index) {
            <circle class="point" [attr.cx]="point.x" [attr.cy]="point.y" r="3" tabindex="0"
              [attr.aria-label]="point.label" (mouseenter)="hoveredPoint.set(point)"
              (mouseleave)="hoveredPoint.set(null)" (focus)="hoveredPoint.set(point)"
              (blur)="hoveredPoint.set(null)"><title>{{ point.label }}</title></circle>
          }
        } @else {
          <text x="350" y="120" text-anchor="middle" class="empty-chart">Série indisponível</text>
        }
      </svg>
      @if (hoveredPoint(); as point) {
        <div class="chart-tooltip" [style.left.%]="point.x / 7" [style.top.%]="point.y / 2.3">
          <strong>{{ point.label.split(': ')[0] }}</strong>
          <span>{{ point.label.split(': ')[1] }} peças</span>
        </div>
      }
      <div class="chart-axis-labels">
        @for (point of points(); track $index) {
          @if ($index === 0 || $index === points().length - 1 || $index % 3 === 0) {
            <span>{{ axisLabel(point, $index) }}</span>
          }
        }
      </div>
      <div class="chart-caption">{{ rangeLabel() }}</div>
    </div>
  `,
  styles: [`
    .chart{height:250px;width:100%;position:relative;padding-left:41px;padding-right:10px}
    .chart svg{height:calc(100% - 24px);width:100%;overflow:visible}
    .grid-lines line{stroke:rgba(168,187,181,.16);stroke-dasharray:4 7;stroke-width:1}
    .area{fill:url(#area-gradient)}
    .line{fill:none;stroke:#14e2a4;stroke-width:2.25;filter:drop-shadow(0 0 3px rgba(20,226,164,.18));vector-effect:non-scaling-stroke}
    .hover-line{stroke:#47e7b5;stroke-width:1;stroke-dasharray:4 4;stroke-opacity:.58;vector-effect:non-scaling-stroke}
    .point{fill:#0d1917;stroke:#14e2a4;stroke-width:2;cursor:pointer;transition:r .18s ease,stroke-width .18s ease}
    .point:hover,.point:focus{r:4;outline:none;stroke:#f3f8f6;stroke-width:2.5}
    .empty-chart{fill:#a8bbb5;font-size:13px}
    .chart-tooltip{position:absolute;z-index:2;transform:translate(-50%,-115%);display:grid;gap:4px;min-width:126px;padding:9px 11px;border:1px solid rgba(255,255,255,.09);border-radius:10px;background:#10201d;box-shadow:0 10px 28px rgba(0,0,0,.28);pointer-events:none;color:#f3f8f6;font-size:11px}
    .chart-tooltip span{color:#14e2a4}
    .chart-y-labels{position:absolute;left:0;top:20px;bottom:43px;display:flex;flex-direction:column;justify-content:space-between;color:#829b93;font-size:11px;pointer-events:none}
    .chart-axis-labels{position:absolute;left:41px;right:10px;bottom:7px;display:flex;justify-content:space-between;color:#a8bbb5;font-size:11px;pointer-events:none}
    .chart-caption{display:none}
  `]
})
export class UiChartComponent {
  readonly label = input('Evolução temporal');
  readonly points = input<TemporalPoint[]>([]);
  readonly hoveredPoint = signal<{ x: number; y: number; label: string } | null>(null);

  readonly axisLabels = computed(() => {
    const max = Math.max(...this.points().map(point => Number(point.quantidadeProduzida ?? 0)), 1);
    return [max, max * .75, max * .5, max * .25, 0].map(value => Math.round(value).toLocaleString('pt-BR'));
  });

  readonly plottedPoints = computed(() => {
    const values = this.points().map(point => Number(point.quantidadeProduzida ?? 0));
    if (!values.length) return [];
    const max = Math.max(...values, 1);
    const step = values.length === 1 ? 0 : 700 / (values.length - 1);
    return values.map((value, index) => ({
      x: Math.round(index * step),
      y: Math.round(205 - (value / max) * 165),
      label: `${this.axisLabel(this.points()[index] ?? {}, index)}: ${value.toLocaleString('pt-BR')}`
    }));
  });

  readonly linePath = computed(() => this.plottedPoints().map((point, index) => `${index ? 'L' : 'M'} ${point.x} ${point.y}`).join(' '));
  readonly areaPath = computed(() => this.plottedPoints().length ? `${this.linePath()} L 700 210 L 0 210 Z` : '');
  readonly rangeLabel = computed(() => this.points().length ? `${this.points().length} pontos reais` : 'Sem série temporal');

  axisLabel(point: TemporalPoint, index: number): string {
    const period = point.periodo ?? point.inicio;
    return period ? period.slice(5, 10).replace('-', '/') : `P${index + 1}`;
  }
}
