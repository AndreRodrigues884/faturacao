package pt.andrerodrigues.faturacao.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import pt.andrerodrigues.faturacao.dashboard.dto.DashboardResponse.CategoryTotal;
import pt.andrerodrigues.faturacao.dashboard.dto.DashboardResponse.Receivables;
import pt.andrerodrigues.faturacao.invoice.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONSULTAS DE AGREGAÇÃO - somas e contagens calculadas pela base de dados (SUM, COUNT, GROUP BY).
 * Usa o EntityManager diretamente, porque os resultados não são entidades, mas totais.
 *
 * Fala com:     a base de dados (JPQL através do EntityManager)
 * É usado por:  DashboardService
 */
@Repository
public class DashboardQueries {

    private static final List<InvoiceStatus> REVENUE_STATUSES = List.of(InvoiceStatus.ISSUED, InvoiceStatus.PAID);

    @PersistenceContext
    private EntityManager entityManager;

    /** Receitas (base sem IVA) por mês, contando só faturas emitidas e pagas. */
    public Map<Integer, BigDecimal> revenueByMonth(LocalDate from, LocalDate to) {
        List<Tuple> rows = entityManager.createQuery("""
                        select extract(month from i.issueDate) as month, sum(i.totalNet) as total
                        from Invoice i
                        where i.status in (:statuses)
                          and i.issueDate between :from and :to
                        group by extract(month from i.issueDate)
                        """, Tuple.class)
                .setParameter("statuses", REVENUE_STATUSES)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return toMonthMap(rows);
    }

    /** Despesas (base sem IVA) por mês. */
    public Map<Integer, BigDecimal> expensesByMonth(LocalDate from, LocalDate to) {
        List<Tuple> rows = entityManager.createQuery("""
                        select extract(month from e.expenseDate) as month, sum(e.netAmount) as total
                        from Expense e
                        where e.expenseDate between :from and :to
                        group by extract(month from e.expenseDate)
                        """, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return toMonthMap(rows);
    }

    /** Despesas do período agrupadas por categoria, da maior para a menor. */
    public List<CategoryTotal> expensesByCategory(LocalDate from, LocalDate to) {
        return entityManager.createQuery("""
                        select e.category.id as categoryId, e.category.name as categoryName, sum(e.netAmount) as total
                        from Expense e
                        where e.expenseDate between :from and :to
                        group by e.category.id, e.category.name
                        order by sum(e.netAmount) desc
                        """, Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList()
                .stream()
                .map(row -> new CategoryTotal(
                        row.get("categoryId", Long.class),
                        row.get("categoryName", String.class),
                        row.get("total", BigDecimal.class)))
                .toList();
    }

    /** Faturas emitidas e por pagar (com IVA), e quantas já passaram o vencimento. */
    public Receivables receivables(LocalDate today) {
        Tuple open = entityManager.createQuery("""
                        select count(i) as count, sum(i.total) as total
                        from Invoice i
                        where i.status = :status
                        """, Tuple.class)
                .setParameter("status", InvoiceStatus.ISSUED)
                .getSingleResult();

        Tuple overdue = entityManager.createQuery("""
                        select count(i) as count, sum(i.total) as total
                        from Invoice i
                        where i.status = :status
                          and i.dueDate < :today
                        """, Tuple.class)
                .setParameter("status", InvoiceStatus.ISSUED)
                .setParameter("today", today)
                .getSingleResult();

        return new Receivables(
                countOf(open), totalOf(open),
                countOf(overdue), totalOf(overdue));
    }

    // ---------- Auxiliares ----------

    private static Map<Integer, BigDecimal> toMonthMap(List<Tuple> rows) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        for (Tuple row : rows) {
            int month = row.get("month", Number.class).intValue();
            result.put(month, row.get("total", BigDecimal.class));
        }
        return result;
    }

    private static long countOf(Tuple row) {
        return row.get("count", Number.class).longValue();
    }

    private static BigDecimal totalOf(Tuple row) {
        BigDecimal total = row.get("total", BigDecimal.class);
        return total != null ? total : BigDecimal.ZERO.setScale(2);
    }
}