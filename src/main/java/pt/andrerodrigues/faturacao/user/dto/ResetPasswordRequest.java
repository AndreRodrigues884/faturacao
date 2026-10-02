package pt.andrerodrigues.faturacao.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO DE ENTRADA - o admin define uma password nova para uma conta (ex: o utilizador esqueceu-se).
 *
 * Fala com:     ninguém
 * É usado por:  UserController, UserService
 */
public record ResetPasswordRequest(

        @NotBlank(message = "A password é obrigatória")
        @Size(min = 8, max = 72, message = "A password tem de ter entre 8 e 72 caracteres")
        String newPassword
) {
}