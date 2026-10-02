package pt.andrerodrigues.faturacao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pt.andrerodrigues.faturacao.user.UserRepository;
import pt.andrerodrigues.faturacao.user.domain.Role;
import pt.andrerodrigues.faturacao.user.domain.User;

/**
 * ARRANQUE - na primeira vez que a app arranca (tabela users vazia), cria o administrador
 * com os dados de app.security.admin. Assim nunca há passwords escritas em migrações SQL.
 *
 * Fala com:     UserRepository, PasswordEncoder, SecurityProperties
 * É usado por:  o Spring, automaticamente, no fim do arranque
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties properties;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            SecurityProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        SecurityProperties.Admin admin = properties.admin();
        userRepository.save(new User(
                admin.email().trim().toLowerCase(),
                admin.name(),
                passwordEncoder.encode(admin.password()),
                Role.ADMIN
        ));

        log.info("Primeiro administrador criado: {}", admin.email());
    }
}