import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/ui/notification.service';
import { PasswordDialog, PasswordDialogData } from '../../shared/password-dialog/password-dialog';

/**
 * COMPONENTE - o esqueleto das páginas autenticadas: menu lateral (com Utilizadores só para ADMIN),
 * barra de topo com o nome, "Mudar password" e "Sair", e o espaço do conteúdo.
 *
 * Fala com:     AuthService (nome, papel, logout, mudar password), MatDialog, NotificationService
 * É usado por:  app.routes.ts (rota '' com children)
 */
@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatButtonModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  protected readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);

  changePassword(): void {
    const data: PasswordDialogData = {
      title: 'Mudar a minha password',
      requireCurrent: true,
      submit: (value) => this.auth.changePassword(value.currentPassword, value.newPassword),
    };

    this.dialog
      .open(PasswordDialog, { data, width: '440px' })
      .afterClosed()
      .subscribe((changed) => {
        if (changed) {
          this.notifications.success('Password alterada');
        }
      });
  }
}