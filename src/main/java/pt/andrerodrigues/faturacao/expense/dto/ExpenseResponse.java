package pt.andrerodrigues.faturacao.expense.dto;

import pt.andrerodrigues.faturacao.category.Category;
import pt.andrerodrigues.faturacao.expense.domain.Expense;
import pt.andrerodrigues.faturacao.expense.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO DE SAÍDA - formato do JSON devolvido para uma despesa.
 *
 * Fala com:     Expense (lê os seus dados), Category (resumo da categoria)
 * É usado por:  ExpenseService, ExpenseController
 */
public record ExpenseResponse(
        Long id,
        CategorySummary category,
        String description,
        String supplierName,
        String supplierNif,
        LocalDate expenseDate,
        BigDecimal netAmount,
        BigDecimal vatAmount,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    public record CategorySummary(Long id, String name) {

        static CategorySummary from(Category category) {
            return new CategorySummary(category.getId(), category.getName());
        }
    }

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                CategorySummary.from(expense.getCategory()),
                expense.getDescription(),
                expense.getSupplierName(),
                expense.getSupplierNif(),
                expense.getExpenseDate(),
                expense.getNetAmount(),
                expense.getVatAmount(),
                expense.getTotalAmount(),
                expense.getPaymentMethod(),
                expense.getNotes(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}