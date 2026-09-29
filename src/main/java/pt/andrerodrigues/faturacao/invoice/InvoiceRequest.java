package pt.andrerodrigues.faturacao.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO DE ENTRADA - formato do JSON para criar/editar um rascunho de fatura.
 *
 * Fala com:     InvoiceLineRequest (as linhas)
 * É usado por:  InvoiceController (recebe e valida com @Valid),
 *               InvoiceService (lê os dados para criar/alterar)
 */
public record InvoiceRequest(

        @NotNull(message = "O cliente é obrigatório")
        Long clientId,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate dueDate,

        @Size(max = 500, message = "As notas não podem ter mais de 500 caracteres")
        String notes,

        @NotEmpty(message = "A fatura tem de ter pelo menos uma linha")
        @Valid
        List<InvoiceLineRequest> lines
) {
}