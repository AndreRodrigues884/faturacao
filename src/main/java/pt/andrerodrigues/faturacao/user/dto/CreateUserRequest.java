package pt.andrerodrigues.faturacao.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.andrerodrigues.faturacao.user.domain.Role;

/**
 * DTO DE ENTRADA - dados para o admin criar uma conta.
 *
 * Fala com:     Role
 * É usado por:  UserController, UserService
 */
public record CreateUserRequest(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email não é válido")
        @Size(max = 255, message = "O email não pode ter mais de 255 caracteres")
        String email,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode ter mais de 150 caracteres")
        String name,

        @NotBlank(message = "A password é obrigatória")
        @Size(min = 8, max = 72, message = "A password tem de ter entre 8 e 72 caracteres")
        String password,

        @NotNull(message = "O papel é obrigatório (ADMIN ou USER)")
        Role role
) {
}