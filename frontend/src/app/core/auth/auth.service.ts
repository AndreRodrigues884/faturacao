import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { LoginRequest, LoginResponse, User } from './auth.models';

/**
 * SERVICE - sessão do utilizador: login, logout, token e dados de quem está autenticado.
 * Guarda a sessão no localStorage para sobreviver a um refresh da página.
 *
 * Fala com:     HttpClient (POST /api/auth/login), Router (volta ao login no logout)
 * É usado por:  Login (fazer login), authInterceptor (ler o token),
 *               guards (está autenticado? é admin?), Shell e Home (nome e papel)
 */

const STORAGE_KEY = 'faturacao.session';

interface Session {
  token: string;
  expiresAt: number;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(this.restoreSession());

  readonly currentUser = computed(() => this.session()?.user ?? null);
  readonly isAdmin = computed(() => this.session()?.user.role === 'ADMIN');

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', credentials).pipe(
      tap((response) => this.saveSession(response)),
    );
  }

  logout(): void {
    this.clearSession();
    this.router.navigate(['/login']);
  }

  /** Devolve o token, ou null se não houver sessão ou se já expirou. */
  token(): string | null {
    const session = this.session();
    if (!session) {
      return null;
    }
    if (Date.now() >= session.expiresAt) {
      this.clearSession();
      return null;
    }
    return session.token;
  }

  hasValidSession(): boolean {
    return this.token() !== null;
  }

  // ---------- Persistência ----------

  private saveSession(response: LoginResponse): void {
    const session: Session = {
      token: response.accessToken,
      expiresAt: Date.now() + response.expiresIn * 1000,
      user: response.user,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  private clearSession(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
  }

  private restoreSession(): Session | null {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (!stored) {
        return null;
      }
      const session = JSON.parse(stored) as Session;
      if (Date.now() >= session.expiresAt) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return session;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}