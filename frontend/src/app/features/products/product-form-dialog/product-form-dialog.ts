import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { applyServerErrors, errorMessage, problemOf } from '../../../core/api/api-errors';
import {
  PRODUCT_TYPE_LABELS,
  Product,
  ProductRequest,
  ProductType,
  VAT_RATE_LABELS,
  VatRate,
} from '../product.models';
import { ProductService } from '../product.service';

/**
 * COMPONENTE - janela com o formulário de criar ou editar um produto ou serviço.
 * Tipo e taxa de IVA escolhem-se numa lista; o 409 (código repetido) aparece no campo do código.
 *
 * Fala com:     ProductService, api-errors
 * É usado por:  ProductList
 */
@Component({
  selector: 'app-product-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './product-form-dialog.html',
  styleUrl: './product-form-dialog.scss',
})
export class ProductFormDialog {
  private readonly service = inject(ProductService);
  private readonly dialogRef = inject<MatDialogRef<ProductFormDialog, boolean>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly product = inject<Product | null>(MAT_DIALOG_DATA);

  protected readonly typeOptions = Object.entries(PRODUCT_TYPE_LABELS) as [ProductType, string][];
  protected readonly vatRateOptions = Object.entries(VAT_RATE_LABELS) as [VatRate, string][];

  protected readonly form = this.fb.nonNullable.group({
    code: [
      this.product?.code ?? '',
      [Validators.required, Validators.maxLength(30), Validators.pattern(/^[A-Za-z0-9_-]+$/)],
    ],
    name: [this.product?.name ?? '', [Validators.required, Validators.maxLength(150)]],
    description: [this.product?.description ?? '', Validators.maxLength(500)],
    type: this.fb.control<ProductType | null>(this.product?.type ?? null, Validators.required),
    unitPrice: this.fb.control<number | null>(this.product?.unitPrice ?? null, [
      Validators.required,
      Validators.min(0),
    ]),
    vatRate: this.fb.control<VatRate | null>(this.product?.vatRate ?? null, Validators.required),
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
    const request: ProductRequest = {
      code: value.code,
      name: value.name,
      description: value.description || null,
      type: value.type!,
      unitPrice: value.unitPrice!,
      vatRate: value.vatRate!,
    };

    const operation = this.product
      ? this.service.update(this.product.id, request)
      : this.service.create(request);

    operation.subscribe({
      next: () => this.dialogRef.close(true),
      error: (error) => {
        this.saving.set(false);

        if (applyServerErrors(this.form, error)) {
          return;
        }

        const problem = problemOf(error);
        if (problem?.status === 409) {
          this.form.controls.code.setErrors({ server: problem.detail });
          this.form.controls.code.markAsTouched();
          return;
        }

        this.errorMessage.set(errorMessage(error, 'Não foi possível guardar o produto.'));
      },
    });
  }
}