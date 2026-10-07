import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { applyServerErrors, errorMessage, problemOf } from '../../../core/api/api-errors';
import { nifValidator } from '../../../shared/validators/nif.validator';
import { Client, ClientRequest } from '../client.models';
import { ClientService } from '../client.service';

/**
 * COMPONENTE - janela com o formulário de criar ou editar um cliente.
 * Valida o NIF e o código postal no browser; os erros da API aparecem nos campos (409 de NIF repetido no NIF).
 *
 * Fala com:     ClientService, nifValidator, api-errors
 * É usado por:  ClientList
 */
@Component({
  selector: 'app-client-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './client-form-dialog.html',
  styleUrl: './client-form-dialog.scss',
})
export class ClientFormDialog {
  private readonly service = inject(ClientService);
  private readonly dialogRef = inject<MatDialogRef<ClientFormDialog, boolean>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly client = inject<Client | null>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    name: [this.client?.name ?? '', [Validators.required, Validators.maxLength(150)]],
    nif: [this.client?.nif ?? '', [Validators.required, nifValidator()]],
    email: [this.client?.email ?? '', [Validators.email, Validators.maxLength(255)]],
    phone: [this.client?.phone ?? '', Validators.maxLength(20)],
    address: [this.client?.address ?? '', Validators.maxLength(255)],
    postalCode: [this.client?.postalCode ?? '', Validators.pattern(/^\d{4}-\d{3}$/)],
    city: [this.client?.city ?? '', Validators.maxLength(100)],
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
    const request: ClientRequest = {
      name: value.name,
      nif: value.nif,
      email: value.email || null,
      phone: value.phone || null,
      address: value.address || null,
      postalCode: value.postalCode || null,
      city: value.city || null,
    };

    const operation = this.client
      ? this.service.update(this.client.id, request)
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
          this.form.controls.nif.setErrors({ server: problem.detail });
          this.form.controls.nif.markAsTouched();
          return;
        }

        this.errorMessage.set(errorMessage(error, 'Não foi possível guardar o cliente.'));
      },
    });
  }
}