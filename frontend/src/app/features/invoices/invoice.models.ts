import { VatRate } from '../products/product.models';

/**
 * MODELOS - espelham os DTOs do backend: InvoiceSummaryResponse (listagem), InvoiceResponse (detalhe),
 * InvoiceRequest (criar/editar), e os filtros da listagem.
 *
 * Fala com:     VatRate (dos produtos)
 * É usado por:  InvoiceService, InvoiceList, InvoiceDetail, InvoiceStatusBadge (e o formulário, a seguir)
 */

export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PAID' | 'CANCELLED';

export const INVOICE_STATUS_LABELS: Record<InvoiceStatus, string> = {
  DRAFT: 'Rascunho',
  ISSUED: 'Emitida',
  PAID: 'Paga',
  CANCELLED: 'Anulada',
};

/** Uma linha da listagem (sem as linhas da fatura). */
export interface InvoiceSummary {
  id: number;
  number: string | null;
  status: InvoiceStatus;
  clientId: number;
  clientName: string;
  issueDate: string | null;
  dueDate: string;
  paidDate: string | null;
  total: number;
  overdue: boolean;
  daysOverdue: number;
}

export interface InvoiceClient {
  id: number;
  name: string;
  nif: string;
  address: string | null;
  postalCode: string | null;
  city: string | null;
}

export interface InvoiceLine {
  id: number;
  lineNumber: number;
  productId: number;
  productCode: string;
  description: string;
  quantity: number;
  unitPrice: number;
  vatRate: VatRate;
  vatPercentage: number;
  lineNet: number;
  lineVat: number;
  lineTotal: number;
}

/** A fatura completa, com cliente e linhas. */
export interface Invoice {
  id: number;
  number: string | null;
  status: InvoiceStatus;
  client: InvoiceClient;
  issueDate: string | null;
  dueDate: string;
  paidDate: string | null;
  notes: string | null;
  lines: InvoiceLine[];
  totalNet: number;
  totalVat: number;
  total: number;
  cancelledAt: string | null;
  cancellationReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InvoiceLineRequest {
  productId: number;
  quantity: number;
}

export interface InvoiceRequest {
  clientId: number;
  dueDate: string;
  notes: string | null;
  lines: InvoiceLineRequest[];
}

/** Filtros, página e ordenação da listagem. */
export interface InvoiceQuery {
  search: string;
  status: InvoiceStatus | null;
  overdue: boolean;
  issuedFrom: string | null;
  issuedTo: string | null;
  page: number;
  size: number;
  sort: string;
}