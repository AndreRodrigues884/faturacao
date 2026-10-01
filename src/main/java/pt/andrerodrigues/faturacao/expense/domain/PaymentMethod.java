package pt.andrerodrigues.faturacao.expense.domain;

/**
 * ENUM - formas de pagamento de uma despesa.
 *
 * Fala com:     ninguém
 * É usado por:  Expense, ExpenseRequest, ExpenseResponse, ExpenseFilter
 */
public enum PaymentMethod {
    CARD,
    CASH,
    TRANSFER,
    MB_WAY,
    OTHER
}