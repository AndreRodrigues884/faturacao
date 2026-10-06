import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Client, ClientRequest } from './client.models';

/**
 * SERVICE - pedidos à API de clientes (/api/clients), incluindo a pesquisa por nome ou NIF.
 *
 * Fala com:     HttpClient
 * É usado por:  ClientList, ClientFormDialog
 */
@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/clients';

  list(search = ''): Observable<Client[]> {
    const term = search.trim();
    const params = term ? new HttpParams().set('search', term) : undefined;
    return this.http.get<Client[]>(this.baseUrl, { params });
  }

  create(request: ClientRequest): Observable<Client> {
    return this.http.post<Client>(this.baseUrl, request);
  }

  update(id: number, request: ClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}