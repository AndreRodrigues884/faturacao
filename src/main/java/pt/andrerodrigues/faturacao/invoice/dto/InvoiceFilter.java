package pt.andrerodrigues.faturacao.invoice.dto;

import pt.andrerodrigues.faturacao.invoice.domain.InvoiceStatus;

import java.time.LocalDate;

/**
 * DTO DE ENTRADA - filtros opcionais da listagem de faturas.
 * Qualquer campo a null significa "não filtrar por isto".
 *
 * Fala com:     ninguém
 * É usado por:  InvoiceController (monta-o a partir do URL), InvoiceSpecifications (constrói a query)
 */
public record InvoiceFilter(
        InvoiceStatus status,
        Long clientId,
        LocalDate issuedFrom,
        LocalDate issuedTo,
        Boolean overdue,
        String search
) {
}