import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Category, CategoryRequest } from './category.models';

/**
 * SERVICE - pedidos à API de categorias (/api/categories).
 * É o "cliente" da API: não tem regras de negócio, só faz os pedidos HTTP.
 *
 * Fala com:     HttpClient (o token é acrescentado pelo authInterceptor)
 * É usado por:  CategoryList, CategoryFormDialog
 */
@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/categories';

  list(): Observable<Category[]> {
    return this.http.get<Category[]>(this.baseUrl);
  }

  create(request: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.baseUrl, request);
  }

  update(id: number, request: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}