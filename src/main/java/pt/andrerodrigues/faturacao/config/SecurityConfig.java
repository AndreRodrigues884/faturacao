package pt.andrerodrigues.faturacao.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * CONFIGURAÇÃO DE SEGURANÇA - quem pode aceder a quê, como se validam os tokens e como se guardam passwords.
 *
 * Fala com:     SecurityProperties (chave secreta), GlobalExceptionHandler (através do HandlerExceptionResolver,
 *               para os erros 401/403 terem o mesmo formato que os outros)
 * É usado por:  o Spring Security, em todos os pedidos; TokenService (JwtEncoder); AuthService (PasswordEncoder)
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) throws Exception {

        // Os erros de segurança acontecem antes de chegar aos controllers; isto reencaminha-os
        // para o GlobalExceptionHandler, para saírem no mesmo formato ProblemDetail.
        AuthenticationEntryPoint unauthorized = (request, response, ex) ->
                exceptionResolver.resolveException(request, response, null, ex);
        AccessDeniedHandler forbidden = (request, response, ex) ->
                exceptionResolver.resolveException(request, response, null, ex);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/categories/**", "/api/clients/**", "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/invoices/*/cancel").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder(SecurityProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }

    @Bean
    JwtDecoder jwtDecoder(SecurityProperties properties) {
        return NimbusJwtDecoder.withSecretKey(secretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private static SecretKey secretKey(SecurityProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.jwt().secret());
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /** Lê o papel do utilizador da claim "roles" do token e transforma-o em ROLE_ADMIN / ROLE_USER. */
    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}