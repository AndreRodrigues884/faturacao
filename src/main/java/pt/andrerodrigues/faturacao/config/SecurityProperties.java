package pt.andrerodrigues.faturacao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * CONFIGURAÇÃO - lê o bloco app.security do application.yml para objetos Java.
 *
 * Fala com:     application.yml (app.security.jwt e app.security.admin)
 * É usado por:  SecurityConfig (chave), TokenService (expiração), AdminInitializer (primeiro admin)
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Jwt jwt, Admin admin) {

    public record Jwt(String secret, Duration expiration) {
    }

    public record Admin(String email, String password, String name) {
    }
}