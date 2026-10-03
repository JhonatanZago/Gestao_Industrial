import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService; let http: HttpTestingController;
  beforeEach(() => { sessionStorage.clear(); TestBed.configureTestingModule({ providers: [AuthService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(AuthService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => { http.verify(); sessionStorage.clear(); });
  it('stores the token and real company scope returned by the login/me contracts', async () => { const promise = service.login('operator@example.com', 'secret'); const request = http.expectOne('/api/v1/auth/login'); expect(request.request.method).toBe('POST'); expect(request.request.body.email).toBe('operator@example.com'); expect(request.request.body.senha).toBe('secret'); request.flush({ accessToken: 'test-token' }); await Promise.resolve(); const me = http.expectOne('/api/v1/auth/me'); expect(me.request.method).toBe('GET'); me.flush({ id: 9, nome: 'Operador', email: 'operator@example.com', empresaId: 12, perfil: 'OPERACIONAL', permissoes: ['ANALYTICS_VISUALIZAR'], setores: [3] }); await promise; expect(service.hasSession()).toBe(true); expect(service.token()).toBe('test-token'); expect(service.companyId()).toBe(12); expect(service.sectors()).toEqual([3]); expect(service.hasPermission('ANALYTICS_VISUALIZAR')).toBe(true); });
  it('discards the client session on logout', () => { sessionStorage.setItem('metalurgica.erp.session', JSON.stringify({ token: 'x' })); service.logout(); expect(service.hasSession()).toBe(false); });
});
