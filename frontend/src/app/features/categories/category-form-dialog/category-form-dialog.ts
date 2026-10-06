import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { applyServerErrors, errorMessage, problemOf } from '../../../core/api/api-errors';
import { Category, CategoryRequest } from '../category.models';
import { CategoryService } from '../category.service';

/**
 * COMPONENTE - janela com o formulário de criar ou editar uma categoria.
 * Recebe a categoria a editar (ou null para criar). Fecha com true quando gravou com sucesso.
 * Os erros da API aparecem nos campos: 400 no campo indicado, 409 (nome repetido) no nome.
 *
 * Fala com:     CategoryService, api-errors
 * É usado por:  CategoryList (abre esta janela)
 */
@Component({
  selector: 'app-category-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './category-form-dialog.html',
  styleUrl: './category-form-dialog.scss',
})
export class CategoryFormDialog {
  private readonly service = inject(CategoryService);
  private readonly dialogRef = inject<MatDialogRef<CategoryFormDialog, boolean>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly category = inject<Category | null>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    name: [this.category?.name ?? '', [Validators.required, Validators.maxLength(80)]],
    description: [this.category?.description ?? '', Validators.maxLength(255)],
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
    const request: CategoryRequest = {
      name: value.name,
      description: value.description || null,
    };

    const operation = this.category
      ? this.service.update(this.category.id, request)
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
          this.form.controls.name.setErrors({ server: problem.detail });
          this.form.controls.name.markAsTouched();
          return;
        }

        this.errorMessage.set(errorMessage(error, 'Não foi possível guardar a categoria.'));
      },
    });
  }
}