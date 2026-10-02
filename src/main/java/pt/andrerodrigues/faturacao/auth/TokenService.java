package pt.andrerodrigues.faturacao.auth;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import pt.andrerodrigues.faturacao.config.SecurityProperties;
import pt.andrerodrigues.faturacao.user.domain.User;

import java.time.Instant;
import java.util.List;

/**
 * SERVICE - cria e assina o JWT de um utilizador.
 *
 * Fala com:     JwtEncoder (assinatura), SecurityProperties (tempo de expiração)
 * É usado por:  AuthService (no login)
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties properties;

    public TokenService(JwtEncoder jwtEncoder, SecurityProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("faturacao-api")
                .issuedAt(now)
                .expiresAt(now.plus(properties.jwt().expiration()))
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", List.of(user.getRole().name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return properties.jwt().expiration().toSeconds();
    }
}