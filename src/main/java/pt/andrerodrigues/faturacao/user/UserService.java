package pt.andrerodrigues.faturacao.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.common.DuplicateResourceException;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;
import pt.andrerodrigues.faturacao.user.domain.Role;
import pt.andrerodrigues.faturacao.user.domain.User;
import pt.andrerodrigues.faturacao.user.dto.CreateUserRequest;
import pt.andrerodrigues.faturacao.user.dto.ResetPasswordRequest;
import pt.andrerodrigues.faturacao.user.dto.UpdateUserRequest;
import pt.andrerodrigues.faturacao.user.dto.UserResponse;

import java.util.List;
import java.util.Locale;

/**
 * SERVICE - gestão de contas pelo administrador.
 * Regra: um admin não se pode desativar nem tirar a si próprio o papel de admin,
 * para o sistema nunca ficar sem nenhum administrador.
 *
 * Fala com:     UserRepository, PasswordEncoder
 * Lança:        ResourceNotFoundException, DuplicateResourceException, BusinessRuleException
 * É usado por:  UserController
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Long id) {
        return UserResponse.from(getOrThrow(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Já existe uma conta com o email " + email);
        }

        User user = new User(email, request.name().trim(), passwordEncoder.encode(request.password()), request.role());
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, Long currentUserId) {
        User user = getOrThrow(id);

        if (user.getId().equals(currentUserId) && request.role() != Role.ADMIN) {
            throw new BusinessRuleException("Não pode retirar a si próprio o papel de administrador");
        }

        user.update(request.name().trim(), request.role());
        repository.flush();
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse deactivate(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessRuleException("Não pode desativar a sua própria conta");
        }

        User user = getOrThrow(id);
        user.deactivate();
        repository.flush();
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse activate(Long id) {
        User user = getOrThrow(id);
        user.activate();
        repository.flush();
        return UserResponse.from(user);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        getOrThrow(id).changePassword(passwordEncoder.encode(request.newPassword()));
    }

    private User getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador " + id + " não encontrado"));
    }
}