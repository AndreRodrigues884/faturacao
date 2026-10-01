package pt.andrerodrigues.faturacao.expense;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pt.andrerodrigues.faturacao.expense.domain.Expense;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * QUERY DINÂMICA - constrói o WHERE da listagem de despesas a partir dos filtros recebidos.
 *
 * Fala com:     ExpenseFilter (lê os filtros)
 * É usado por:  ExpenseService (passa o resultado ao ExpenseRepository)
 */
public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> withFilters(ExpenseFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (filter.categoryId() != null) {
                conditions.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }

            if (filter.from() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("expenseDate"), filter.from()));
            }

            if (filter.to() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.<LocalDate>get("expenseDate"), filter.to()));
            }

            if (filter.paymentMethod() != null) {
                conditions.add(cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
            }

            if (filter.search() != null && !filter.search().isBlank()) {
                String pattern = "%" + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
                conditions.add(cb.or(
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("supplierName")), pattern)
                ));
            }

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}