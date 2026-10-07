import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localePt from '@angular/common/locales/pt-PT';
import {
  ApplicationConfig,
  DEFAULT_CURRENCY_CODE,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { PtPaginatorIntl } from './core/ui/pt-paginator-intl';

registerLocaleData(localePt);

/**
 * CONFIGURAÇÃO GLOBAL - router (com parâmetros como inputs), HttpClient com interceptors,
 * formato português (números, moeda, datas, calendário e paginação).
 *
 * Fala com:     app.routes.ts, authInterceptor, PtPaginatorIntl
 * É usado por:  main.ts (ao arrancar a aplicação)
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideNativeDateAdapter(),
    { provide: LOCALE_ID, useValue: 'pt-PT' },
    { provide: DEFAULT_CURRENCY_CODE, useValue: 'EUR' },
    { provide: MAT_DATE_LOCALE, useValue: 'pt-PT' },
    { provide: MatPaginatorIntl, useClass: PtPaginatorIntl },
  ],
};