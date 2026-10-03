import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UiDialogComponent, UiStateComponent, UiTextInputComponent } from './ui-form-components';

@Component({ standalone: true, imports: [UiTextInputComponent, UiDialogComponent, UiStateComponent], template: `<ui-text-input id="name" label="Nome"/><ui-dialog [open]="true" title="Confirmar"><p>Conteúdo</p></ui-dialog><ui-state kind="forbidden"/>` })
class Host {}

describe('form and state primitives', () => {
  it('exposes labels, dialog semantics and forbidden state', async () => {
    await TestBed.configureTestingModule({ imports: [Host] }).compileComponents();
    const fixture = TestBed.createComponent(Host); fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('label')?.textContent).toContain('Nome');
    expect(el.querySelector('[role="dialog"]')).toBeTruthy();
    expect(el.textContent).toContain('Acesso');
  });
});
