package pt.andrerodrigues.faturacao.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import pt.andrerodrigues.faturacao.common.validation.ValidNif;
import pt.andrerodrigues.faturacao.expense.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO DE ENTRADA - formato do JSON para criar/editar uma despesa, com as validações.
 *
 * Fala com:     ValidNif (NIF do fornecedor), PaymentMethod
 * É usado por:  ExpenseController (recebe e valida), ExpenseService (cria/altera)
 */
public record ExpenseRequest(

        @NotNull(message = "A categoria é obrigatória")
        Long categoryId,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição não pode ter mais de 255 caracteres")
        String description,

        @Size(max = 150, message = "O nome do fornecedor não pode ter mais de 150 caracteres")
        String supplierName,

        @ValidNif
        String supplierNif,

        @NotNull(message = "A data da despesa é obrigatória")
        @PastOrPresent(message = "A data da despesa não pode ser no futuro")
        LocalDate expenseDate,

        @NotNull(message = "O total é obrigatório")
        @DecimalMin(value = "0.01", message = "O total tem de ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "O total pode ter no máximo 2 casas decimais")
        BigDecimal totalAmount,

        @DecimalMin(value = "0.00", message = "O IVA não pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "O IVA pode ter no máximo 2 casas decimais")
        BigDecimal vatAmount,

        @NotNull(message = "A forma de pagamento é obrigatória")
        PaymentMethod paymentMethod,

        @Size(max = 500, message = "As notas não podem ter mais de 500 caracteres")
        String notes
) {
}