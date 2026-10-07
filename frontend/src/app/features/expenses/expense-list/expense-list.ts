import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Subject, catchError, debounceTime, filter, merge, of, startWith, switchMap, tap } from 'rxjs';

import { errorMessage } from '../../../core/api/api-errors';
import { PageResponse } from '../../../core/api/page';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/confirm-dialog/confirm-dialog';
import { toIsoDate } from '../../../shared/utils/date';
import { Category } from '../../categories/category.models';
import { CategoryService } from '../../categories/category.service';
import { ExpenseFormData, ExpenseFormDialog } from '../expense-form-dialog/expense-form-dialog';
import { Expense, PAYMENT_METHOD_LABELS, PaymentMethod } from '../expense.models';
import { ExpenseService } from '../expense.service';

/**
 * COMPONENTE - página de despesas: filtros (pesquisa, categoria, forma de pagamento, datas),
 * paginação e ordenação no servidor, criar/editar numa janela, apagar.
 *
 * Fala com:     ExpenseService, CategoryService (para o filtro e o formulário), MatDialog, NotificationService
 * É usado por:  app.routes.ts (rota /despesas)
 */
@Component({
  selector: 'app-expense-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './expense-list.html',
  styleUrl: './expense-list.scss',
})
export class ExpenseList {
  private readonly service = inject(ExpenseService);
  private readonly categoryService = inject(CategoryService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  protected readonly paymentOptions = Object.entries(PAYMENT_METHOD_LABELS) as [PaymentMethod, string][];
  protected readonly columns = ['expenseDate', 'description', 'category', 'paymentMethod', 'totalAmount', 'actions'];

  protected readonly categories = signal<Category[]>([]);

  protected readonly filters = this.fb.group({
    search: this.fb.nonNullable.control(''),
    categoryId: this.fb.control<number | null>(null),
    paymentMethod: this.fb.control<PaymentMethod | null>(null),
    from: this.fb.control<Date | null>(null),
    to: this.fb.control<Date | null>(null),
  });

  protected readonly page = signal<PageResponse<Expense> | null>(null);
  protected readonly loading = signal(true);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);
  private readonly sort = signal('expenseDate,desc');

  private readonly reload$ = new Subject<void>();

  constructor() {
    this.categoryService
      .list()
      .pipe(takeUntilDestroyed())
      .subscribe({
        next: (categories) => this.categories.set(categories),
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível carregar as categorias.')),
      });

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
              this.notifications.error(errorMessage(error, 'Não foi possível carregar as despesas.'));
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

  paymentLabel(method: PaymentMethod): string {
    return PAYMENT_METHOD_LABELS[method];
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.reload$.next();
  }

  onSort(sort: Sort): void {
    this.sort.set(sort.direction ? `${sort.active},${sort.direction}` : 'expenseDate,desc');
    this.pageIndex.set(0);
    this.reload$.next();
  }

  clearFilters(): void {
    this.filters.reset({ search: '', categoryId: null, paymentMethod: null, from: null, to: null });
  }

  openForm(expense?: Expense): void {
    const data: ExpenseFormData = { expense: expense ?? null, categories: this.categories() };

    this.dialog
      .open(ExpenseFormDialog, { data, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.notifications.success(expense ? 'Despesa atualizada' : 'Despesa registada');
          this.reload$.next();
        }
      });
  }

  delete(expense: Expense): void {
    const data: ConfirmDialogData = {
      title: 'Apagar despesa',
      message: `Tem a certeza de que quer apagar a despesa "${expense.description}"?`,
      confirmLabel: 'Apagar',
    };

    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.service.delete(expense.id)),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Despesa apagada');
          this.reload$.next();
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível apagar a despesa.')),
      });
  }

  private buildQuery() {
    const value = this.filters.getRawValue();
    return {
      search: value.search,
      categoryId: value.categoryId,
      paymentMethod: value.paymentMethod,
      from: toIsoDate(value.from),
      to: toIsoDate(value.to),
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.sort(),
    };
  }
}