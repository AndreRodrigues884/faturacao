package pt.andrerodrigues.faturacao.expense.dto;

import pt.andrerodrigues.faturacao.expense.domain.PaymentMethod;

import java.time.LocalDate;

/**
 * DTO DE ENTRADA - filtros opcionais da listagem de despesas (null = não filtrar).
 *
 * Fala com:     PaymentMethod
 * É usado por:  ExpenseController (monta-o a partir do URL), ExpenseSpecifications
 */
public record ExpenseFilter(
        Long categoryId,
        LocalDate from,
        LocalDate to,
        PaymentMethod paymentMethod,
        String search
) {
}