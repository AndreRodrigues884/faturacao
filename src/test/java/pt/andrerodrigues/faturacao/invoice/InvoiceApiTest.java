package pt.andrerodrigues.faturacao.invoice;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.andrerodrigues.faturacao.IntegrationTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DA API - contrato HTTP de /api/invoices: criação, ciclo de vida, erros e listagem.
 *
 * Testa:  InvoiceController + InvoiceService + GlobalExceptionHandler (através de toda a aplicação)
 */
@WithMockUser(roles = "ADMIN")
@DisplayName("API de faturas")
class InvoiceApiTest extends IntegrationTest {

    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");

    @Autowired
    private MockMvcTester mvc;

    // ---------- Auxiliares ----------

    /** Rascunho para o cliente 1, com 2 unidades do produto 1 (45,00 € com IVA a 23%). */
    private static String draftJson() {
        return """
                {
                  "clientId": 1,
                  "dueDate": "%s",
                  "lines": [
                    { "productId": 1, "quantity": 2 }
                  ]
                }
                """.formatted(LocalDate.now(LISBON).plusDays(30));
    }

    private MvcTestResult postInvoice(String json) {
        return mvc.post().uri("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private static long idOf(MvcTestResult result) throws Exception {
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    // ---------- Ciclo de vida ----------

    @Test
    @DisplayName("percorre o ciclo completo: rascunho, emissão, proteção e pagamento")
    void cicloCompleto() throws Exception {
        // 1. Criar rascunho
        MvcTestResult created = postInvoice(draftJson());
        assertThat(created).hasStatus(HttpStatus.CREATED);
        assertThat(created).bodyJson().extractingPath("$.status").isEqualTo("DRAFT");
        assertThat(created).bodyJson().extractingPath("$.total").isEqualTo(110.70);
        long id = idOf(created);

        // 2. Emitir
        MvcTestResult issued = mvc.post().uri("/api/invoices/{id}/issue", id).exchange();
        assertThat(issued).hasStatusOk();
        assertThat(issued).bodyJson().extractingPath("$.status").isEqualTo("ISSUED");
        assertThat(issued).bodyJson().extractingPath("$.number").asString().matches("FT \\d{4}/\\d{4}");

        // 3. Uma fatura emitida já não se altera nem se apaga
        assertThat(mvc.put().uri("/api/invoices/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(draftJson()))
                .hasStatus(422);
        assertThat(mvc.delete().uri("/api/invoices/{id}", id)).hasStatus(422);

        // 4. Pagar (sem corpo: a data é a de hoje)
        MvcTestResult paid = mvc.post().uri("/api/invoices/{id}/pay", id).exchange();
        assertThat(paid).hasStatusOk();
        assertThat(paid).bodyJson().extractingPath("$.status").isEqualTo("PAID");

        // 5. Uma fatura paga não se anula
        assertThat(mvc.post().uri("/api/invoices/{id}/cancel", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "reason": "Teste" }
                        """))
                .hasStatus(422);
    }

    // ---------- Validação ----------

    @Test
    @DisplayName("POST sem linhas responde 400 com o erro no campo lines")
    void semLinhas() {
        MvcTestResult result = postInvoice("""
                { "clientId": 1, "dueDate": "2030-01-01", "lines": [] }
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.errors.lines").isNotNull();
    }

    @Test
    @DisplayName("POST com quantidade zero indica a linha e o campo com erro")
    void erroNumaLinha() {
        MvcTestResult result = postInvoice("""
                {
                  "clientId": 1,
                  "dueDate": "2030-01-01",
                  "lines": [ { "productId": 1, "quantity": 0 } ]
                }
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.errors['lines[0].quantity']").isNotNull();
    }

    @Test
    @DisplayName("POST com cliente inexistente responde 422")
    void clienteInexistente() {
        MvcTestResult result = postInvoice("""
                {
                  "clientId": 999999,
                  "dueDate": "2030-01-01",
                  "lines": [ { "productId": 1, "quantity": 1 } ]
                }
                """);

        assertThat(result).hasStatus(422);
    }

    @Test
    @DisplayName("POST /cancel sem corpo responde 400")
    void anularSemCorpo() throws Exception {
        long id = idOf(postInvoice(draftJson()));

        assertThat(mvc.post().uri("/api/invoices/{id}/cancel", id)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    // ---------- Listagem ----------

    @Test
    @DisplayName("GET devolve uma página com conteúdo e informação de paginação")
    void listagemPaginada() {
        postInvoice(draftJson());

        MvcTestResult result = mvc.get().uri("/api/invoices?size=5").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.content").isNotNull();
        assertThat(result).bodyJson().extractingPath("$.size").isEqualTo(5);
        assertThat(result).bodyJson().extractingPath("$.page").isEqualTo(0);
        assertThat(result).bodyJson().extractingPath("$.totalElements").isNotNull();
    }

    @Test
    @DisplayName("GET limita o tamanho da página a 100")
    void limiteDoTamanhoDaPagina() {
        MvcTestResult result = mvc.get().uri("/api/invoices?size=500").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.size").isEqualTo(100);
    }

    @Test
    @DisplayName("GET recusa ordenar por campos não permitidos")
    void ordenacaoProibida() {
        assertThat(mvc.get().uri("/api/invoices?sort=clientNif")).hasStatus(422);
    }

    @Test
    @DisplayName("GET com um estado inexistente responde 400")
    void estadoInexistente() {
        assertThat(mvc.get().uri("/api/invoices?status=XPTO")).hasStatus(HttpStatus.BAD_REQUEST);
    }
}