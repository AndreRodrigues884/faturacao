package pt.andrerodrigues.faturacao.demo;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.andrerodrigues.faturacao.config.DemoProperties;

/**
 * CONTROLLER (só com o perfil "demo") - devolve as credenciais públicas da conta de demonstração,
 * para o ecrã de login mostrar o botão "Entrar com a conta de demonstração".
 * Sem o perfil "demo", este endpoint não existe (404) e o botão não aparece.
 *
 * Fala com:     DemoProperties
 * É usado por:  o ecrã de login do frontend
 */
@RestController
@Profile("demo")
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoProperties properties;

    public DemoController(DemoProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/credentials")
    public DemoCredentials credentials() {
        return new DemoCredentials(properties.email(), properties.password());
    }

    public record DemoCredentials(String email, String password) {
    }
}