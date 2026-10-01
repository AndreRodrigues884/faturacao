package pt.andrerodrigues.faturacao.invoice.dto;

import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * DTO DE ENTRADA - dados para marcar uma fatura como paga. O corpo é opcional:
 * sem data, assume-se hoje.
 *
 * Fala com:     ninguém
 * É usado por:  InvoiceController, InvoiceService
 */
public record PaymentRequest(

        @PastOrPresent(message = "A data de pagamento não pode ser no futuro")
        LocalDate paidDate
) {
}