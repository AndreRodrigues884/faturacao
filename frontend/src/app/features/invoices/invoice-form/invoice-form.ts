import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';

import { applyServerErrors, errorMessage } from '../../../core/api/api-errors';
import { NotificationService } from '../../../core/ui/notification.service';
import { addDays, fromIsoDate, toIsoDate } from '../../../shared/utils/date';
import { Client } from '../../clients/client.models';
import { ClientService } from '../../clients/client.service';
import { Product } from '../../products/product.models';
import { ProductService } from '../../products/product.service';
import { Invoice, InvoiceRequest } from '../invoice.models';
import { InvoiceService } from '../invoice.service';

/**
 * COMPONENTE - formulário de criar e editar rascunhos de fatura: cliente, vencimento, notas
 * e uma lista dinâmica de linhas (produto + quantidade), com pré-visualização dos totais.
 * Os totais definitivos são sempre calculados pelo backend.
 *
 * Fala com:     InvoiceService, ClientService, ProductService, Router, NotificationService, api-errors
 * É usado por:  app.routes.ts (rotas /faturas/nova e /faturas/:id/editar)
 */

type LineForm = FormGroup<{
  productId: FormControl<number | null>;
  quantity: FormControl<number | null>;
}>;

interface LinePreview {
  netCents: number;
  vatCents: number;
  totalCents: number;
}

/** Divisão inteira com arredondamento HALF_UP (como o RoundingMode.HALF_UP do backend). */
function divideHalfUp(dividend: number, divisor: number): number {
  return Math.floor((2 * dividend + divisor) / (2 * divisor));
}

@Component({
  selector: 'app-invoice-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './invoice-form.html',
  styleUrl: './invoice-form.scss',
})
export class InvoiceForm implements OnInit {
  readonly id = input<string>();

  private readonly invoiceService = inject(InvoiceService);
  private readonly clientService = inject(ClientService);
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  protected readonly clients = signal<Client[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly isEdit = computed(() => !!this.id());
  protected readonly backLink = computed(() => (this.id() ? ['/faturas', this.id()] : ['/faturas']));

  protected readonly form = this.fb.group({
    clientId: this.fb.control<number | null>(null, Validators.required),
    dueDate: this.fb.control<Date | null>(addDays(new Date(), 30), Validators.required),
    notes: this.fb.nonNullable.control('', Validators.maxLength(500)),
    lines: this.fb.array<LineForm>([], Validators.required),
  });

  protected get lines() {
    return this.form.controls.lines;
  }

  // ---------- Pré-visualização dos totais ----------

  private readonly linesValue = toSignal(this.lines.valueChanges, { initialValue: [] });
  private readonly productsById = computed(() => new Map(this.products().map((p) => [p.id, p])));

  protected readonly previews = computed(() =>
    this.linesValue().map((line) => this.preview(line.productId ?? null, line.quantity ?? null)),
  );

  protected readonly totals = computed(() =>
    this.previews().reduce<LinePreview>(
      (sum, line) => ({
        netCents: sum.netCents + (line?.netCents ?? 0),
        vatCents: sum.vatCents + (line?.vatCents ?? 0),
        totalCents: sum.totalCents + (line?.totalCents ?? 0),
      }),
      { netCents: 0, vatCents: 0, totalCents: 0 },
    ),
  );

  // ---------- Carregar ----------

  ngOnInit(): void {
    const id = this.id();

    forkJoin({
      clients: this.clientService.list(),
      products: this.productService.list(true),
      invoice: id ? this.invoiceService.get(Number(id)) : of(null),
    }).subscribe({
      next: ({ clients, products, invoice }) => {
        this.clients.set(clients);
        this.products.set(products);

        if (invoice) {
          if (invoice.status !== 'DRAFT') {
            this.notifications.error('Só os rascunhos podem ser editados.');
            this.router.navigate(['/faturas', invoice.id]);
            return;
          }
          this.fillForm(invoice);
        } else {
          this.addLine();
        }

        this.loading.set(false);
      },
      error: (error) => {
        this.notifications.error(errorMessage(error, 'Não foi possível abrir o formulário.'));
        this.router.navigate(['/faturas']);
      },
    });
  }

  // ---------- Linhas ----------

  addLine(productId: number | null = null, quantity: number | null = 1): void {
    this.lines.push(
      this.fb.group({
        productId: this.fb.control<number | null>(productId, Validators.required),
        quantity: this.fb.control<number | null>(quantity, [Validators.required, Validators.min(0.001)]),
      }),
    );
  }

  removeLine(index: number): void {
    this.lines.removeAt(index);
  }

  // ---------- Guardar ----------

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      if (this.lines.length === 0) {
        this.errorMessage.set('Acrescente pelo menos uma linha.');
      }
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();
    const request: InvoiceRequest = {
      clientId: value.clientId!,
      dueDate: toIsoDate(value.dueDate)!,
      notes: value.notes || null,
      lines: value.lines.map((line) => ({
        productId: line.productId!,
        quantity: line.quantity!,
      })),
    };

    const id = this.id();
    const operation = id
      ? this.invoiceService.update(Number(id), request)
      : this.invoiceService.create(request);

    operation.subscribe({
      next: (invoice) => {
        this.notifications.success(id ? 'Rascunho atualizado' : 'Rascunho criado');
        this.router.navigate(['/faturas', invoice.id]);
      },
      error: (error) => {
        this.saving.set(false);
        if (applyServerErrors(this.form, error)) {
          this.errorMessage.set('Corrija os campos assinalados.');
          return;
        }
        this.errorMessage.set(errorMessage(error, 'Não foi possível guardar a fatura.'));
      },
    });
  }

  // ---------- Auxiliares ----------

  private fillForm(invoice: Invoice): void {
    this.form.patchValue({
      clientId: invoice.client.id,
      dueDate: fromIsoDate(invoice.dueDate),
      notes: invoice.notes ?? '',
    });
    this.lines.clear();
    for (const line of invoice.lines) {
      this.addLine(line.productId, line.quantity);
    }
  }

  /** Estimativa de uma linha, em cêntimos inteiros, com as mesmas regras do backend. */
  private preview(productId: number | null, quantity: number | null): LinePreview | null {
    const product = productId !== null ? this.productsById().get(productId) : undefined;
    if (!product || quantity === null || quantity <= 0) {
      return null;
    }

    const quantityThousandths = Math.round(quantity * 1000);
    const priceCents = Math.round(product.unitPrice * 100);

    const netCents = divideHalfUp(quantityThousandths * priceCents, 1000);
    const vatCents = divideHalfUp(netCents * product.vatPercentage, 100);

    return { netCents, vatCents, totalCents: netCents + vatCents };
  }
}