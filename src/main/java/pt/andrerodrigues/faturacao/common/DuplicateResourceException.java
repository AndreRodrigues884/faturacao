package pt.andrerodrigues.faturacao.common;

/**
 * Exception thrown when a resource already exists and cannot be duplicated.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}