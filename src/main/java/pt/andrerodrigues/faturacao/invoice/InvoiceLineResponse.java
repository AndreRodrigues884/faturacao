package pt.andrerodrigues.faturacao.invoice;

import pt.andrerodrigues.faturacao.product.VatRate;

import java.math.BigDecimal;

/**
 * DTO DE SAÍDA - uma linha dentro do JSON de resposta de uma fatura.
 *
 * Fala com:     InvoiceLine (lê os seus dados no método from)
 * É usado por:  InvoiceResponse (lista de linhas)
 */
public record InvoiceLineResponse(
        Long id,
        int lineNumber,
        Long productId,
        String productCode,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        VatRate vatRate,
        BigDecimal vatPercentage,
        BigDecimal lineNet,
        BigDecimal lineVat,
        BigDecimal lineTotal
) {

    public static InvoiceLineResponse from(InvoiceLine line) {
        return new InvoiceLineResponse(
                line.getId(),
                line.getLineNumber(),
                line.getProduct().getId(),
                line.getProductCode(),
                line.getDescription(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getVatRate(),
                line.getVatPercentage(),
                line.getLineNet(),
                line.getLineVat(),
                line.getLineTotal()
        );
    }
}