import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * VALIDADOR - NIF português (9 dígitos + dígito de controlo). Mesmo algoritmo do NifValidator do backend.
 * Campo vazio é considerado válido: o "obrigatório" é verificado pelo Validators.required.
 *
 * Fala com:     ninguém (só a regra matemática)
 * É usado por:  formulários com NIF (ClientFormDialog e, mais tarde, despesas)
 */
export function nifValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const nif = String(control.value ?? '').replace(/\s/g, '');

    if (!nif) {
      return null;
    }
    if (!/^\d{9}$/.test(nif)) {
      return { nif: true };
    }

    let sum = 0;
    for (let i = 0; i < 8; i++) {
      sum += Number(nif[i]) * (9 - i);
    }
    const remainder = sum % 11;
    const expectedCheckDigit = remainder < 2 ? 0 : 11 - remainder;

    return expectedCheckDigit === Number(nif[8]) ? null : { nif: true };
  };
}