import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export class ApiRequestError extends Error {
  constructor(message: string, readonly status: number, readonly correlationId?: string, readonly code?: string, readonly backendMessage?: string) { super(message); }
}

export const errorInterceptor: HttpInterceptorFn = (request, next) => next(request).pipe(catchError((error: HttpErrorResponse) => {
  const correlationId = error.headers.get('X-Correlation-Id') ?? error.error?.correlationId;
  const code = error.error?.code;
  const message = error.status === 401 ? 'Sua sessão expirou. Entre novamente.' : error.status === 403 ? 'Acesso não autorizado para esta operação.' : error.status === 429 ? 'Muitas tentativas. Aguarde e tente novamente.' : `Falha na comunicação com o backend${correlationId ? ` [${correlationId}]` : ''}.`;
  return throwError(() => new ApiRequestError(message, error.status, correlationId ?? undefined, code, typeof error.error?.message === 'string' ? error.error.message : undefined));
}));
