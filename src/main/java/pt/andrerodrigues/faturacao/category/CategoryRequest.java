/**
 * DTO DE ENTRADA - formato do JSON recebido no POST e PUT, com as validações.
 *
 * Fala com:     ninguém (só transporta dados)
 * É usado por:  CategoryController (recebe e valida com @Valid),
 *               CategoryService (lê os dados para criar/alterar)
 */

package pt.andrerodrigues.faturacao.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Request object for creating or updating a category.
 */
public record CategoryRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 80, message = "O nome não pode ter mais de 80 caracteres")
        String name,

        @Size(max = 255, message = "A descrição não pode ter mais de 255 caracteres")
        String description
) {
}