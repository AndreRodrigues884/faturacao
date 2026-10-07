import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PageResponse } from '../../core/api/page';
import { Expense, ExpenseQuery, ExpenseRequest } from './expense.models';

/**
 * SERVICE - pedidos à API de despesas (/api/expenses): listagem paginada com filtros e CRUD.
 *
 * Fala com:     HttpClient
 * É usado por:  ExpenseList, ExpenseFormDialog
 */
@Injectable({ providedIn: 'root' })
export class ExpenseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/expenses';

  search(query: ExpenseQuery): Observable<PageResponse<Expense>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sort', query.sort);

    if (query.search.trim()) {
      params = params.set('search', query.search.trim());
    }
    if (query.categoryId !== null) {
      params = params.set('categoryId', query.categoryId);
    }
    if (query.paymentMethod) {
      params = params.set('paymentMethod', query.paymentMethod);
    }
    if (query.from) {
      params = params.set('from', query.from);
    }
    if (query.to) {
      params = params.set('to', query.to);
    }

    return this.http.get<PageResponse<Expense>>(this.baseUrl, { params });
  }

  create(request: ExpenseRequest): Observable<Expense> {
    return this.http.post<Expense>(this.baseUrl, request);
  }

  update(id: number, request: ExpenseRequest): Observable<Expense> {
    return this.http.put<Expense>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}