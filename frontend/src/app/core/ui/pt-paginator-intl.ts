import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';

/**
 * TRADUÇÃO - textos do paginador da Material em português ("1–20 de 47", "Por página").
 *
 * Fala com:     MatPaginatorIntl (a classe da Material que fornece os textos)
 * É usado por:  app.config.ts (substitui a versão inglesa em toda a aplicação)
 */
@Injectable()
export class PtPaginatorIntl extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Por página';
  override nextPageLabel = 'Página seguinte';
  override previousPageLabel = 'Página anterior';
  override firstPageLabel = 'Primeira página';
  override lastPageLabel = 'Última página';

  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0) {
      return '0 de 0';
    }
    const start = page * pageSize + 1;
    const end = Math.min((page + 1) * pageSize, length);
    return `${start}–${end} de ${length}`;
  };
}