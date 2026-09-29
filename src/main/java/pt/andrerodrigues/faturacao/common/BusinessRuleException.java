package pt.andrerodrigues.faturacao.common;

/**
 * EXCEÇÃO DE DOMÍNIO - "o pedido está bem escrito, mas viola uma regra de negócio".
 * Ex: alterar uma fatura já emitida, usar um produto desativado.
 *
 * É lançada por: entidades (ex: Invoice) e services
 * É apanhada por: GlobalExceptionHandler, que a transforma em 422
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}