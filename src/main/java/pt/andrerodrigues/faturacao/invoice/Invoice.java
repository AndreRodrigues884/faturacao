package pt.andrerodrigues.faturacao.invoice;

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
 * ENTIDADE - uma fatura, com as suas linhas e totais.
 * Contém as regras: só se altera em rascunho, e os totais são sempre recalculados.
 *
 * Fala com:     Client (a quem é passada), InvoiceLine (as suas linhas),
 *               Product (para criar linhas), BusinessRuleException (quando uma regra é violada)
 * É usado por:  InvoiceRepository (lê/grava), InvoiceService (cria/altera/apaga),
 *               InvoiceResponse (é convertida em DTO)
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // ---------- Regras de negócio ----------

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
    public Client getClient() { return client; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public String getNotes() { return notes; }
    public List<InvoiceLine> getLines() { return Collections.unmodifiableList(lines); }
    public BigDecimal getTotalNet() { return totalNet; }
    public BigDecimal getTotalVat() { return totalVat; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}