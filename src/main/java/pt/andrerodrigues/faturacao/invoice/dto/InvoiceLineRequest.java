package pt.andrerodrigues.faturacao.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO DE ENTRADA - uma linha dentro do JSON de criação/edição de fatura.
 *
 * Fala com:     ninguém
 * É usado por:  InvoiceRequest (lista de linhas), InvoiceService (lê produto e quantidade)
 */
public record InvoiceLineRequest(

        @NotNull(message = "O produto é obrigatório")
        Long productId,

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "A quantidade tem de ser maior que zero")
        @Digits(integer = 7, fraction = 3, message = "A quantidade pode ter no máximo 3 casas decimais")
        BigDecimal quantity
) {
}