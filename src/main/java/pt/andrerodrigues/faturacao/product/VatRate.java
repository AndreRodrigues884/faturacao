package pt.andrerodrigues.faturacao.product;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ENUM - as taxas de IVA de Portugal continental, com a respetiva percentagem.
 *
 * Fala com:     ninguém (só faz contas com BigDecimal)
 * É usado por:  Product (campo vatRate), ProductRequest, ProductResponse (cálculo do IVA)
 *               e mais tarde pelas linhas das faturas
 */
public enum VatRate {

    // As taxas de IVA de Portugal continental, com a respetiva percentagem.
    NORMAL(new BigDecimal("23")),
    INTERMEDIATE(new BigDecimal("13")),
    REDUCED(new BigDecimal("6"));

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final BigDecimal percentage;

    VatRate(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    /** Calcula o valor do IVA sobre um montante, arredondado a 2 casas decimais. */
    public BigDecimal vatAmountFor(BigDecimal amount) {
        return amount.multiply(percentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}