import { CurrencyPipe, DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { InvoiceStatusBadge } from '../../invoices/invoice-status-badge/invoice-status-badge';
import { DashboardData } from '../dashboard.models';
import { MonthlyChart } from '../monthly-chart/monthly-chart';

/**
 * COMPONENTE - página inicial: receitas, despesas e resultado do ano, valores a receber,
 * gráfico mensal, despesas por categoria e faturas mais atrasadas.
 *
 * Fala com:     GET /api/dashboard (através do httpResource), AuthService (nome), MonthlyChart, InvoiceStatusBadge
 * É usado por:  app.routes.ts (rota /inicio)
 */
@Component({
  selector: 'app-dashboard',
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    MatCardModule,
    MatSelectModule,
    MatProgressBarModule,
    MonthlyChart,
    InvoiceStatusBadge,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  protected readonly auth = inject(AuthService);

  private readonly currentYear = new Date().getFullYear();
  protected readonly years = [this.currentYear, this.currentYear - 1, this.currentYear - 2];
  protected readonly year = signal(this.currentYear);

  protected readonly dashboard = httpResource<DashboardData>(() => `/api/dashboard?year=${this.year()}`);

  /** O maior total das categorias, para desenhar as barras em proporção. */
  private readonly maxCategoryTotal = computed(() => {
    const totals = this.dashboard.value()?.expensesByCategory.map((c) => c.total) ?? [];
    return Math.max(1, ...totals);
  });

  protected categoryWidth(total: number): number {
    return (total / this.maxCategoryTotal()) * 100;
  }
}