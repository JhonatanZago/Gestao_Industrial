import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

interface LoginResponse { token?: string; accessToken?: string; permissoes?: string[]; permissions?: string[]; }
interface MeResponse { id: number; nome: string; email: string; empresaId: number; perfil: string; permissoes: string[]; setores: number[]; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly key = 'metalurgica.erp.session';
  hasSession() { return !!sessionStorage.getItem(this.key); }
  async login(username: string, password: string) {
    if (!username || !password) throw new Error('credentials');
    const response = await firstValueFrom(this.http.post<LoginResponse>(environment.apiBaseUrl + '/auth/login', { email: username, senha: password }));
    const token = response.token ?? response.accessToken;
    if (!token) throw new Error('missing-token');
    sessionStorage.setItem(this.key, JSON.stringify({ token, permissions: response.permissoes ?? response.permissions ?? [] }));
    try {
      const me = await firstValueFrom(this.http.get<MeResponse>(environment.apiBaseUrl + '/auth/me'));
      sessionStorage.setItem(this.key, JSON.stringify({ token, permissions: me.permissoes ?? response.permissoes ?? response.permissions ?? [], companyId: me.empresaId, userId: me.id, profile: me.perfil, sectors: me.setores ?? [] }));
    } catch (error) {
      sessionStorage.removeItem(this.key);
      throw error;
    }
  }
  logout() { sessionStorage.removeItem(this.key); }
  token(): string | null { try { return JSON.parse(sessionStorage.getItem(this.key) ?? '{}').token ?? null; } catch { return null; } }
  companyId(): number | null { try { return JSON.parse(sessionStorage.getItem(this.key) ?? '{}').companyId ?? null; } catch { return null; } }
  sectors(): number[] { try { return JSON.parse(sessionStorage.getItem(this.key) ?? '{}').sectors ?? []; } catch { return []; } }
  hasPermission(permission: string) { try { const session = JSON.parse(sessionStorage.getItem(this.key) ?? '{}'); const permissions = session.permissions as string[] | undefined; return Array.isArray(permissions) && permissions.includes(permission); } catch { return false; } }
}
