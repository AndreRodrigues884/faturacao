/**
 * UTILITÁRIO - converte uma data do calendário no formato da API ("2026-10-31"),
 * usando o dia LOCAL, sem conversões de fuso horário.
 *
 * Fala com:     ninguém
 * É usado por:  filtros e formulários com datas (InvoiceList, InvoiceForm, despesas)
 */
export function toIsoDate(date: Date | null): string | null {
  if (!date) {
    return null;
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}