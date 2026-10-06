import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { filter, switchMap } from 'rxjs';

import { errorMessage } from '../../../core/api/api-errors';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/confirm-dialog/confirm-dialog';
import { CategoryFormDialog } from '../category-form-dialog/category-form-dialog';
import { Category } from '../category.models';
import { CategoryService } from '../category.service';

/**
 * COMPONENTE - página de categorias: tabela, criar/editar numa janela, apagar (só ADMIN).
 *
 * Fala com:     CategoryService, MatDialog (abre CategoryFormDialog e ConfirmDialog),
 *               NotificationService, AuthService (é admin?)
 * É usado por:  app.routes.ts (rota /categorias)
 */
@Component({
  selector: 'app-category-list',
  imports: [MatTableModule, MatButtonModule, MatProgressBarModule],
  templateUrl: './category-list.html',
  styleUrl: './category-list.scss',
})
export class CategoryList {
  private readonly service = inject(CategoryService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected readonly categories = signal<Category[]>([]);
  protected readonly loading = signal(true);
  protected readonly columns = ['name', 'description', 'actions'];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.service.list().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.notifications.error(errorMessage(error, 'Não foi possível carregar as categorias.'));
      },
    });
  }

  openForm(category?: Category): void {
    this.dialog
      .open(CategoryFormDialog, { data: category ?? null, width: '480px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.notifications.success(category ? 'Categoria atualizada' : 'Categoria criada');
          this.load();
        }
      });
  }

  delete(category: Category): void {
    const data: ConfirmDialogData = {
      title: 'Apagar categoria',
      message: `Tem a certeza de que quer apagar a categoria "${category.name}"?`,
      confirmLabel: 'Apagar',
    };

    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.service.delete(category.id)),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Categoria apagada');
          this.load();
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível apagar a categoria.')),
      });
  }
}