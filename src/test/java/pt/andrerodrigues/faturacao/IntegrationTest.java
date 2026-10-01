package pt.andrerodrigues.faturacao;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * BASE DOS TESTES DE INTEGRAÇÃO - arranca a aplicação inteira ligada ao PostgreSQL do Testcontainers.
 * Os testes de integração estendem esta classe em vez de repetirem as anotações.
 *
 * Fala com:     TestcontainersConfiguration
 * É usado por:  todos os testes de integração
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {
}