
/**
 * ANOTAÇÃO DE VALIDAÇÃO - marca um campo como "tem de ser um NIF português válido".
 *
 * Fala com:     NifValidator (é ele que faz a verificação)
 * É usado por:  DTOs de entrada, ex: ClientRequest
 *
 * Usa-se como qualquer anotação de validação: @ValidNif String nif
 */

package pt.andrerodrigues.faturacao.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Constraint(validatedBy = NifValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNif {

    String message() default "NIF inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}