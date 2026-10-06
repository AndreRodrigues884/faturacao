import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

/**
 * COMPONENTE REUTILIZÁVEL - janela "Tem a certeza?". Fecha com true (confirmou) ou false (cancelou).
 *
 * Fala com:     ninguém (recebe o texto por MAT_DIALOG_DATA)
 * É usado por:  qualquer ecrã com ações destrutivas (ex: CategoryList ao apagar)
 */

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './confirm-dialog.html',
})
export class ConfirmDialog {
  protected readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}