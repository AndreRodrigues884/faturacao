import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { applyServerErrors, errorMessage } from '../../../core/api/api-errors';
import { fromIsoDate, toIsoDate } from '../../../shared/utils/date';
import { nifValidator } from '../../../shared/validators/nif.validator';
import { Category } from '../../categories/category.models';
import { PAYMENT_METHOD_LABELS, Expense, ExpenseRequest, PaymentMethod } from '../expense.models';
import { ExpenseService } from '../expense.service';

/**
 * COMPONENTE - janela com o formulário de criar ou editar uma despesa.
 * Valida o NIF do fornecedor, a data (não pode ser futura) e, entre campos, que o IVA não excede o total.
 * Mostra a base sem IVA (total - IVA) enquanto se escreve.
 *
 * Fala com:     ExpenseService, nifValidator, api-errors
 * É usado por:  ExpenseList (passa a despesa a editar e a lista de categorias)
 */

export interface ExpenseFormData {
  expense: Expense | null;
  categories: Category[];
}

/** Validador de GRUPO: compara dois campos do formulário. */
function vatNotGreaterThanTotal(group: AbstractControl): ValidationErrors | null {
  const total = group.get('totalAmount')?.value as number | null;
  const vat = group.get('vatAmount')?.value as number | null;
  if (total === null || vat === null) {
    return null;
  }
  return Math.round(vat * 100) > Math.round(total * 100) ? { vatGreaterThanTotal: true } : null;
}

@Component({
  selector: 'app-expense-form-dialog',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatButtonModule,
  ],
  templateUrl: './expense-form-dialog.html',
  styleUrl: './expense-form-dialog.scss',
})
export class ExpenseFormDialog {
  private readonly service = inject(ExpenseService);
  private readonly dialogRef = inject<MatDialogRef<ExpenseFormDialog, boolean>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly data = inject<ExpenseFormData>(MAT_DIALOG_DATA);
  protected readonly expense = this.data.expense;
  protected readonly today = new Date();
  protected readonly paymentOptions = Object.entries(PAYMENT_METHOD_LABELS) as [PaymentMethod, string][];

  protected readonly form = this.fb.group(
    {
      categoryId: this.fb.control<number | null>(this.expense?.category.id ?? null, Validators.required),
      description: this.fb.nonNullable.control(this.expense?.description ?? '', [
        Validators.required,
        Validators.maxLength(255),
      ]),
      supplierName: this.fb.nonNullable.control(this.expense?.supplierName ?? '', Validators.maxLength(150)),
      supplierNif: this.fb.nonNullable.control(this.expense?.supplierNif ?? '', nifValidator()),
      expenseDate: this.fb.control<Date | null>(
        this.expense ? fromIsoDate(this.expense.expenseDate) : new Date(),
        Validators.required,
      ),
      totalAmount: this.fb.control<number | null>(this.expense?.totalAmount ?? null, [
        Validators.required,
        Validators.min(0.01),
      ]),
      vatAmount: this.fb.control<number | null>(this.expense?.vatAmount ?? null, Validators.min(0)),
      paymentMethod: this.fb.control<PaymentMethod | null>(this.expense?.paymentMethod ?? null, Validators.required),
      notes: this.fb.nonNullable.control(this.expense?.notes ?? '', Validators.maxLength(500)),
    },
    { validators: vatNotGreaterThanTotal },
  );

  private readonly formValue = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  /** Base sem IVA, em cêntimos inteiros (total - IVA), ou null se o total ainda não foi preenchido. */
  protected readonly netCents = computed(() => {
    const { totalAmount, vatAmount } = this.formValue();
    if (totalAmount === null || totalAmount === undefined) {
      return null;
    }
    return Math.round(totalAmount * 100) - Math.round((vatAmount ?? 0) * 100);
  });

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();
    const request: ExpenseRequest = {
      categoryId: value.categoryId!,
      description: value.description,
      supplierName: value.supplierName || null,
      supplierNif: value.supplierNif || null,
      expenseDate: toIsoDate(value.expenseDate)!,
      totalAmount: value.totalAmount!,
      vatAmount: value.vatAmount,
      paymentMethod: value.paymentMethod!,
      notes: value.notes || null,
    };

    const operation = this.expense
      ? this.service.update(this.expense.id, request)
      : this.service.create(request);

    operation.subscribe({
      next: () => this.dialogRef.close(true),
      error: (error) => {
        this.saving.set(false);
        if (applyServerErrors(this.form, error)) {
          return;
        }
        this.errorMessage.set(errorMessage(error, 'Não foi possível guardar a despesa.'));
      },
    });
  }
}