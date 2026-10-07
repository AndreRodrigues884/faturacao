import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  filter,
  merge,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { errorMessage } from '../../../core/api/api-errors';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/confirm-dialog/confirm-dialog';
import { ClientFormDialog } from '../client-form-dialog/client-form-dialog';
import { Client } from '../client.models';
import { ClientService } from '../client.service';

/**
 * COMPONENTE - página de clientes: pesquisa enquanto se escreve, tabela, criar/editar, apagar (só ADMIN).
 *
 * Fala com:     ClientService, MatDialog, NotificationService, AuthService
 * É usado por:  app.routes.ts (rota /clientes)
 */
@Component({
  selector: 'app-client-list',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss',
})
export class ClientList {
  private readonly service = inject(ClientService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly clients = signal<Client[]>([]);
  protected readonly loading = signal(true);
  protected readonly columns = ['name', 'nif', 'email', 'city', 'actions'];

  private readonly reload$ = new Subject<void>();

  constructor() {
    merge(
      this.search.valueChanges.pipe(debounceTime(300), distinctUntilChanged()),
      this.reload$,
    )
      .pipe(
        startWith(null),
        tap(() => this.loading.set(true)),
        switchMap(() =>
          this.service.list(this.search.value).pipe(
            catchError((error) => {
              this.notifications.error(errorMessage(error, 'Não foi possível carregar os clientes.'));
              return of([] as Client[]);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((clients) => {
        this.clients.set(clients);
        this.loading.set(false);
      });
  }

  openForm(client?: Client): void {
    this.dialog
      .open(ClientFormDialog, { data: client ?? null, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.notifications.success(client ? 'Cliente atualizado' : 'Cliente criado');
          this.reload$.next();
        }
      });
  }

  delete(client: Client): void {
    const data: ConfirmDialogData = {
      title: 'Apagar cliente',
      message: `Tem a certeza de que quer apagar o cliente "${client.name}"?`,
      confirmLabel: 'Apagar',
    };

    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.service.delete(client.id)),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Cliente apagado');
          this.reload$.next();
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível apagar o cliente.')),
      });
  }
}