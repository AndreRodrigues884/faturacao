package pt.andrerodrigues.faturacao.common;

import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * UTILITÁRIO - garante que a ordenação pedida no URL (?sort=...) só usa campos permitidos.
 * Nunca se confia no que vem do pedido, nem no nome de um campo para ordenar.
 *
 * Fala com:     BusinessRuleException (quando o campo não é permitido)
 * É usado por:  services com listagens paginadas (InvoiceService, ExpenseService)
 */
public final class SortValidator {

    private SortValidator() {
    }

    public static void validate(Pageable pageable, Set<String> allowedFields) {
        pageable.getSort().forEach(order -> {
            if (!allowedFields.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "Não é possível ordenar por '" + order.getProperty() + "'. Campos permitidos: " + allowedFields);
            }
        });
    }
}