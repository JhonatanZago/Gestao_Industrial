import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { authGuard, permissionGuard } from './guards';

describe('route guards', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideRouter([])] });
  });

  it('redirects unauthenticated users instead of exposing dashboard data', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/login');
  });

  it('redirects users without the requested permission', () => {
    sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'token', permissions: [] }));
    const result = TestBed.runInInjectionContext(() => permissionGuard('SETOR_VISUALIZAR')({} as never, {} as never));
    expect(result).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/forbidden');
  });
});
