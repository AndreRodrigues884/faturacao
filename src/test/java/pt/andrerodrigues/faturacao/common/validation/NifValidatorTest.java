package pt.andrerodrigues.faturacao.common.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE UNITÁRIO - regra do dígito de controlo do NIF português.
 *
 * Testa:  NifValidator
 */
@DisplayName("Validação de NIF")
class NifValidatorTest {

    @ParameterizedTest(name = "\"{0}\" é válido")
    @ValueSource(strings = {"123456789", "509999999", "234567899", "123 456 789"})
    @DisplayName("aceita NIFs com dígito de controlo correto")
    void aceitaNifsValidos(String nif) {
        assertThat(NifValidator.isValidNif(nif)).isTrue();
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @ValueSource(strings = {"123456788", "12345678", "1234567890", "12345678A", "abcdefghi"})
    @DisplayName("recusa NIFs com dígito errado, tamanho errado ou letras")
    void recusaNifsInvalidos(String nif) {
        assertThat(NifValidator.isValidNif(nif)).isFalse();
    }

    @Test
    @DisplayName("deixa os campos vazios para o @NotBlank decidir")
    void campoVazioNaoEhResponsabilidadeDoValidador() {
        NifValidator validator = new NifValidator();

        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("   ", null)).isTrue();
    }
}