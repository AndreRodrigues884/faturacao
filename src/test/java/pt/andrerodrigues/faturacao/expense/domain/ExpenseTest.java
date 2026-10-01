package pt.andrerodrigues.faturacao.expense.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.andrerodrigues.faturacao.category.Category;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TESTE UNITÁRIO - regras da despesa: base calculada e IVA nunca maior que o total.
 *
 * Testa:  Expense
 */
@DisplayName("Despesa")
class ExpenseTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 30);
    private static final Category REFEICOES = new Category("Refeições", null);

    private static Expense expense(String total, String vat) {
        return new Expense(REFEICOES, "Jantar", null, null, DATE,
                new BigDecimal(total), vat == null ? null : new BigDecimal(vat),
                PaymentMethod.CARD, null);
    }

    @Test
    @DisplayName("calcula a base como total menos IVA")
    void calculaBase() {
        Expense expense = expense("84.75", "9.75");

        assertThat(expense.getNetAmount()).isEqualByComparingTo("75.00");
    }

    @Test
    @DisplayName("sem IVA indicado, assume zero e a base é o total")
    void semIva() {
        Expense expense = expense("50.00", null);

        assertThat(expense.getVatAmount()).isEqualByComparingTo("0.00");
        assertThat(expense.getNetAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("recusa IVA maior do que o total")
    void recusaIvaMaiorQueTotal() {
        assertThatThrownBy(() -> expense("84.75", "100.00"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("IVA");
    }

    @Test
    @DisplayName("recalcula a base ao editar o total")
    void recalculaAoEditar() {
        Expense expense = expense("84.75", "9.75");

        expense.update(REFEICOES, "Jantar", null, null, DATE,
                new BigDecimal("90.00"), new BigDecimal("9.75"), PaymentMethod.CARD, null);

        assertThat(expense.getNetAmount()).isEqualByComparingTo("80.25");
    }
}