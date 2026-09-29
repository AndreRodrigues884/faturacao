package pt.andrerodrigues.faturacao.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ENTIDADE - representa uma linha da tabela products (produtos e serviços).
 *
 * Fala com: ProductType, VatRate (enums dos seus campos)
 * É usado por: ProductRepository (lê/grava), ProductService
 * (cria/altera/desativa),
 * ProductResponse (é convertida em DTO)
 *
 * Nunca é apagada: é desativada (active = false), porque pode estar em faturas
 * antigas.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ProductType type;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "vat_rate", nullable = false, length = 20)
    private VatRate vatRate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public Product(String code, String name, String description, ProductType type,
            BigDecimal unitPrice, VatRate vatRate) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
        this.unitPrice = unitPrice;
        this.vatRate = vatRate;
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

    public void update(String code, String name, String description, ProductType type,
            BigDecimal unitPrice, VatRate vatRate) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
        this.unitPrice = unitPrice;
        this.vatRate = vatRate;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductType getType() {
        return type;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public VatRate getVatRate() {
        return vatRate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}