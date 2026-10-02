package pt.andrerodrigues.faturacao;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * BASE DOS TESTES DE INTEGRAÇÃO - arranca a aplicação inteira ligada ao PostgreSQL do Testcontainers,
 * com o MockMvc disponível para simular pedidos HTTP e o @WithMockUser a funcionar.
 *
 * Fala com:     TestcontainersConfiguration, SecurityTestConfiguration
 * É usado por:  todos os testes de integração e da API
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, SecurityTestConfiguration.class})
public abstract class IntegrationTest {
}