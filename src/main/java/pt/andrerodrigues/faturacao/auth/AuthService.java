package pt.andrerodrigues.faturacao.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.auth.dto.ChangePasswordRequest;
import pt.andrerodrigues.faturacao.auth.dto.LoginRequest;
import pt.andrerodrigues.faturacao.auth.dto.LoginResponse;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.config.DemoProperties;
import pt.andrerodrigues.faturacao.user.UserRepository;
import pt.andrerodrigues.faturacao.user.domain.User;
import pt.andrerodrigues.faturacao.user.dto.UserResponse;

/**
 * SERVICE - login (verifica credenciais e emite o token), dados do utilizador autenticado
 * e mudança da própria password. A conta de demonstração não pode mudar de password.
 *
 * Fala com:     UserRepository, PasswordEncoder, TokenService, DemoProperties
 * Lança:        BadCredentialsException (credenciais erradas ou conta inativa -> 401),
 *               BusinessRuleException (password atual errada, nova igual à atual,
 *               ou conta de demonstração -> 422)
 * É usado por:  AuthController
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Email ou password inválidos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final DemoProperties demoProperties;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       DemoProperties demoProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.demoProperties = demoProperties;
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

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("O utilizador já não está ativo"));

        if (user.getEmail().equalsIgnoreCase(demoProperties.email())) {
            throw new BusinessRuleException("A password da conta de demonstração não pode ser alterada");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("A password atual está incorreta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("A nova password tem de ser diferente da atual");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}