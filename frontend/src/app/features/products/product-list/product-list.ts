import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { filter, switchMap } from 'rxjs';

import { errorMessage } from '../../../core/api/api-errors';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/ui/notification.service';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/confirm-dialog/confirm-dialog';
import { ProductFormDialog } from '../product-form-dialog/product-form-dialog';
import { ProductService } from '../product.service';
import { PRODUCT_TYPE_LABELS, Product, ProductType } from '../product.models';

/**
 * COMPONENTE - página de produtos e serviços: tabela com preços em euros, criar/editar,
 * mostrar inativos, desativar e reativar (só ADMIN).
 *
 * Fala com:     ProductService, MatDialog, NotificationService, AuthService
 * É usado por:  app.routes.ts (rota /produtos)
 */
@Component({
  selector: 'app-product-list',
  imports: [CurrencyPipe, MatTableModule, MatButtonModule, MatProgressBarModule, MatSlideToggleModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
})
export class ProductList {
  private readonly service = inject(ProductService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  protected typeLabel(type: ProductType): string {
    return PRODUCT_TYPE_LABELS[type];
  }
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(true);
  protected readonly includeInactive = signal(false);
  protected readonly columns = ['code', 'name', 'type', 'unitPrice', 'vat', 'priceWithVat', 'actions'];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.service.list(this.includeInactive()).subscribe({
      next: (products) => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.notifications.error(errorMessage(error, 'Não foi possível carregar os produtos.'));
      },
    });
  }

  toggleInactive(checked: boolean): void {
    this.includeInactive.set(checked);
    this.load();
  }

  openForm(product?: Product): void {
    this.dialog
      .open(ProductFormDialog, { data: product ?? null, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.notifications.success(product ? 'Produto atualizado' : 'Produto criado');
          this.load();
        }
      });
  }

  deactivate(product: Product): void {
    const data: ConfirmDialogData = {
      title: 'Desativar produto',
      message: `O produto "${product.name}" deixa de poder ser usado em faturas novas. As faturas antigas não são afetadas.`,
      confirmLabel: 'Desativar',
    };

    this.dialog
      .open(ConfirmDialog, { data })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed === true),
        switchMap(() => this.service.deactivate(product.id)),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Produto desativado');
          this.load();
        },
        error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível desativar o produto.')),
      });
  }

  activate(product: Product): void {
    this.service.activate(product.id).subscribe({
      next: () => {
        this.notifications.success('Produto reativado');
        this.load();
      },
      error: (error) => this.notifications.error(errorMessage(error, 'Não foi possível reativar o produto.')),
    });
  }
}