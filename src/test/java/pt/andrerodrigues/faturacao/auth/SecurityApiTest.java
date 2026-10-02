package pt.andrerodrigues.faturacao.auth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.andrerodrigues.faturacao.IntegrationTest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DA API - segurança de ponta a ponta, com login e tokens JWT reais:
 * 401 sem token, 403 por falta de papel, contas desativadas, regra do admin.
 *
 * Testa:  SecurityConfig, AuthController, UserController, GlobalExceptionHandler
 */
@DisplayName("Segurança da API")
class SecurityApiTest extends IntegrationTest {

    private static final String ADMIN_EMAIL = "admin@faturacao.local";
    private static final String ADMIN_PASSWORD = "admin12345";

    @Autowired
    private MockMvcTester mvc;

    // ---------- Auxiliares ----------

    private MvcTestResult loginRequest(String email, String password) {
        return mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "%s", "password": "%s" }
                        """.formatted(email, password))
                .exchange();
    }

    private String login(String email, String password) throws Exception {
        MvcTestResult result = loginRequest(email, password);
        assertThat(result).hasStatusOk();
        return JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.accessToken");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    /** O admin cria uma conta USER com um email único e devolve o id. */
    private long createUser(String adminToken, String email) throws Exception {
        MvcTestResult result = mvc.post().uri("/api/users")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "%s", "name": "Utilizador Teste", "password": "password123", "role": "USER" }
                        """.formatted(email))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private static String uniqueEmail() {
        return "user" + System.nanoTime() + "@teste.pt";
    }

    // ---------- Autenticação (401) ----------

    @Test
    @DisplayName("sem token responde 401 em formato ProblemDetail")
    void semToken() {
        MvcTestResult result = mvc.get().uri("/api/categories").exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(401);
    }

    @Test
    @DisplayName("um token falso ou adulterado responde 401")
    void tokenInvalido() {
        assertThat(mvc.get().uri("/api/categories")
                .header("Authorization", bearer("abc.def.ghi")))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("login com a password errada responde 401")
    void loginErrado() {
        assertThat(loginRequest(ADMIN_EMAIL, "errada123")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("login devolve um token que dá acesso ao /me, sem expor o hash da password")
    void loginEMe() throws Exception {
        String token = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        MvcTestResult me = mvc.get().uri("/api/auth/me")
                .header("Authorization", bearer(token))
                .exchange();

        assertThat(me).hasStatusOk();
        assertThat(me).bodyJson().extractingPath("$.email").isEqualTo(ADMIN_EMAIL);
        assertThat(me).bodyJson().extractingPath("$.role").isEqualTo("ADMIN");
        assertThat(me).bodyJson().doesNotHavePath("$.passwordHash");
    }

    // ---------- Autorização (403) ----------

    @Test
    @DisplayName("um USER pode ver e trabalhar, mas não apagar, anular nem gerir contas")
    void permissoesDoUser() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = uniqueEmail();
        createUser(adminToken, email);
        String userToken = login(email, "password123");

        assertThat(mvc.get().uri("/api/categories")
                .header("Authorization", bearer(userToken)))
                .hasStatusOk();

        assertThat(mvc.delete().uri("/api/categories/{id}", 999_999)
                .header("Authorization", bearer(userToken)))
                .hasStatus(HttpStatus.FORBIDDEN);

        assertThat(mvc.post().uri("/api/invoices/{id}/cancel", 999_999)
                .header("Authorization", bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "reason": "Teste" }
                        """))
                .hasStatus(HttpStatus.FORBIDDEN);

        assertThat(mvc.get().uri("/api/users")
                .header("Authorization", bearer(userToken)))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    // ---------- Gestão de contas ----------

    @Test
    @DisplayName("uma conta desativada já não consegue fazer login")
    void contaDesativada() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = uniqueEmail();
        long userId = createUser(adminToken, email);

        assertThat(mvc.post().uri("/api/users/{id}/deactivate", userId)
                .header("Authorization", bearer(adminToken)))
                .hasStatusOk();

        assertThat(loginRequest(email, "password123")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("um admin não se pode desativar a si próprio")
    void adminNaoSeDesativa() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcTestResult me = mvc.get().uri("/api/auth/me")
                .header("Authorization", bearer(adminToken))
                .exchange();
        long adminId = ((Number) JsonPath.read(
                me.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id")).longValue();

        assertThat(mvc.post().uri("/api/users/{id}/deactivate", adminId)
                .header("Authorization", bearer(adminToken)))
                .hasStatus(422);
    }
}