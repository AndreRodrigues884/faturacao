import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Product, ProductRequest } from './product.models';

/**
 * SERVICE - pedidos à API de produtos (/api/products), incluindo desativar e reativar.
 *
 * Fala com:     HttpClient
 * É usado por:  ProductList, ProductFormDialog (e, mais tarde, o formulário das faturas)
 */
@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/products';

  list(includeInactive = false): Observable<Product[]> {
    const params = new HttpParams().set('includeInactive', includeInactive);
    return this.http.get<Product[]>(this.baseUrl, { params });
  }

  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  update(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  activate(id: number): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/${id}/activate`, null);
  }
}