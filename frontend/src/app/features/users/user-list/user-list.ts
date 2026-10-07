import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { filter, switchMap } from 'rxjs';

import { errorMessage } from '../../../core/api/api-errors';
import { Role, User } from '../../../core/auth/auth.models';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/confirm-dialog/confirm-dialog';
import { PasswordDialog, PasswordDialogData } from '../../../shared/password-dialog/password-dialog';
import { UserFormDialog } from '../user-form-dialog/user-form-dialog';
import { ROLE_LABELS } from '../user.models';
import { UserService } from '../user.service';

/**
 * COMPONENTE - gestão de contas (só ADMIN): criar, editar nome e papel, desativar e reativar,
 * redefinir password. O admin não vê os botões para se desativar a si próprio.
 *
 * Fala com:     UserService, MatDialog (UserFormDialog, ConfirmDialog, PasswordDialog),
 *               AuthService (quem sou eu?), NotificationService
 * É usado por:  app.routes.ts (rota /utilizadores, protegida pelo adminGuard)
 */
@Component({
  selector: 'app-user-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatProgressBarModule],
  templateUrl: './user-list.html',
  styleUrl: './user-list.scss',
})
export class UserList {
  private readonly service = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  private readonly auth = inject(AuthService);

  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(true);
  protected readonly columns = ['name', 'email', 'role', 'status', 'createdAt', 'actions'];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.service.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.notifications.error(errorMessage(error, 'Não foi possível carregar os utilizadores.'));
      },
    });
  }

  roleLabel(role: Role): string {
    return ROLE_LABELS[role];
  }

  isSelf(user: User): boolean {
    return user.id === this.auth.currentUser()?.id;
  }

  openForm(user?: User): void {
    this.dialog
      .open(UserFormDialog, { data: user ?? null, width: '480px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.notifications.success(user ? 'Utilizador atualizado' : 'Utilizador criado');
          this.load();
        }
      });
  }

  deactivate(user: User): void {
    const data: ConfirmDialogData = {
      title: 'Desativar conta',
      message: `${user.name} deixa de conseguir entrar na aplicação. O histórico (faturas, despesas) mantém-se.`,
      confirmLabel: 'Desativar',
    };

    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.service.deactivate(user.id)),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Conta desativada');
          this.load();
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível desativar a conta.')),
      });
  }

  activate(user: User): void {
    this.service.activate(user.id).subscribe({
      next: () => {
        this.notifications.success('Conta reativada');
        this.load();
      },
      error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível reativar a conta.')),
    });
  }

  resetPassword(user: User): void {
    const data: PasswordDialogData = {
      title: `Redefinir a password de ${user.name}`,
      requireCurrent: false,
      submit: (value) => this.service.resetPassword(user.id, value.newPassword),
    };

    this.dialog
      .open(PasswordDialog, { data, width: '440px' })
      .afterClosed()
      .subscribe((changed) => {
        if (changed) {
          this.notifications.success(`Password de ${user.name} redefinida`);
        }
      });
  }
}