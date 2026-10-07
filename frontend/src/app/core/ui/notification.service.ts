import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * SERVICE - notificações breves no fundo do ecrã (ex: "Categoria criada").
 *
 * Fala com:     MatSnackBar (Angular Material)
 * É usado por:  componentes, depois de criar, alterar, apagar, ou quando algo falha
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.snackBar.open(message, 'OK', { duration: 3000 });
  }

  error(message: string): void {
    this.snackBar.open(message, 'Fechar', { duration: 6000 });
  }
}