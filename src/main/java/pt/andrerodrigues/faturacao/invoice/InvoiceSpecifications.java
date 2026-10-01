package pt.andrerodrigues.faturacao.invoice;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pt.andrerodrigues.faturacao.invoice.domain.Invoice;
import pt.andrerodrigues.faturacao.invoice.domain.InvoiceStatus;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * QUERY DINÂMICA - constrói o WHERE da listagem de faturas a partir dos filtros recebidos.
 * Só entram no WHERE os filtros que vieram preenchidos.
 *
 * Fala com:     InvoiceFilter (lê os filtros)
 * É usado por:  InvoiceService (passa o resultado ao InvoiceRepository)
 */
public final class InvoiceSpecifications {

    private InvoiceSpecifications() {
    }

    public static Specification<Invoice> withFilters(InvoiceFilter filter, LocalDate today) {
        return (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (filter.status() != null) {
                conditions.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.clientId() != null) {
                conditions.add(cb.equal(root.get("client").get("id"), filter.clientId()));
            }

            if (filter.issuedFrom() != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("issueDate"), filter.issuedFrom()));
            }

            if (filter.issuedTo() != null) {
                conditions.add(cb.lessThanOrEqualTo(root.<LocalDate>get("issueDate"), filter.issuedTo()));
            }

            if (Boolean.TRUE.equals(filter.overdue())) {
                conditions.add(cb.equal(root.get("status"), InvoiceStatus.ISSUED));
                conditions.add(cb.lessThan(root.<LocalDate>get("dueDate"), today));
            }

            if (filter.search() != null && !filter.search().isBlank()) {
                String pattern = "%" + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
                conditions.add(cb.or(
                        cb.like(cb.lower(root.get("number")), pattern),
                        cb.like(cb.lower(root.get("client").get("name")), pattern)
                ));
            }

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }
}