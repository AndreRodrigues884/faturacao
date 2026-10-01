package pt.andrerodrigues.faturacao.expense.domain;

import jakarta.persistence.*;
import pt.andrerodrigues.faturacao.category.Category;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

/**
 * ENTIDADE - uma despesa, registada a partir do documento do fornecedor (talão, fatura de compra).
 * Os valores são os do documento; só a base (total - IVA) é calculada.
 *
 * Fala com:     Category (a categoria da despesa), PaymentMethod,
 *               BusinessRuleException (IVA maior que o total)
 * É usado por:  ExpenseRepository (lê/grava), ExpenseService (cria/altera/apaga),
 *               ExpenseResponse (é convertida em DTO)
 */
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @Column(name = "supplier_nif", length = 9)
    private String supplierNif;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Expense() {
    }

    public Expense(Category category, String description, String supplierName, String supplierNif,
                   LocalDate expenseDate, BigDecimal totalAmount, BigDecimal vatAmount,
                   PaymentMethod paymentMethod, String notes) {
        update(category, description, supplierName, supplierNif,
                expenseDate, totalAmount, vatAmount, paymentMethod, notes);
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

    public void update(Category category, String description, String supplierName, String supplierNif,
                       LocalDate expenseDate, BigDecimal totalAmount, BigDecimal vatAmount,
                       PaymentMethod paymentMethod, String notes) {

        BigDecimal total = totalAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = (vatAmount == null ? BigDecimal.ZERO : vatAmount).setScale(2, RoundingMode.HALF_UP);

        if (vat.compareTo(total) > 0) {
            throw new BusinessRuleException("O IVA (" + vat + ") não pode ser maior do que o total (" + total + ")");
        }

        this.category = category;
        this.description = description;
        this.supplierName = supplierName;
        this.supplierNif = supplierNif;
        this.expenseDate = expenseDate;
        this.totalAmount = total;
        this.vatAmount = vat;
        this.netAmount = total.subtract(vat);
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierNif() { return supplierNif; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}