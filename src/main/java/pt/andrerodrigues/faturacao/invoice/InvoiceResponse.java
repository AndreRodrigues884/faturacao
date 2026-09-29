package pt.andrerodrigues.faturacao.invoice;

import pt.andrerodrigues.faturacao.client.Client;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO DE SAÍDA - formato do JSON devolvido para uma fatura, com cliente resumido e linhas.
 *
 * Fala com:     Invoice (lê os seus dados), InvoiceLineResponse (converte as linhas),
 *               Client (lê o resumo do cliente)
 * É usado por:  InvoiceService (cria-o a partir da entidade),
 *               InvoiceController (devolve-o ao cliente)
 */
public record InvoiceResponse(
        Long id,
        InvoiceStatus status,
        ClientSummary client,
        LocalDate dueDate,
        String notes,
        List<InvoiceLineResponse> lines,
        BigDecimal totalNet,
        BigDecimal totalVat,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt
) {

    /** Só os dados do cliente que a fatura precisa de mostrar. */
    public record ClientSummary(Long id, String name, String nif) {

        static ClientSummary from(Client client) {
            return new ClientSummary(client.getId(), client.getName(), client.getNif());
        }
    }

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getStatus(),
                ClientSummary.from(invoice.getClient()),
                invoice.getDueDate(),
                invoice.getNotes(),
                invoice.getLines().stream().map(InvoiceLineResponse::from).toList(),
                invoice.getTotalNet(),
                invoice.getTotalVat(),
                invoice.getTotal(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}