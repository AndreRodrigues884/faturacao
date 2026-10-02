package pt.andrerodrigues.faturacao.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.andrerodrigues.faturacao.user.domain.Role;

/**
 * DTO DE ENTRADA - o admin altera o nome e o papel de uma conta.
 * O email não muda (identifica a conta); a password tem o seu próprio endpoint.
 *
 * Fala com:     Role
 * É usado por:  UserController, UserService
 */
public record UpdateUserRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode ter mais de 150 caracteres")
        String name,

        @NotNull(message = "O papel é obrigatório (ADMIN ou USER)")
        Role role
) {
}