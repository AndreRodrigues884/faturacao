import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { Router, RouterLink } from '@angular/router';
import { Observable, filter, finalize, switchMap } from 'rxjs';

import { errorMessage } from '../../../core/api/api-errors';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/confirm-dialog/confirm-dialog';
import { PromptDialog, PromptDialogData } from '../../../shared/prompt-dialog/prompt-dialog';
import { InvoiceStatusBadge } from '../invoice-status-badge/invoice-status-badge';
import { Invoice } from '../invoice.models';
import { InvoiceService } from '../invoice.service';

/**
 * COMPONENTE - página de uma fatura: cliente, datas, linhas e totais, e as ações do ciclo de vida.
 *   Rascunho -> Editar, Emitir, Apagar
 *   Emitida  -> Marcar como paga, Anular (só ADMIN)
 *   Paga / Anulada -> sem ações
 *
 * Fala com:     InvoiceService, MatDialog (ConfirmDialog, PromptDialog), Router, NotificationService, AuthService
 * É usado por:  app.routes.ts (rota /faturas/:id)
 */
@Component({
  selector: 'app-invoice-detail',
  imports: [
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatProgressBarModule,
    InvoiceStatusBadge,
  ],
  templateUrl: './invoice-detail.html',
  styleUrl: './invoice-detail.scss',
})
export class InvoiceDetail implements OnInit {
  readonly id = input.required<string>();

  private readonly service = inject(InvoiceService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly invoice = signal<Invoice | null>(null);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly lineColumns = ['description', 'quantity', 'unitPrice', 'vat', 'lineNet', 'lineTotal'];

  ngOnInit(): void {
    this.service.get(Number(this.id())).subscribe({
      next: (invoice) => {
        this.invoice.set(invoice);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.notifications.error(errorMessage(error, 'Não foi possível carregar a fatura.'));
        this.router.navigate(['/faturas']);
      },
    });
  }

  // ---------- Ações do ciclo de vida ----------

  issue(invoice: Invoice): void {
    this.runAfterConfirm(
      {
        title: 'Emitir fatura',
        message: 'Depois de emitida, a fatura recebe um número e já não pode ser alterada nem apagada.',
        confirmLabel: 'Emitir',
      },
      () => this.service.issue(invoice.id),
      (updated) => `Fatura ${updated.number} emitida`,
    );
  }

  markAsPaid(invoice: Invoice): void {
    this.runAfterConfirm(
      {
        title: 'Marcar como paga',
        message: `Confirma que a fatura ${invoice.number} foi paga hoje?`,
        confirmLabel: 'Marcar como paga',
      },
      () => this.service.pay(invoice.id),
      () => 'Fatura marcada como paga',
    );
  }

  cancel(invoice: Invoice): void {
    const data: PromptDialogData = {
      title: `Anular a fatura ${invoice.number}`,
      message: 'A fatura mantém o número, mas fica anulada. Esta ação não pode ser desfeita.',
      label: 'Motivo da anulação',
      confirmLabel: 'Anular',
    };

    this.dialog
      .open<PromptDialog, PromptDialogData, string>(PromptDialog, { data, width: '480px' })
      .afterClosed()
      .pipe(
        filter((reason): reason is string => !!reason),
        switchMap((reason) => this.withBusy(this.service.cancel(invoice.id, reason))),
      )
      .subscribe({
        next: (updated) => {
          this.invoice.set(updated);
          this.notifications.success('Fatura anulada');
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível anular a fatura.')),
      });
  }

  deleteDraft(invoice: Invoice): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Apagar rascunho',
          message: 'O rascunho vai ser apagado. Como ainda não foi emitido, não consome nenhum número.',
          confirmLabel: 'Apagar',
        } satisfies ConfirmDialogData,
      })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.withBusy(this.service.delete(invoice.id))),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Rascunho apagado');
          this.router.navigate(['/faturas']);
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível apagar o rascunho.')),
      });
  }

  // ---------- Auxiliares ----------

  /** Pede confirmação, executa a ação e mostra a fatura atualizada. */
  private runAfterConfirm(
    data: ConfirmDialogData,
    action: () => Observable<Invoice>,
    successMessage: (updated: Invoice) => string,
  ): void {
    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.withBusy(action())),
      )
      .subscribe({
        next: (updated) => {
          this.invoice.set(updated);
          this.notifications.success(successMessage(updated));
        },
        error: (error) => this.notifications.error(errorMessage(error, 'A operação falhou.')),
      });
  }

  /** Desativa os botões enquanto o pedido decorre. */
  private withBusy<T>(request: Observable<T>): Observable<T> {
    this.busy.set(true);
    return request.pipe(finalize(() => this.busy.set(false)));
  }
}