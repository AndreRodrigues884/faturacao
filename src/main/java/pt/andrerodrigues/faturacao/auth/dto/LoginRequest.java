package pt.andrerodrigues.faturacao.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO DE ENTRADA - credenciais de login.
 *
 * Fala com:     ninguém
 * É usado por:  AuthController, AuthService
 */
public record LoginRequest(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email não é válido")
        String email,

        @NotBlank(message = "A password é obrigatória")
        String password
) {
}