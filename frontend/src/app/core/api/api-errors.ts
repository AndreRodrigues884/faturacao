import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';

import { ProblemDetail } from './problem-detail';

/**
 * UTILITÁRIOS - ler os erros da API (ProblemDetail) e aplicá-los aos formulários.
 *
 * Fala com:     ProblemDetail
 * É usado por:  todos os componentes que fazem pedidos e mostram erros (ex: CategoryFormDialog, CategoryList)
 */

/** Devolve o ProblemDetail do erro, se a API o enviou. */
export function problemOf(error: unknown): ProblemDetail | null {
  if (error instanceof HttpErrorResponse && error.error && typeof error.error === 'object') {
    return error.error as ProblemDetail;
  }
  return null;
}

/** Mensagem legível de um erro: o "detail" da API, ou uma mensagem por defeito. */
export function errorMessage(error: unknown, fallback = 'Ocorreu um erro inesperado.'): string {
  if (error instanceof HttpErrorResponse && error.status === 0) {
    return 'Não foi possível ligar ao servidor.';
  }
  return problemOf(error)?.detail ?? fallback;
}

/**
 * Coloca os erros de validação da API (o mapa "errors" do 400) nos campos do formulário.
 * Ex: { "name": "O nome é obrigatório" } aparece por baixo do campo "name".
 * Devolve true se aplicou pelo menos um erro.
 */
export function applyServerErrors(form: FormGroup, error: unknown): boolean {
  const errors = problemOf(error)?.errors;
  if (!errors) {
    return false;
  }

  let applied = false;
  for (const [field, message] of Object.entries(errors)) {
    // "lines[0].quantity" (formato do Spring) -> "lines.0.quantity" (formato do Angular)
    const control = form.get(field.replace(/\[(\d+)\]/g, '.$1'));
    if (control) {
      control.setErrors({ server: message });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}