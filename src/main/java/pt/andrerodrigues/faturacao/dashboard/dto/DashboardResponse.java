package pt.andrerodrigues.faturacao.dashboard.dto;

import pt.andrerodrigues.faturacao.invoice.dto.InvoiceSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO DE SAÍDA - tudo o que a página inicial mostra, num só pedido.
 * Receitas e despesas são valores SEM IVA (o IVA não é rendimento da empresa).
 * O "a receber" é COM IVA (é o que os clientes vão pagar).
 *
 * Fala com:     InvoiceSummaryResponse (faturas em atraso)
 * É usado por:  DashboardService, DashboardQueries, DashboardController
 */
public record DashboardResponse(
        int year,
        List<MonthFigures> months,
        YearTotals totals,
        Receivables receivables,
        List<CategoryTotal> expensesByCategory,
        List<InvoiceSummaryResponse> overdueInvoices
) {

    /** Receitas, despesas e resultado de um mês (1 = janeiro). */
    public record MonthFigures(int month, BigDecimal revenue, BigDecimal expenses, BigDecimal result) {
    }

    /** Somas do ano inteiro. */
    public record YearTotals(BigDecimal revenue, BigDecimal expenses, BigDecimal result) {
    }

    /** Faturas emitidas e por pagar, e quantas dessas já passaram o vencimento. */
    public record Receivables(long openCount, BigDecimal openTotal, long overdueCount, BigDecimal overdueTotal) {
    }

    /** Total de despesas de uma categoria no ano. */
    public record CategoryTotal(Long categoryId, String categoryName, BigDecimal total) {
    }
}