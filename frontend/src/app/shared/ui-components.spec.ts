import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UiBadgeComponent } from './ui-badge.component';
import { UiButtonComponent } from './ui-button.component';
import { UiChartComponent } from './ui-chart.component';

@Component({ standalone: true, imports: [UiButtonComponent, UiBadgeComponent, UiChartComponent], template: `<ui-button>Salvar</ui-button><ui-badge>Ativo</ui-badge><ui-line-chart />` }) class HostComponent {}
describe('shared UI components', () => { it('renders accessible shared primitives', async () => { await TestBed.configureTestingModule({ imports: [HostComponent] }).compileComponents(); const el = TestBed.createComponent(HostComponent).nativeElement as HTMLElement; expect(el.querySelector('button')?.textContent).toContain('Salvar'); expect(el.querySelector('ui-badge')).toBeTruthy(); expect(el.querySelector('svg')).toBeTruthy(); }); });
