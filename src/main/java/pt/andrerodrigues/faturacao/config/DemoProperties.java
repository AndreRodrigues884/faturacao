package pt.andrerodrigues.faturacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CONFIGURAÇÃO - credenciais da conta de demonstração (bloco app.demo do application.yml).
 *
 * Fala com:     application.yml
 * É usado por:  DemoDataInitializer (cria a conta), DemoController (mostra as credenciais no login),
 *               AuthService (impede que a password da conta de demonstração seja mudada)
 */
@ConfigurationProperties(prefix = "app.demo")
public record DemoProperties(String email, String password) {
}