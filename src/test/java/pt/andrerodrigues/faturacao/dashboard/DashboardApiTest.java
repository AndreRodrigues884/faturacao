package pt.andrerodrigues.faturacao.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.andrerodrigues.faturacao.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DA API - contrato do dashboard: 12 meses sempre presentes, blocos da resposta, ano inválido.
 *
 * Testa:  DashboardController, DashboardService, DashboardQueries
 */
@DisplayName("API do dashboard")
@WithMockUser(roles = "USER")
class DashboardApiTest extends IntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    @DisplayName("devolve os 12 meses do ano e todos os blocos, mesmo sem dados")
    void estruturaCompleta() {
        MvcTestResult result = mvc.get().uri("/api/dashboard?year=2026").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.year").isEqualTo(2026);
        assertThat(result).bodyJson().extractingPath("$.months").asArray().hasSize(12);
        assertThat(result).bodyJson().extractingPath("$.months[0].month").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.totals").isNotNull();
        assertThat(result).bodyJson().extractingPath("$.receivables").isNotNull();
        assertThat(result).bodyJson().extractingPath("$.expensesByCategory").isNotNull();
    }

    @Test
    @DisplayName("recusa um ano fora do intervalo razoável")
    void anoInvalido() {
        assertThat(mvc.get().uri("/api/dashboard?year=1900")).hasStatus(422);
    }

    @Test
    @DisplayName("um ano sem nenhuma fatura nem despesa dá zeros, e não erros")
    void anoVazio() {
        MvcTestResult result = mvc.get().uri("/api/dashboard?year=2001").exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.totals.revenue").isEqualTo(0.0);
        assertThat(result).bodyJson().extractingPath("$.totals.expenses").isEqualTo(0.0);
    }
}