import { InvoiceSummary } from '../invoices/invoice.models';

/**
 * MODELOS - espelham o DashboardResponse do backend.
 *
 * Fala com:     InvoiceSummary (faturas em atraso)
 * É usado por:  Dashboard, MonthlyChart
 */

export interface MonthFigures {
  month: number;
  revenue: number;
  expenses: number;
  result: number;
}

export interface DashboardData {
  year: number;
  months: MonthFigures[];
  totals: { revenue: number; expenses: number; result: number };
  receivables: { openCount: number; openTotal: number; overdueCount: number; overdueTotal: number };
  expensesByCategory: { categoryId: number; categoryName: string; total: number }[];
  overdueInvoices: InvoiceSummary[];
}