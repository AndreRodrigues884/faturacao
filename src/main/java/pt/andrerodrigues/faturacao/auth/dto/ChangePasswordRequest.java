package pt.andrerodrigues.faturacao.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO DE ENTRADA - o próprio utilizador muda a sua password (tem de confirmar a atual).
 *
 * Fala com:     ninguém
 * É usado por:  AuthController, AuthService
 */
public record ChangePasswordRequest(

        @NotBlank(message = "A password atual é obrigatória")
        String currentPassword,

        @NotBlank(message = "A nova password é obrigatória")
        @Size(min = 8, max = 72, message = "A nova password tem de ter entre 8 e 72 caracteres")
        String newPassword
) {
}