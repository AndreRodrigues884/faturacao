import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localePt from '@angular/common/locales/pt-PT';
import {
  ApplicationConfig,
  DEFAULT_CURRENCY_CODE,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

registerLocaleData(localePt);

/**
 * CONFIGURAÇÃO GLOBAL - router, HttpClient com interceptors, e formato português
 * para números, moedas e datas (pt-PT, euros).
 *
 * Fala com:     app.routes.ts, authInterceptor
 * É usado por:  main.ts (ao arrancar a aplicação)
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'pt-PT' },
    { provide: DEFAULT_CURRENCY_CODE, useValue: 'EUR' },
  ],
};