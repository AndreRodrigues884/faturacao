/**
 * MODELOS - espelham os DTOs do backend: ExpenseResponse, ExpenseRequest, e o enum PaymentMethod,
 * com as etiquetas em português. Inclui os filtros da listagem.
 *
 * Fala com:     ninguém (só tipos e constantes)
 * É usado por:  ExpenseService, ExpenseList, ExpenseFormDialog
 */

export type PaymentMethod = 'CARD' | 'CASH' | 'TRANSFER' | 'MB_WAY' | 'OTHER';

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  CARD: 'Cartão',
  CASH: 'Numerário',
  TRANSFER: 'Transferência',
  MB_WAY: 'MB WAY',
  OTHER: 'Outro',
};

export interface ExpenseCategory {
  id: number;
  name: string;
}

export interface Expense {
  id: number;
  category: ExpenseCategory;
  description: string;
  supplierName: string | null;
  supplierNif: string | null;
  expenseDate: string;
  netAmount: number;
  vatAmount: number;
  totalAmount: number;
  paymentMethod: PaymentMethod;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseRequest {
  categoryId: number;
  description: string;
  supplierName: string | null;
  supplierNif: string | null;
  expenseDate: string;
  totalAmount: number;
  vatAmount: number | null;
  paymentMethod: PaymentMethod;
  notes: string | null;
}

export interface ExpenseQuery {
  search: string;
  categoryId: number | null;
  paymentMethod: PaymentMethod | null;
  from: string | null;
  to: string | null;
  page: number;
  size: number;
  sort: string;
}