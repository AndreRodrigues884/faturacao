package pt.andrerodrigues.faturacao.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pt.andrerodrigues.faturacao.common.validation.ValidNif;

/**
 * DTO DE ENTRADA - formato do JSON recebido no POST e PUT de clientes, com as validações.
 *
 * Fala com:     ValidNif (validação do NIF, pasta common/validation)
 * É usado por:  ClientController (recebe e valida com @Valid),
 *               ClientService (lê os dados para criar/alterar)
 */
public record ClientRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode ter mais de 150 caracteres")
        String name,

        @NotBlank(message = "O NIF é obrigatório")
        @ValidNif
        String nif,

        @Email(message = "O email não é válido")
        @Size(max = 255, message = "O email não pode ter mais de 255 caracteres")
        String email,

        @Size(max = 20, message = "O telefone não pode ter mais de 20 caracteres")
        String phone,

        @Size(max = 255, message = "A morada não pode ter mais de 255 caracteres")
        String address,

        @Pattern(regexp = "\\d{4}-\\d{3}", message = "O código postal deve ter o formato 0000-000")
        String postalCode,

        @Size(max = 100, message = "A cidade não pode ter mais de 100 caracteres")
        String city
) {
}