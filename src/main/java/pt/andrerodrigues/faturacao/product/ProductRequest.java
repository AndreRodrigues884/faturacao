package pt.andrerodrigues.faturacao.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO DE ENTRADA - formato do JSON recebido no POST e PUT de produtos, com as validações.
 *
 * Fala com:     ProductType, VatRate (enums)
 * É usado por:  ProductController (recebe e valida com @Valid),
 *               ProductService (lê os dados para criar/alterar)
 */
public record ProductRequest(

        @NotBlank(message = "O código é obrigatório")
        @Size(max = 30, message = "O código não pode ter mais de 30 caracteres")
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "O código só pode ter letras, números, hífen e underscore")
        String code,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode ter mais de 150 caracteres")
        String name,

        @Size(max = 500, message = "A descrição não pode ter mais de 500 caracteres")
        String description,

        @NotNull(message = "O tipo é obrigatório (PRODUCT ou SERVICE)")
        ProductType type,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "O preço pode ter no máximo 10 dígitos inteiros e 2 casas decimais")
        BigDecimal unitPrice,

        @NotNull(message = "A taxa de IVA é obrigatória (NORMAL, INTERMEDIATE ou REDUCED)")
        VatRate vatRate
) {
}