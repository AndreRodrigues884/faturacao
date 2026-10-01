package pt.andrerodrigues.faturacao.invoice.dto;

import pt.andrerodrigues.faturacao.client.Client;
import pt.andrerodrigues.faturacao.invoice.domain.Invoice;
import pt.andrerodrigues.faturacao.invoice.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO DE SAÍDA - formato do JSON devolvido para uma fatura.
 * Num rascunho, o cliente vem dos dados atuais; numa fatura emitida, vem da fotografia.
 *
 * Fala com:     Invoice (lê os seus dados), InvoiceLineResponse (converte as linhas),
 *               Client (dados atuais do cliente, só em rascunho)
 * É usado por:  InvoiceService (cria-o a partir da entidade),
 *               InvoiceController (devolve-o ao cliente)
 */
public record InvoiceResponse(
        Long id,
        String number,
        InvoiceStatus status,
        ClientSummary client,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate paidDate,
        String notes,
        List<InvoiceLineResponse> lines,
        BigDecimal totalNet,
        BigDecimal totalVat,
        BigDecimal total,
        Instant cancelledAt,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt
) {

    public record ClientSummary(Long id, String name, String nif, String address, String postalCode, String city) {

        static ClientSummary from(Invoice invoice) {
            if (invoice.getStatus() == InvoiceStatus.DRAFT) {
                Client client = invoice.getClient();
                return new ClientSummary(client.getId(), client.getName(), client.getNif(),
                        client.getAddress(), client.getPostalCode(), client.getCity());
            }
            return new ClientSummary(invoice.getClient().getId(), invoice.getClientName(), invoice.getClientNif(),
                    invoice.getClientAddress(), invoice.getClientPostalCode(), invoice.getClientCity());
        }
    }

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getNumber(),
                invoice.getStatus(),
                ClientSummary.from(invoice),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getPaidDate(),
                invoice.getNotes(),
                invoice.getLines().stream().map(InvoiceLineResponse::from).toList(),
                invoice.getTotalNet(),
                invoice.getTotalVat(),
                invoice.getTotal(),
                invoice.getCancelledAt(),
                invoice.getCancellationReason(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}