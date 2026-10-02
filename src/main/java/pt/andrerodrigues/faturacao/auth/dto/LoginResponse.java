package pt.andrerodrigues.faturacao.auth.dto;

import pt.andrerodrigues.faturacao.user.dto.UserResponse;

/**
 * DTO DE SAÍDA - resposta do login: o token e os dados do utilizador autenticado.
 *
 * Fala com:     UserResponse
 * É usado por:  AuthService, AuthController
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}