import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

/**
 * COMPONENTE REUTILIZÁVEL - janela que pede um texto obrigatório (ex: motivo da anulação).
 * Fecha com o texto escrito, ou sem valor se o utilizador cancelar.
 *
 * Fala com:     ninguém (recebe os textos por MAT_DIALOG_DATA)
 * É usado por:  InvoiceDetail (anular) e qualquer ação que precise de uma justificação
 */

export interface PromptDialogData {
  title: string;
  message?: string;
  label: string;
  confirmLabel?: string;
  maxLength?: number;
}

@Component({
  selector: 'app-prompt-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './prompt-dialog.html',
  styleUrl: './prompt-dialog.scss',
})
export class PromptDialog {
  protected readonly data = inject<PromptDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject<MatDialogRef<PromptDialog, string>>(MatDialogRef);

  protected readonly text = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(this.data.maxLength ?? 255)],
  });

  confirm(): void {
    if (this.text.invalid || !this.text.value.trim()) {
      this.text.markAsTouched();
      return;
    }
    this.dialogRef.close(this.text.value.trim());
  }
}