import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PageResponse } from '../../core/api/page';
import { Invoice, InvoiceQuery, InvoiceRequest, InvoiceSummary } from './invoice.models';

/**
 * SERVICE - pedidos à API de faturas (/api/invoices): listagem paginada com filtros,
 * CRUD do rascunho e as ações do ciclo de vida (emitir, pagar, anular).
 *
 * Fala com:     HttpClient
 * É usado por:  InvoiceList, InvoiceDetail (e o formulário, a seguir)
 */
@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/invoices';

  search(query: InvoiceQuery): Observable<PageResponse<InvoiceSummary>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sort', query.sort);

    if (query.search.trim()) {
      params = params.set('search', query.search.trim());
    }
    if (query.status) {
      params = params.set('status', query.status);
    }
    if (query.overdue) {
      params = params.set('overdue', true);
    }
    if (query.issuedFrom) {
      params = params.set('issuedFrom', query.issuedFrom);
    }
    if (query.issuedTo) {
      params = params.set('issuedTo', query.issuedTo);
    }

    return this.http.get<PageResponse<InvoiceSummary>>(this.baseUrl, { params });
  }

  get(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.baseUrl}/${id}`);
  }

  create(request: InvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.baseUrl, request);
  }

  update(id: number, request: InvoiceRequest): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  issue(id: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.baseUrl}/${id}/issue`, null);
  }

  pay(id: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.baseUrl}/${id}/pay`, null);
  }

  cancel(id: number, reason: string): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.baseUrl}/${id}/cancel`, { reason });
  }
}