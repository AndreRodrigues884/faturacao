package pt.andrerodrigues.faturacao.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.andrerodrigues.faturacao.user.domain.User;
import java.util.List;

import java.util.Optional;

/**
 * REPOSITORY - acesso à tabela users.
 *
 * Fala com:     a base de dados
 * Trabalha com: User
 * É usado por:  AuthService (login e /me), AdminInitializer
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

        List<User> findAllByOrderByNameAsc();
}