import { Component, input } from '@angular/core';

@Component({ selector: 'ui-badge', standalone: true, template: `<span [class]="tone()"><ng-content /></span>`, styles: [`span{display:inline-block;border-radius:999px;padding:.3rem .55rem;font-size:.65rem;font-weight:700}span.success{background:#18332f;color:#70dcb1}span.warning{background:#3a3020;color:#e2bc78}span.danger{background:#42272b;color:#ffaaa8}span.info{background:#1d3042;color:#86b8ef}`] })
export class UiBadgeComponent { readonly tone = input<'success' | 'warning' | 'danger' | 'info'>('success'); }
