package pt.andrerodrigues.faturacao.invoice.dto;

import pt.andrerodrigues.faturacao.invoice.domain.Invoice;
import pt.andrerodrigues.faturacao.invoice.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DTO DE SAÍDA - versão resumida de uma fatura, para listagens (sem linhas).
 * Inclui o cálculo de "em atraso" em relação à data de hoje.
 *
 * Fala com:     Invoice (lê os seus dados)
 * É usado por:  InvoiceService (listagem), InvoiceController
 */
public record InvoiceSummaryResponse(
        Long id,
        String number,
        InvoiceStatus status,
        Long clientId,
        String clientName,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate paidDate,
        BigDecimal total,
        boolean overdue,
        long daysOverdue
) {

    public static InvoiceSummaryResponse from(Invoice invoice, LocalDate today) {
        boolean overdue = invoice.getStatus() == InvoiceStatus.ISSUED
                && invoice.getDueDate().isBefore(today);

        long daysOverdue = overdue
                ? ChronoUnit.DAYS.between(invoice.getDueDate(), today)
                : 0;

        String clientName = invoice.getStatus() == InvoiceStatus.DRAFT
                ? invoice.getClient().getName()
                : invoice.getClientName();

        return new InvoiceSummaryResponse(
                invoice.getId(),
                invoice.getNumber(),
                invoice.getStatus(),
                invoice.getClient().getId(),
                clientName,
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getPaidDate(),
                invoice.getTotal(),
                overdue,
                daysOverdue
        );
    }
}