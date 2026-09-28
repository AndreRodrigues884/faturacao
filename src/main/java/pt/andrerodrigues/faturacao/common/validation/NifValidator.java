/**
 * VALIDADOR - verifica se um texto é um NIF português válido (9 dígitos + dígito de controlo).
 *
 * Fala com:     ninguém (é só a regra matemática)
 * É usado por:  a anotação @ValidNif (o Spring chama-o automaticamente durante o @Valid)
 */

package pt.andrerodrigues.faturacao.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class NifValidator implements ConstraintValidator<ValidNif, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // campo vazio é problema do @NotBlank, não deste validador
        }
        return isValidNif(value);
    }

    public static boolean isValidNif(String value) {
        String nif = value.replaceAll("\\s", "");

        if (!nif.matches("\\d{9}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(nif.charAt(i));
            sum += digit * (9 - i);
        }

        int remainder = sum % 11;
        int expectedCheckDigit = remainder < 2 ? 0 : 11 - remainder;
        int actualCheckDigit = Character.getNumericValue(nif.charAt(8));

        return expectedCheckDigit == actualCheckDigit;
    }
}