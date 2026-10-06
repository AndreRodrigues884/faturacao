import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Router, RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, merge, of, startWith, switchMap, tap } from 'rxjs';


import { errorMessage } from '../../../core/api/api-errors';
import { PageResponse } from '../../../core/api/page';
import { NotificationService } from '../../../core/ui/notification.service';
import { toIsoDate } from '../../../shared/utils/date';
import { InvoiceStatusBadge } from '../invoice-status-badge/invoice-status-badge';
import { INVOICE_STATUS_LABELS, InvoiceStatus, InvoiceSummary } from '../invoice.models';
import { InvoiceService } from '../invoice.service';

/**
 * COMPONENTE - listagem de faturas: filtros (pesquisa, estado, atraso, datas de emissão),
 * paginação e ordenação feitas no servidor. Clicar numa linha abre o detalhe.
 *
 * Fala com:     InvoiceService, Router, NotificationService, InvoiceStatusBadge
 * É usado por:  app.routes.ts (rota /faturas)
 */
@Component({
  selector: 'app-invoice-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    MatTableModule,
    RouterLink,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatButtonModule,
    MatProgressBarModule,
    InvoiceStatusBadge,
  ],
  templateUrl: './invoice-list.html',
  styleUrl: './invoice-list.scss',
})
export class InvoiceList {
  private readonly service = inject(InvoiceService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  protected readonly statusOptions = Object.entries(INVOICE_STATUS_LABELS) as [InvoiceStatus, string][];
  protected readonly columns = ['number', 'clientName', 'issueDate', 'dueDate', 'total', 'status'];

  protected readonly filters = this.fb.group({
    search: this.fb.nonNullable.control(''),
    status: this.fb.control<InvoiceStatus | null>(null),
    overdue: this.fb.nonNullable.control(false),
    issuedFrom: this.fb.control<Date | null>(null),
    issuedTo: this.fb.control<Date | null>(null),
  });

  protected readonly page = signal<PageResponse<InvoiceSummary> | null>(null);
  protected readonly loading = signal(true);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);
  private readonly sort = signal('createdAt,desc');

  private readonly reload$ = new Subject<void>();

  constructor() {
    merge(
      this.filters.valueChanges.pipe(
        debounceTime(300),
        tap(() => this.pageIndex.set(0)),
      ),
      this.reload$,
    )
      .pipe(
        startWith(null),
        tap(() => this.loading.set(true)),
        switchMap(() =>
          this.service.search(this.buildQuery()).pipe(
            catchError((error) => {
              this.notifications.error(errorMessage(error, 'Não foi possível carregar as faturas.'));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((page) => {
        this.page.set(page);
        this.loading.set(false);
      });
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.reload$.next();
  }

  onSort(sort: Sort): void {
    this.sort.set(sort.direction ? `${sort.active},${sort.direction}` : 'createdAt,desc');
    this.pageIndex.set(0);
    this.reload$.next();
  }

  clearFilters(): void {
    this.filters.reset({ search: '', status: null, overdue: false, issuedFrom: null, issuedTo: null });
  }

  open(invoice: InvoiceSummary): void {
    this.router.navigate(['/faturas', invoice.id]);
  }

  private buildQuery() {
    const value = this.filters.getRawValue();
    return {
      search: value.search,
      status: value.status,
      overdue: value.overdue,
      issuedFrom: toIsoDate(value.issuedFrom),
      issuedTo: toIsoDate(value.issuedTo),
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.sort(),
    };
  }
}