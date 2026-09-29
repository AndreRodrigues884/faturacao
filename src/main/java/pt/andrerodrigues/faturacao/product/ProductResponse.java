package pt.andrerodrigues.faturacao.product;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO DE SAÍDA - formato do JSON devolvido pela API de produtos.
 * Inclui o IVA e o preço com IVA já calculados, para o site não ter de fazer contas.
 *
 * Fala com:     Product (lê os seus dados), VatRate (calcula o IVA)
 * É usado por:  ProductService (cria-o a partir da entidade),
 *               ProductController (devolve-o ao cliente)
 */
public record ProductResponse(
        Long id,
        String code,
        String name,
        String description,
        ProductType type,
        BigDecimal unitPrice,
        VatRate vatRate,
        BigDecimal vatPercentage,
        BigDecimal vatAmount,
        BigDecimal priceWithVat,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductResponse from(Product product) {
        BigDecimal vatAmount = product.getVatRate().vatAmountFor(product.getUnitPrice());

        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getType(),
                product.getUnitPrice(),
                product.getVatRate(),
                product.getVatRate().getPercentage(),
                vatAmount,
                product.getUnitPrice().add(vatAmount),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}