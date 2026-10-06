import { Component, computed, input } from '@angular/core';

import { INVOICE_STATUS_LABELS, InvoiceStatus } from '../invoice.models';

/**
 * COMPONENTE REUTILIZÁVEL - o estado da fatura como uma etiqueta colorida
 * e, se for o caso, o aviso "Em atraso há N dias".
 *
 * Fala com:     ninguém (recebe o estado e os dias de atraso como inputs)
 * É usado por:  InvoiceList, InvoiceDetail
 */
@Component({
  selector: 'app-invoice-status-badge',
  templateUrl: './invoice-status-badge.html',
  styleUrl: './invoice-status-badge.scss',
})
export class InvoiceStatusBadge {
  readonly status = input.required<InvoiceStatus>();
  readonly daysOverdue = input(0);

  protected readonly label = computed(() => INVOICE_STATUS_LABELS[this.status()]);
}