import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  get<T>(path: string, params?: Record<string, string | number | boolean>) { let query = new HttpParams(); for (const [key, value] of Object.entries(params ?? {})) query = query.set(key, String(value)); return this.http.get<T>(environment.apiBaseUrl + path, { params: query }); }
  post<T>(path: string, body: unknown) { return this.http.post<T>(environment.apiBaseUrl + path, body); }
  put<T>(path: string, body: unknown) { return this.http.put<T>(environment.apiBaseUrl + path, body); }
  patch<T>(path: string, body?: unknown) { return this.http.patch<T>(environment.apiBaseUrl + path, body ?? {}); }
  delete<T>(path: string) { return this.http.delete<T>(environment.apiBaseUrl + path); }
}
