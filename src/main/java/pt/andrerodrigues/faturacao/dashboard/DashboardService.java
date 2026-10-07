package pt.andrerodrigues.faturacao.dashboard;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.dashboard.dto.DashboardResponse;
import pt.andrerodrigues.faturacao.dashboard.dto.DashboardResponse.MonthFigures;
import pt.andrerodrigues.faturacao.dashboard.dto.DashboardResponse.YearTotals;
import pt.andrerodrigues.faturacao.invoice.InvoiceService;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceFilter;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceSummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * SERVICE - monta o dashboard de um ano: receitas e despesas por mês, totais do ano,
 * valores a receber, despesas por categoria e as faturas mais atrasadas.
 *
 * Fala com:     DashboardQueries (somas e contagens), InvoiceService (faturas em atraso)
 * Lança:        BusinessRuleException (ano fora do intervalo razoável)
 * É usado por:  DashboardController
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final int OVERDUE_LIMIT = 5;

    private final DashboardQueries queries;
    private final InvoiceService invoiceService;

    public DashboardService(DashboardQueries queries, InvoiceService invoiceService) {
        this.queries = queries;
        this.invoiceService = invoiceService;
    }

    public DashboardResponse forYear(Integer requestedYear) {
        LocalDate today = LocalDate.now(LISBON);
        int year = requestedYear != null ? requestedYear : today.getYear();

        if (year < 2000 || year > 2100) {
            throw new BusinessRuleException("Ano inválido: " + year);
        }

        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        Map<Integer, BigDecimal> revenue = queries.revenueByMonth(from, to);
        Map<Integer, BigDecimal> expenses = queries.expensesByMonth(from, to);

        List<MonthFigures> months = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    BigDecimal monthRevenue = revenue.getOrDefault(month, ZERO);
                    BigDecimal monthExpenses = expenses.getOrDefault(month, ZERO);
                    return new MonthFigures(month, monthRevenue, monthExpenses, monthRevenue.subtract(monthExpenses));
                })
                .toList();

        BigDecimal totalRevenue = sum(months, MonthFigures::revenue);
        BigDecimal totalExpenses = sum(months, MonthFigures::expenses);

        return new DashboardResponse(
                year,
                months,
                new YearTotals(totalRevenue, totalExpenses, totalRevenue.subtract(totalExpenses)),
                queries.receivables(today),
                queries.expensesByCategory(from, to),
                mostOverdueInvoices()
        );
    }

    /** As faturas em atraso há mais tempo (vencimento mais antigo primeiro). */
    private List<InvoiceSummaryResponse> mostOverdueInvoices() {
        InvoiceFilter onlyOverdue = new InvoiceFilter(null, null, null, null, true, null);
        return invoiceService
                .search(onlyOverdue, PageRequest.of(0, OVERDUE_LIMIT, Sort.by("dueDate")))
                .content();
    }

    private static BigDecimal sum(List<MonthFigures> months, Function<MonthFigures, BigDecimal> field) {
        return months.stream().map(field).reduce(ZERO, BigDecimal::add);
    }
}