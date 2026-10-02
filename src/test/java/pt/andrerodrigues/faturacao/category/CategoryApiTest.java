package pt.andrerodrigues.faturacao.category;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DA API - contrato HTTP de /api/categories: status, headers, formato dos erros.
 *
 * Testa:  CategoryController + GlobalExceptionHandler (através de toda a aplicação)
 */
@WithMockUser(roles = "ADMIN")
@DisplayName("API de categorias")
class CategoryApiTest extends IntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    // ---------- Auxiliares ----------

    /** Nome único em cada execução, para os testes não colidirem com dados já existentes. */
    private static String uniqueName(String base) {
        return base + " " + System.nanoTime();
    }

    private MvcTestResult postCategory(String name) {
        return mvc.post().uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "%s", "description": "Criada pelo teste" }
                        """.formatted(name))
                .exchange();
    }

    private static long idOf(MvcTestResult result) throws Exception {
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    // ---------- Testes ----------

    @Test
    @DisplayName("POST cria a categoria e responde 201 com o header Location")
    void criaCategoria() {
        String name = uniqueName("Viagens");

        MvcTestResult result = postCategory(name);

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).headers().containsHeader("Location");
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo(name);
        assertThat(result).bodyJson().extractingPath("$.id").isNotNull();
    }

    @Test
    @DisplayName("POST com nome vazio responde 400 com o erro no campo name")
    void validacao() {
        MvcTestResult result = mvc.post().uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "" }
                        """)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(400);
        assertThat(result).bodyJson().extractingPath("$.errors.name").isNotNull();
    }

    @Test
    @DisplayName("POST com nome repetido (noutras maiúsculas) responde 409")
    void duplicado() {
        String name = uniqueName("Formacao");
        postCategory(name);

        MvcTestResult result = postCategory(name.toUpperCase());

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST com JSON mal formado responde 400")
    void jsonMalFormado() {
        MvcTestResult result = mvc.post().uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"name\": ")
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET de uma categoria inexistente responde 404 em formato ProblemDetail")
    void naoEncontrada() {
        MvcTestResult result = mvc.get().uri("/api/categories/{id}", 999_999).exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(404);
        assertThat(result).bodyJson().extractingPath("$.detail").isNotNull();
    }

    @Test
    @DisplayName("DELETE responde 204 e a categoria deixa de existir")
    void apaga() throws Exception {
        long id = idOf(postCategory(uniqueName("Temporaria")));

        assertThat(mvc.delete().uri("/api/categories/{id}", id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(mvc.get().uri("/api/categories/{id}", id)).hasStatus(HttpStatus.NOT_FOUND);
    }
}