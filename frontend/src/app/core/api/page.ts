/**
 * MODELO - uma página de resultados. Espelha o PageResponse<T> genérico do backend.
 *
 * Fala com:     ninguém (só tipos)
 * É usado por:  services com listagens paginadas (InvoiceService e, mais tarde, ExpenseService)
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}