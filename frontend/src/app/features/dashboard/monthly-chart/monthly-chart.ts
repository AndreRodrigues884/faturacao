import { Component, DestroyRef, ElementRef, afterRenderEffect, inject, input, viewChild } from '@angular/core';
import Chart from 'chart.js/auto';

import { MonthFigures } from '../dashboard.models';

/**
 * COMPONENTE - gráfico de barras com as receitas e as despesas de cada mês (Chart.js).
 * Recebe os 12 meses como input e atualiza o gráfico sempre que mudam (ex: outro ano).
 *
 * Fala com:     Chart.js
 * É usado por:  Dashboard
 */

const MONTH_LABELS = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
const euros = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' });

@Component({
  selector: 'app-monthly-chart',
  templateUrl: './monthly-chart.html',
  styleUrl: './monthly-chart.scss',
})
export class MonthlyChart {
  readonly months = input.required<MonthFigures[]>();

  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart: Chart | null = null;

  constructor() {
    afterRenderEffect(() => {
      const months = this.months();
      const revenue = months.map((m) => m.revenue);
      const expenses = months.map((m) => m.expenses);

      if (this.chart) {
        this.chart.data.datasets[0].data = revenue;
        this.chart.data.datasets[1].data = expenses;
        this.chart.update();
        return;
      }

      this.chart = new Chart(this.canvas().nativeElement, {
        type: 'bar',
        data: {
          labels: MONTH_LABELS,
          datasets: [
            { label: 'Receitas', data: revenue, backgroundColor: '#1e88e5', borderRadius: 4 },
            { label: 'Despesas', data: expenses, backgroundColor: '#ef6c00', borderRadius: 4 },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            tooltip: {
              callbacks: {
                                label: (context) => `${context.dataset.label}: ${euros.format(context.parsed.y ?? 0)}`,
              },
            },
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { callback: (value) => euros.format(Number(value)) },
            },
          },
        },
      });
    });

    inject(DestroyRef).onDestroy(() => this.chart?.destroy());
  }
}