import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/**
 * GUARDS - decidem se uma rota pode ser aberta. Se não, redirecionam.
 *   authGuard  -> só com sessão válida (senão vai para /login)
 *   adminGuard -> só para ADMIN (senão vai para /inicio)
 *   guestGuard -> só SEM sessão (quem já fez login não volta a ver o /login)
 *
 * Fala com:     AuthService, Router
 * É usado por:  app.routes.ts
 */

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasValidSession() ? true : router.createUrlTree(['/login']);
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAdmin() ? true : router.createUrlTree(['/inicio']);
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasValidSession() ? router.createUrlTree(['/inicio']) : true;
};