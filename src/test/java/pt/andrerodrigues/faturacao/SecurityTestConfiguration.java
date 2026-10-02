package pt.andrerodrigues.faturacao;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;

/**
 * CONFIGURAÇÃO DE TESTES - faz o @WithMockUser funcionar nos pedidos do MockMvc.
 * Em cada pedido, copia o utilizador simulado do teste para dentro do pedido,
 * para o Spring Security o reconhecer como autenticado.
 *
 * Fala com:     Spring Security Test (testSecurityContext)
 * É usado por:  IntegrationTest (e, através dela, todos os testes da API)
 */
@TestConfiguration(proxyBeanMethods = false)
public class SecurityTestConfiguration {

    @Bean
    MockMvcBuilderCustomizer securityContextCustomizer() {
        return builder -> builder.defaultRequest(
                MockMvcRequestBuilders.get("/").with(testSecurityContext()));
    }
}