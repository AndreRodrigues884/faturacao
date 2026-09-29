package pt.andrerodrigues.faturacao.invoice;

import jakarta.persistence.*;
import pt.andrerodrigues.faturacao.product.Product;
import pt.andrerodrigues.faturacao.product.VatRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ENTIDADE - uma linha de uma fatura (produto, quantidade, preço, IVA).
 * Guarda uma FOTOGRAFIA do produto no momento em que a linha é criada.
 *
 * Fala com:     Invoice (a fatura a que pertence), Product (copia os seus dados)
 * É usado por:  Invoice (só a Invoice cria linhas), InvoiceLineResponse
 */
@Entity
@Table(name = "invoice_lines")
public class InvoiceLine {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_code", nullable = false, length = 30)
    private String productCode;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "vat_rate", nullable = false, length = 20)
    private VatRate vatRate;

    @Column(name = "vat_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPercentage;

    @Column(name = "line_net", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineNet;

    @Column(name = "line_vat", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineVat;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected InvoiceLine() {
    }

    InvoiceLine(Invoice invoice, int lineNumber, Product product, BigDecimal quantity) {
        this.invoice = invoice;
        this.lineNumber = lineNumber;
        this.product = product;

        // Fotografia do produto neste momento
        this.productCode = product.getCode();
        this.description = product.getName();
        this.unitPrice = product.getUnitPrice();
        this.vatRate = product.getVatRate();
        this.vatPercentage = product.getVatRate().getPercentage().setScale(2, RoundingMode.HALF_UP);

        // Contas da linha: primeiro o valor da linha, só depois o IVA sobre esse valor
        this.quantity = quantity.setScale(3, RoundingMode.HALF_UP);
        this.lineNet = unitPrice.multiply(this.quantity).setScale(2, RoundingMode.HALF_UP);
        this.lineVat = lineNet.multiply(vatPercentage).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        this.lineTotal = lineNet.add(lineVat);
    }

    public Long getId() { return id; }
    public int getLineNumber() { return lineNumber; }
    public Product getProduct() { return product; }
    public String getProductCode() { return productCode; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public VatRate getVatRate() { return vatRate; }
    public BigDecimal getVatPercentage() { return vatPercentage; }
    public BigDecimal getLineNet() { return lineNet; }
    public BigDecimal getLineVat() { return lineVat; }
    public BigDecimal getLineTotal() { return lineTotal; }
}