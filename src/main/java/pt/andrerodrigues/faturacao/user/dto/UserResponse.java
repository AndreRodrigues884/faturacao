package pt.andrerodrigues.faturacao.user.dto;

import pt.andrerodrigues.faturacao.user.domain.Role;
import pt.andrerodrigues.faturacao.user.domain.User;

import java.time.Instant;

/**
 * DTO DE SAÍDA - dados públicos de um utilizador. Nunca inclui o hash da password.
 *
 * Fala com:     User
 * É usado por:  AuthService (login e /me), LoginResponse
 */
public record UserResponse(
        Long id,
        String email,
        String name,
        Role role,
        boolean active,
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}