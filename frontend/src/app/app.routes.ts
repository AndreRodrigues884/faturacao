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
  {
    path: 'categorias',
    loadComponent: () =>
      import('./features/categories/category-list/category-list').then((m) => m.CategoryList),
  },
  {
    path: 'clientes',
    loadComponent: () =>
      import('./features/clients/client-list/client-list').then((m) => m.ClientList),
  },
  {
    path: 'produtos',
    loadComponent: () =>
      import('./features/products/product-list/product-list').then((m) => m.ProductList),
  },
  {
    path: 'faturas',
    loadComponent: () =>
      import('./features/invoices/invoice-list/invoice-list').then((m) => m.InvoiceList),
  },
  {
    path: 'faturas/nova',
    loadComponent: () =>
      import('./features/invoices/invoice-form/invoice-form').then((m) => m.InvoiceForm),
  },
  {
    path: 'faturas/:id',
    loadComponent: () =>
      import('./features/invoices/invoice-detail/invoice-detail').then((m) => m.InvoiceDetail),
  },
  {
    path: 'faturas/:id/editar',
    loadComponent: () =>
      import('./features/invoices/invoice-form/invoice-form').then((m) => m.InvoiceForm),
  },
  {
    path: 'despesas',
    loadComponent: () =>
      import('./features/expenses/expense-list/expense-list').then((m) => m.ExpenseList),
  },
  { path: '**', redirectTo: '' },
];