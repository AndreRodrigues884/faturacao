import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/**
 * INTERCEPTOR - corre em TODOS os pedidos HTTP da aplicação.
 *   1. Junta o header "Authorization: Bearer <token>" aos pedidos para /api (menos o login).
 *   2. Se a API responder 401 (token inválido ou expirado), termina a sessão e volta ao login.
 *
 * Fala com:     AuthService
 * É usado por:  o HttpClient, configurado no app.config.ts
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);

  const isApiRequest = request.url.startsWith('/api/');
  const isLoginRequest = request.url === '/api/auth/login';
  const token = auth.token();

  const authorizedRequest =
    token && isApiRequest && !isLoginRequest
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authorizedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isLoginRequest) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};