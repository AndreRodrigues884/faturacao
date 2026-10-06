import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

/**
 * CONFIGURAÇÃO GLOBAL - o que a aplicação inteira usa: router, HttpClient e interceptors.
 *
 * Fala com:     app.routes.ts, authInterceptor
 * É usado por:  main.ts (ao arrancar a aplicação)
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};