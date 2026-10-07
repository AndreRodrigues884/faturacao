import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Observable } from 'rxjs';

import { applyServerErrors, errorMessage } from '../../core/api/api-errors';

/**
 * COMPONENTE REUTILIZÁVEL - janela para definir uma password nova (com confirmação).
 * Quem a abre passa a AÇÃO a executar (submit), por isso a mesma janela serve para:
 *   - o utilizador mudar a sua password (pede também a atual)
 *   - o admin redefinir a password de outra pessoa
 *
 * Fala com:     a função submit que recebe, api-errors
 * É usado por:  Shell (mudar a minha password), UserList (redefinir a de outro utilizador)
 */

export interface PasswordDialogData {
  title: string;
  requireCurrent: boolean;
  submit: (value: { currentPassword: string; newPassword: string }) => Observable<unknown>;
}

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value as string;
  const confirmation = group.get('confirmPassword')?.value as string;
  return password && confirmation && password !== confirmation ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-password-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './password-dialog.html',
  styleUrl: './password-dialog.scss',
})
export class PasswordDialog {
  protected readonly data = inject<PasswordDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject<MatDialogRef<PasswordDialog, boolean>>(MatDialogRef);
  private readonly fb = inject(FormBuilder);

  protected readonly form = this.fb.nonNullable.group(
    {
      currentPassword: ['', this.data.requireCurrent ? Validators.required : []],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

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
    this.data.submit({ currentPassword: value.currentPassword, newPassword: value.newPassword }).subscribe({
      next: () => this.dialogRef.close(true),
      error: (error) => {
        this.saving.set(false);
        if (applyServerErrors(this.form, error)) {
          return;
        }
        this.errorMessage.set(errorMessage(error, 'Não foi possível alterar a password.'));
      },
    });
  }
}