/**
 * DTO DE SAÍDA - formato do JSON devolvido pela API.
 *
 * Fala com:     Category (lê os seus dados no método from)
 * É usado por:  CategoryService (cria-o a partir da entidade),
 *               CategoryController (devolve-o ao cliente)
 */

package pt.andrerodrigues.faturacao.category;

import java.time.Instant;

/**
 * Represents a response object for category information.
 * This record is used to transfer category data in API responses.
 */
public record CategoryResponse(
        Long id,
        String name,
        String description,
        Instant createdAt
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
        );
    }
}