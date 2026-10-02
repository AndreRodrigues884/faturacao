package pt.andrerodrigues.faturacao.user.domain;

/**
 * ENUM - papéis dos utilizadores.
 *   ADMIN -> pode tudo, incluindo apagar, anular faturas e gerir utilizadores
 *   USER  -> trabalho do dia a dia: criar, editar, emitir e pagar
 *
 * Fala com:     ninguém
 * É usado por:  User, TokenService (vai para o JWT), SecurityConfig (regras de acesso)
 */
public enum Role {
    ADMIN,
    USER
}