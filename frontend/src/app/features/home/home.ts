import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';

import { AuthService } from '../../core/auth/auth.service';

/**
 * COMPONENTE - página inicial. Por agora, cumprimenta o utilizador e mostra quantas categorias existem,
 * o que prova que o interceptor está a enviar o token (o endpoint exige autenticação).
 *
 * Fala com:     AuthService (nome e papel), HttpClient (GET /api/categories)
 * É usado por:  app.routes.ts (rota /inicio)
 */
@Component({
  selector: 'app-home',
  imports: [MatCardModule],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  protected readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);

  protected readonly categories = toSignal(
    this.http.get<{ id: number; name: string }[]>('/api/categories'),
    { initialValue: [] },
  );
}