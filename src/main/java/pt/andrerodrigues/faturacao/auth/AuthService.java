package pt.andrerodrigues.faturacao.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.auth.dto.LoginRequest;
import pt.andrerodrigues.faturacao.auth.dto.LoginResponse;
import pt.andrerodrigues.faturacao.user.UserRepository;
import pt.andrerodrigues.faturacao.user.domain.User;
import pt.andrerodrigues.faturacao.user.dto.UserResponse;

/**
 * SERVICE - login (verifica credenciais e emite o token) e dados do utilizador autenticado.
 *
 * Fala com:     UserRepository, PasswordEncoder, TokenService
 * Lança:        BadCredentialsException (credenciais erradas -> 401)
 * É usado por:  AuthController
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Email ou password inválidos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(User::isActive)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        return new LoginResponse(
                tokenService.generateToken(user),
                "Bearer",
                tokenService.expiresInSeconds(),
                UserResponse.from(user)
        );
    }

    public UserResponse currentUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .map(UserResponse::from)
                .orElseThrow(() -> new BadCredentialsException("O utilizador já não está ativo"));
    }
}