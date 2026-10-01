package pt.andrerodrigues.faturacao.invoice.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pt.andrerodrigues.faturacao.client.Client;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.product.Product;
import pt.andrerodrigues.faturacao.product.ProductType;
import pt.andrerodrigues.faturacao.product.VatRate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TESTE UNITÁRIO - regras da fatura: totais, IVA por linha, fotografias e ciclo de vida.
 * Não usa Spring nem base de dados: só objetos Java criados com new.
 *
 * Testa:  Invoice, InvoiceLine
 */
@DisplayName("Fatura")
class InvoiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    // ---------- Dados de teste ----------

    private static Client client() {
        return new Client("Cliente Teste, Lda.", "509999999", null, null, "Rua A, 1", "4700-001", "Braga");
    }

    private static Product product(String code, String price, VatRate vatRate) {
        return new Product(code, "Produto " + code, null, ProductType.SERVICE, new BigDecimal(price), vatRate);
    }

    private static Invoice draft() {
        return new Invoice(client(), TODAY.plusDays(30), null);
    }

    private static Invoice draftWithOneLine() {
        Invoice invoice = draft();
        invoice.addLine(product("SRV-001", "45.00", VatRate.NORMAL), new BigDecimal("1"));
        return invoice;
    }

    // ---------- Totais ----------

    @Nested
    @DisplayName("Totais e IVA")
    class Totais {

        @Test
        @DisplayName("soma as linhas com taxas de IVA diferentes")
        void somaLinhasComTaxasDiferentes() {
            Invoice invoice = draft();

            invoice.addLine(product("SRV-001", "45.00", VatRate.NORMAL), new BigDecimal("10"));
            invoice.addLine(product("PRD-001", "29.90", VatRate.REDUCED), new BigDecimal("3"));
            invoice.addLine(product("PRD-002", "12.50", VatRate.INTERMEDIATE), new BigDecimal("2"));

            assertThat(invoice.getTotalNet()).isEqualByComparingTo("564.70");
            assertThat(invoice.getTotalVat()).isEqualByComparingTo("112.13");
            assertThat(invoice.getTotal()).isEqualByComparingTo("676.83");
        }

        @Test
        @DisplayName("calcula o IVA sobre o valor da linha e não por unidade")
        void ivaCalculadoPorLinha() {
            Invoice invoice = draft();

            invoice.addLine(product("PRD-001", "29.90", VatRate.REDUCED), new BigDecimal("3"));

            // 89,70 x 6% = 5,382 -> 5,38 (por unidade daria 1,79 x 3 = 5,37)
            assertThat(invoice.getLines().get(0).getLineVat()).isEqualByComparingTo("5.38");
        }

        @Test
        @DisplayName("aceita quantidades decimais")
        void quantidadesDecimais() {
            Invoice invoice = draft();

            invoice.addLine(product("SRV-002", "35.00", VatRate.NORMAL), new BigDecimal("1.5"));

            InvoiceLine line = invoice.getLines().get(0);
            assertThat(line.getLineNet()).isEqualByComparingTo("52.50");
            assertThat(line.getLineVat()).isEqualByComparingTo("12.08");
            assertThat(line.getLineTotal()).isEqualByComparingTo("64.58");
        }

        @Test
        @DisplayName("recalcula os totais quando as linhas são removidas")
        void recalculaAoLimparLinhas() {
            Invoice invoice = draftWithOneLine();

            invoice.clearLines();

            assertThat(invoice.getTotal()).isEqualByComparingTo("0.00");
            assertThat(invoice.getLines()).isEmpty();
        }
    }

    // ---------- Linhas ----------

    @Nested
    @DisplayName("Linhas")
    class Linhas {

        @Test
        @DisplayName("guarda uma fotografia do produto que não muda se o produto mudar")
        void fotografiaDoProduto() {
            Product product = product("SRV-001", "45.00", VatRate.NORMAL);
            Invoice invoice = draft();
            invoice.addLine(product, new BigDecimal("1"));

            product.update("SRV-001", "Outro nome", null, ProductType.SERVICE,
                    new BigDecimal("99.00"), VatRate.REDUCED);

            InvoiceLine line = invoice.getLines().get(0);
            assertThat(line.getUnitPrice()).isEqualByComparingTo("45.00");
            assertThat(line.getVatRate()).isEqualTo(VatRate.NORMAL);
            assertThat(line.getDescription()).isEqualTo("Produto SRV-001");
        }

        @Test
        @DisplayName("recusa produtos desativados")
        void recusaProdutoDesativado() {
            Product product = product("SRV-003", "60.00", VatRate.NORMAL);
            product.deactivate();
            Invoice invoice = draft();

            assertThatThrownBy(() -> invoice.addLine(product, BigDecimal.ONE))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("desativado");
        }

        @Test
        @DisplayName("numera as linhas pela ordem em que são adicionadas")
        void numeracaoDasLinhas() {
            Invoice invoice = draft();

            invoice.addLine(product("A", "1.00", VatRate.NORMAL), BigDecimal.ONE);
            invoice.addLine(product("B", "1.00", VatRate.NORMAL), BigDecimal.ONE);

            assertThat(invoice.getLines())
                    .extracting(InvoiceLine::getLineNumber)
                    .containsExactly(1, 2);
        }
    }

    // ---------- Emissão ----------

    @Nested
    @DisplayName("Emissão")
    class Emissao {

        @Test
        @DisplayName("atribui número formatado, data, estado e fotografia do cliente")
        void emiteFatura() {
            Invoice invoice = draftWithOneLine();

            invoice.issue("FT", 2026, 7, TODAY);

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
            assertThat(invoice.getNumber()).isEqualTo("FT 2026/0007");
            assertThat(invoice.getIssueDate()).isEqualTo(TODAY);
            assertThat(invoice.getClientName()).isEqualTo("Cliente Teste, Lda.");
            assertThat(invoice.getClientCity()).isEqualTo("Braga");
        }

        @Test
        @DisplayName("mantém os dados do cliente da emissão mesmo que o cliente mude")
        void fotografiaDoCliente() {
            Invoice invoice = draftWithOneLine();
            invoice.issue("FT", 2026, 1, TODAY);

            invoice.getClient().update("Nome Novo", "509999999", null, null, "Rua B", "4700-002", "Porto");

            assertThat(invoice.getClientName()).isEqualTo("Cliente Teste, Lda.");
            assertThat(invoice.getClientCity()).isEqualTo("Braga");
        }

        @Test
        @DisplayName("recusa emitir uma fatura sem linhas")
        void recusaSemLinhas() {
            Invoice invoice = draft();

            assertThatThrownBy(() -> invoice.issue("FT", 2026, 1, TODAY))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("sem linhas");
        }

        @Test
        @DisplayName("recusa vencimento anterior à data de emissão")
        void recusaVencimentoNoPassado() {
            Invoice invoice = new Invoice(client(), TODAY.minusDays(1), null);
            invoice.addLine(product("SRV-001", "45.00", VatRate.NORMAL), BigDecimal.ONE);

            assertThatThrownBy(() -> invoice.issue("FT", 2026, 1, TODAY))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("vencimento");
        }

        @Test
        @DisplayName("depois de emitida, já não pode ser alterada nem apagada")
        void emitidaFicaProtegida() {
            Invoice invoice = draftWithOneLine();
            invoice.issue("FT", 2026, 1, TODAY);
            Product product = product("SRV-002", "35.00", VatRate.NORMAL);

            assertThatThrownBy(() -> invoice.addLine(product, BigDecimal.ONE))
                    .isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(invoice::clearLines)
                    .isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(() -> invoice.updateHeader(client(), TODAY.plusDays(60), "x"))
                    .isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(invoice::ensureCanBeDeleted)
                    .isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(() -> invoice.issue("FT", 2026, 2, TODAY))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("um rascunho pode ser apagado")
        void rascunhoPodeSerApagado() {
            Invoice invoice = draftWithOneLine();

            assertThatCode(invoice::ensureCanBeDeleted).doesNotThrowAnyException();
        }
    }

    // ---------- Pagamento e anulação ----------

    @Nested
    @DisplayName("Pagamento e anulação")
    class PagamentoEAnulacao {

        @Test
        @DisplayName("marca como paga uma fatura emitida")
        void pagaFaturaEmitida() {
            Invoice invoice = draftWithOneLine();
            invoice.issue("FT", 2026, 1, TODAY);

            invoice.markAsPaid(TODAY.plusDays(5));

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(invoice.getPaidDate()).isEqualTo(TODAY.plusDays(5));
        }

        @Test
        @DisplayName("recusa pagar um rascunho")
        void recusaPagarRascunho() {
            Invoice invoice = draftWithOneLine();

            assertThatThrownBy(() -> invoice.markAsPaid(TODAY))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("recusa data de pagamento anterior à emissão")
        void recusaPagamentoAntesDaEmissao() {
            Invoice invoice = draftWithOneLine();
            invoice.issue("FT", 2026, 1, TODAY);

            assertThatThrownBy(() -> invoice.markAsPaid(TODAY.minusDays(1)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("anula uma fatura emitida e guarda o motivo")
        void anulaFaturaEmitida() {
            Invoice invoice = draftWithOneLine();
            invoice.issue("FT", 2026, 1, TODAY);

            invoice.cancel("Erro no valor");

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
            assertThat(invoice.getCancellationReason()).isEqualTo("Erro no valor");
            assertThat(invoice.getCancelledAt()).isNotNull();
            assertThat(invoice.getNumber()).isEqualTo("FT 2026/0001");
        }

        @Test
        @DisplayName("recusa anular uma fatura paga")
        void recusaAnularPaga() {
            Invoice invoice = draftWithOneLine();
            invoice.issue("FT", 2026, 1, TODAY);
            invoice.markAsPaid(TODAY);

            assertThatThrownBy(() -> invoice.cancel("Teste"))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }
}