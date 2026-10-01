package pt.andrerodrigues.faturacao.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE UNITÁRIO - cálculo do IVA e arredondamento HALF_UP.
 *
 * Testa:  VatRate
 */
@DisplayName("Cálculo do IVA")
class VatRateTest {

    @ParameterizedTest(name = "{0} sobre {1} = {2}")
    @CsvSource({
            "NORMAL,       100.00, 23.00",
            "REDUCED,       29.90,  1.79",   // 1,794 arredonda para baixo
            "INTERMEDIATE,  12.50,  1.63",   // 1,625 arredonda para cima (HALF_UP)
            "NORMAL,        52.50, 12.08",   // 12,075 arredonda para cima
            "NORMAL,         0.00,  0.00"
    })
    @DisplayName("calcula o IVA com 2 casas decimais e arredondamento HALF_UP")
    void calculaIvaComArredondamento(VatRate rate, BigDecimal amount, BigDecimal expectedVat) {
        BigDecimal vat = rate.vatAmountFor(amount);

        assertThat(vat).isEqualByComparingTo(expectedVat);
        assertThat(vat.scale()).isEqualTo(2);
    }
}