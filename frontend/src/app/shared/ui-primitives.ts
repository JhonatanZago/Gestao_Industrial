import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({ selector: 'ui-input', standalone: true, imports: [FormsModule], template: `<label>{{label()}}<input [type]="type()" [value]="value()" [placeholder]="placeholder()" (input)="valueChange.emit($any($event.target).value)" /></label>`, styles: [`label{display:grid;gap:.4rem;color:var(--text-secondary);font-size:.78rem}input{background:var(--bg-surface);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:.7rem;color:var(--text-primary)}`] })
export class UiInputComponent { readonly label = input.required<string>(); readonly type = input('text'); readonly value = input(''); readonly placeholder = input(''); readonly valueChange = output<string>(); }

@Component({ selector: 'ui-select', standalone: true, imports: [FormsModule], template: `<label>{{label()}}<select [value]="value()" (change)="valueChange.emit($any($event.target).value)"><ng-content /></select></label>`, styles: [`label{display:grid;gap:.4rem;color:var(--text-secondary);font-size:.78rem}select{background:var(--bg-surface);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:.7rem;color:var(--text-primary)}`] })
export class UiSelectComponent { readonly label = input.required<string>(); readonly value = input(''); readonly valueChange = output<string>(); }

@Component({ selector: 'ui-page-header', standalone: true, template: `<header><div><small>{{eyebrow()}}</small><h1>{{title()}}</h1><p>{{subtitle()}}</p></div><ng-content /></header>`, styles: [`header{display:flex;justify-content:space-between;align-items:flex-end;gap:1rem;margin-bottom:1.5rem}small{color:var(--accent);letter-spacing:.16em;font-weight:800}h1{margin:.35rem 0;font-size:2rem}p{color:var(--text-muted);margin:0}`] })
export class UiPageHeaderComponent { readonly eyebrow = input('CENTRO DE CONTROLE'); readonly title = input.required<string>(); readonly subtitle = input(''); }

@Component({ selector: 'ui-empty-state', standalone: true, template: `<section role="status"><strong>{{title()}}</strong><p>{{message()}}</p><button type="button" (click)="retry.emit()">Tentar novamente</button></section>`, styles: [`section{text-align:center;padding:2rem;color:var(--text-muted)}strong{color:var(--text-primary)}button{margin-top:.8rem;background:var(--bg-surface-raised);border:1px solid var(--border-subtle);border-radius:var(--radius-sm);padding:.55rem .8rem;color:var(--text-primary)}`] })
export class UiEmptyStateComponent { readonly title = input('Nenhum dado encontrado'); readonly message = input('Não há dados para os filtros selecionados.'); readonly retry = output<void>(); }
