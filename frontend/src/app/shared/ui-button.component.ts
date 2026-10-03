import { Component, input } from '@angular/core';

@Component({ selector: 'ui-button', standalone: true, template: `<button [class.primary]="variant() === 'primary'" [class.secondary]="variant() === 'secondary'" [disabled]="disabled()"><ng-content /></button>`, styles: [`:host{display:inline-block}button{border:0;border-radius:9px;padding:.8rem 1.1rem;font:700 .78rem inherit;cursor:pointer}button.primary{background:#44d7a0;color:#082019}button.secondary{background:#172527;color:#b7cbc6;border:1px solid #273a3b}button:disabled{opacity:.5;cursor:not-allowed}`] })
export class UiButtonComponent { readonly variant = input<'primary' | 'secondary'>('primary'); readonly disabled = input(false); }
