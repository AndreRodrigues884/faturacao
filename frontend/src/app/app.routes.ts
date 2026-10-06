import { Routes } from '@angular/router';

import { authGuard, guestGuard } from './core/auth/auth.guards';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { Shell } from './layout/shell/shell';

/**
 * ROTAS - que endereço mostra que ecrã.
 *   /login     -> ecrã de login (só sem sessão)
 *   /          -> o Shell (menu + barra de topo), só com sessão; as páginas aparecem dentro dele
 *   /inicio    -> página inicial, dentro do Shell
 *
 * Fala com:     guards, componentes das páginas
 * É usado por:  app.config.ts (provideRouter)
 */
export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [guestGuard] },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'inicio', component: Home },
    ],
  },
  { path: '**', redirectTo: '' },
];