import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

/**
 * COMPONENTE - ecrã de login: formulário de email e password.
 * Em caso de sucesso vai para /inicio; em caso de erro mostra a mensagem.
 *
 * Fala com:     AuthService (login), Router (navegar depois do login)
 * É usado por:  app.routes.ts (rota /login)
 */
@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/inicio']),
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          error.status === 401
            ? 'Email ou password inválidos'
            : 'Não foi possível ligar ao servidor. Tente novamente.',
        );
      },
    });
  }
}