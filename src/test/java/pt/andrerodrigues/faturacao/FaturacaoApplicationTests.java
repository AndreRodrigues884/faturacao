package pt.andrerodrigues.faturacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TESTE DE INTEGRAÇÃO - a aplicação arranca: o Flyway aplica todas as migrações
 * e o Hibernate valida que as entidades batem certo com as tabelas.
 */
@DisplayName("Arranque da aplicação")
class FaturacaoApplicationTests extends IntegrationTest {

    @Test
    @DisplayName("arranca com todas as migrações aplicadas e o esquema validado")
    void contextLoads() {
    }
}