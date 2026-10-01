package pt.andrerodrigues.faturacao;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * CONFIGURAÇÃO DE TESTES - cria um PostgreSQL em Docker, novo e vazio, para os testes de integração.
 * O Spring liga-se a ele automaticamente, em vez de à base de dados de desenvolvimento.
 *
 * Fala com:     Docker (através do Testcontainers)
 * É usado por:  IntegrationTest (e, através dela, todos os testes de integração)
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }
}