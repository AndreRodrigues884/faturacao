package pt.andrerodrigues.faturacao.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO DE ENTRADA - motivo da anulação de uma fatura (obrigatório).
 *
 * Fala com:     ninguém
 * É usado por:  InvoiceController, InvoiceService
 */
public record CancelRequest(

        @NotBlank(message = "O motivo da anulação é obrigatório")
        @Size(max = 255, message = "O motivo não pode ter mais de 255 caracteres")
        String reason
) {
}