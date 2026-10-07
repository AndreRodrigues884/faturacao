import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { applyServerErrors, errorMessage, problemOf } from '../../../core/api/api-errors';
import { Role, User } from '../../../core/auth/auth.models';
import { ROLE_LABELS } from '../user.models';
import { UserService } from '../user.service';

/**
 * COMPONENTE - janela para criar uma conta (email, nome, password, papel)
 * ou editar uma existente (só nome e papel; o email identifica a conta e não muda).
 *
 * Fala com:     UserService, api-errors
 * É usado por:  UserList
 */
@Component({
  selector: 'app-user-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './user-form-dialog.html',
  styleUrl: './user-form-dialog.scss',
})
export class UserFormDialog {
  private readonly service = inject(UserService);
  private readonly dialogRef = inject<MatDialogRef<UserFormDialog, boolean>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly user = inject<User | null>(MAT_DIALOG_DATA);
  protected readonly roleOptions = Object.entries(ROLE_LABELS) as [Role, string][];

  protected readonly form = this.fb.group({
    email: this.fb.nonNullable.control(this.user?.email ?? '', [
      Validators.required,
      Validators.email,
      Validators.maxLength(255),
    ]),
    name: this.fb.nonNullable.control(this.user?.name ?? '', [Validators.required, Validators.maxLength(150)]),
    password: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(72),
    ]),
    role: this.fb.control<Role | null>(this.user?.role ?? 'USER', Validators.required),
  });

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    if (this.user) {
      this.form.controls.email.disable();
      this.form.controls.password.disable();
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();
    const operation = this.user
      ? this.service.update(this.user.id, { name: value.name, role: value.role! })
      : this.service.create({ email: value.email, name: value.name, password: value.password, role: value.role! });

    operation.subscribe({
      next: () => this.dialogRef.close(true),
      error: (error) => {
        this.saving.set(false);

        if (applyServerErrors(this.form, error)) {
          return;
        }

        const problem = problemOf(error);
        if (problem?.status === 409) {
          this.form.controls.email.setErrors({ server: problem.detail });
          this.form.controls.email.markAsTouched();
          return;
        }

        this.errorMessage.set(errorMessage(error, 'Não foi possível guardar o utilizador.'));
      },
    });
  }
}