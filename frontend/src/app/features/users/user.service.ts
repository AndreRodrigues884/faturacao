import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { User } from '../../core/auth/auth.models';
import { CreateUserRequest, UpdateUserRequest } from './user.models';

/**
 * SERVICE - pedidos à API de gestão de utilizadores (/api/users). Só funciona para ADMIN:
 * para os outros, o backend responde 403.
 *
 * Fala com:     HttpClient
 * É usado por:  UserList, UserFormDialog
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/users';

  list(): Observable<User[]> {
    return this.http.get<User[]>(this.baseUrl);
  }

  create(request: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, request);
  }

  update(id: number, request: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${id}/deactivate`, null);
  }

  activate(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${id}/activate`, null);
  }

  resetPassword(id: number, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/reset-password`, { newPassword });
  }
}