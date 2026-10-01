package pt.andrerodrigues.faturacao.invoice.numbering;

import jakarta.persistence.*;

/**
 * ENTIDADE - contador de números de uma série num ano (ex: FT 2026, último número 7).
 *
 * Fala com:     ninguém
 * É usado por:  InvoiceSeriesRepository (lê com lock), InvoiceNumberGenerator (pede o próximo número)
 */
@Entity
@Table(name = "invoice_series")
public class InvoiceSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String prefix;

    @Column(name = "fiscal_year", nullable = false)
    private int fiscalYear;

    @Column(name = "last_number", nullable = false)
    private int lastNumber;

    protected InvoiceSeries() {
    }

    /** Avança o contador e devolve o número novo. Só é seguro com o lock ativo. */
    public int nextNumber() {
        this.lastNumber++;
        return this.lastNumber;
    }

    public String getPrefix() { return prefix; }
    public int getFiscalYear() { return fiscalYear; }
    public int getLastNumber() { return lastNumber; }
}