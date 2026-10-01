package pt.andrerodrigues.faturacao.invoice.domain;

import jakarta.persistence.*;
import pt.andrerodrigues.faturacao.client.Client;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.product.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * ENTIDADE - uma fatura, com as suas linhas, totais e ciclo de vida.
 * DRAFT -> ISSUED -> PAID, ou ISSUED -> CANCELLED. Só o rascunho se altera ou apaga.
 *
 * Fala com:     Client (a quem é passada), InvoiceLine (as suas linhas),
 *               Product (para criar linhas), BusinessRuleException (quando uma regra é violada)
 * É usado por:  InvoiceRepository (lê/grava), InvoiceService (cria/altera/emite/paga/anula),
 *               InvoiceResponse (é convertida em DTO)
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNumber ASC")
    private List<InvoiceLine> lines = new ArrayList<>();

    @Column(name = "total_net", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO.setScale(2);

    @Column(name = "total_vat", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVat = BigDecimal.ZERO.setScale(2);

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO.setScale(2);

    // ---------- Emissão ----------

    @Column(name = "series_prefix", length = 10)
    private String seriesPrefix;

    @Column(name = "fiscal_year")
    private Integer fiscalYear;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(length = 30)
    private String number;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    // Fotografia do cliente no momento da emissão
    @Column(name = "client_name", length = 150)
    private String clientName;

    @Column(name = "client_nif", length = 9)
    private String clientNif;

    @Column(name = "client_address", length = 255)
    private String clientAddress;

    @Column(name = "client_postal_code", length = 8)
    private String clientPostalCode;

    @Column(name = "client_city", length = 100)
    private String clientCity;

    // ---------- Pagamento e anulação ----------

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Invoice() {
    }

    public Invoice(Client client, LocalDate dueDate, String notes) {
        this.client = client;
        this.dueDate = dueDate;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ---------- Regras do rascunho ----------

    public void updateHeader(Client client, LocalDate dueDate, String notes) {
        ensureDraft();
        this.client = client;
        this.dueDate = dueDate;
        this.notes = notes;
    }

    public void addLine(Product product, BigDecimal quantity) {
        ensureDraft();
        if (!product.isActive()) {
            throw new BusinessRuleException("O produto " + product.getCode() + " está desativado e não pode ser faturado");
        }
        lines.add(new InvoiceLine(this, lines.size() + 1, product, quantity));
        recalculateTotals();
    }

    public void clearLines() {
        ensureDraft();
        lines.clear();
        recalculateTotals();
    }

    public void ensureCanBeDeleted() {
        if (status != InvoiceStatus.DRAFT) {
            throw new BusinessRuleException("Só é possível apagar faturas em rascunho. Uma fatura emitida anula-se.");
        }
    }

    // ---------- Ciclo de vida ----------

    /** Verifica, sem alterar nada, se a fatura pode ser emitida nesta data. */
    public void ensureCanBeIssued(LocalDate issueDate) {
        ensureDraft();
        if (lines.isEmpty()) {
            throw new BusinessRuleException("Não é possível emitir uma fatura sem linhas");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new BusinessRuleException("A data de vencimento (" + dueDate + ") não pode ser anterior à data de emissão (" + issueDate + ")");
        }
    }

    public void issue(String seriesPrefix, int fiscalYear, int sequenceNumber, LocalDate issueDate) {
        ensureCanBeIssued(issueDate);

        this.seriesPrefix = seriesPrefix;
        this.fiscalYear = fiscalYear;
        this.sequenceNumber = sequenceNumber;
        this.number = String.format("%s %d/%04d", seriesPrefix, fiscalYear, sequenceNumber);
        this.issueDate = issueDate;

        this.clientName = client.getName();
        this.clientNif = client.getNif();
        this.clientAddress = client.getAddress();
        this.clientPostalCode = client.getPostalCode();
        this.clientCity = client.getCity();

        this.status = InvoiceStatus.ISSUED;
    }

    public void markAsPaid(LocalDate paidDate) {
        if (status != InvoiceStatus.ISSUED) {
            throw new BusinessRuleException("Só faturas emitidas podem ser marcadas como pagas. Esta fatura está " + status);
        }
        if (paidDate.isBefore(issueDate)) {
            throw new BusinessRuleException("A data de pagamento não pode ser anterior à data de emissão (" + issueDate + ")");
        }
        this.paidDate = paidDate;
        this.status = InvoiceStatus.PAID;
    }

    public void cancel(String reason) {
        if (status != InvoiceStatus.ISSUED) {
            throw new BusinessRuleException("Só faturas emitidas e não pagas podem ser anuladas. Esta fatura está " + status);
        }
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
        this.status = InvoiceStatus.CANCELLED;
    }

    // ---------- Auxiliares ----------

    private void ensureDraft() {
        if (status != InvoiceStatus.DRAFT) {
            throw new BusinessRuleException("Só é possível alterar faturas em rascunho. Esta fatura está " + status);
        }
    }

    private void recalculateTotals() {
        this.totalNet = sum(InvoiceLine::getLineNet);
        this.totalVat = sum(InvoiceLine::getLineVat);
        this.total = sum(InvoiceLine::getLineTotal);
    }

    private BigDecimal sum(Function<InvoiceLine, BigDecimal> field) {
        return lines.stream()
                .map(field)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }

    // ---------- Getters ----------

    public Long getId() { return id; }
    public long getVersion() { return version; }
    public Client getClient() { return client; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public String getNotes() { return notes; }
    public List<InvoiceLine> getLines() { return Collections.unmodifiableList(lines); }
    public BigDecimal getTotalNet() { return totalNet; }
    public BigDecimal getTotalVat() { return totalVat; }
    public BigDecimal getTotal() { return total; }
    public String getSeriesPrefix() { return seriesPrefix; }
    public Integer getFiscalYear() { return fiscalYear; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public String getNumber() { return number; }
    public LocalDate getIssueDate() { return issueDate; }
    public String getClientName() { return clientName; }
    public String getClientNif() { return clientNif; }
    public String getClientAddress() { return clientAddress; }
    public String getClientPostalCode() { return clientPostalCode; }
    public String getClientCity() { return clientCity; }
    public LocalDate getPaidDate() { return paidDate; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}