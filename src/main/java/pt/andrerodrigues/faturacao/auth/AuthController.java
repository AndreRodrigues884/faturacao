package pt.andrerodrigues.faturacao.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.andrerodrigues.faturacao.auth.dto.ChangePasswordRequest;
import pt.andrerodrigues.faturacao.auth.dto.LoginRequest;
import pt.andrerodrigues.faturacao.auth.dto.LoginResponse;
import pt.andrerodrigues.faturacao.user.dto.UserResponse;

/**
 * CONTROLLER - porta de entrada HTTP da autenticação (/api/auth).
 *   POST /login -> público: troca email e password por um token
 *   GET  /me    -> autenticado: dados do utilizador dono do token
 *
 * Fala com:     AuthService
 * É usado por:  clientes externos (Postman, Angular)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.currentUser(Long.valueOf(jwt.getSubject()));
    }

        @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt,
                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(Long.valueOf(jwt.getSubject()), request);
    }
}