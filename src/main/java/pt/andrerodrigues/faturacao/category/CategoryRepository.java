/**
 * REPOSITORY - acesso à tabela categories no PostgreSQL.
 *
 * Fala com:     a base de dados (através do Hibernate)
 * Trabalha com: Category (lê e grava entidades)
 * É usado por:  CategoryService (só por ele)
 *
 * É uma interface: o Spring Data cria a implementação no arranque.
 */

package pt.andrerodrigues.faturacao.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing Category entities.
 * This interface extends JpaRepository to provide CRUD operations and custom query methods.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Retrieves all categories ordered by their name in ascending order.
     *
     * @return a list of categories sorted by name
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Checks if a category with the specified name exists, ignoring case.
     *
     * @param name the name of the category to check
     * @return true if a category with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);


    /**
     * Checks if a category with the specified name exists, ignoring case, excluding the category with the given ID.
     *
     * @param name the name of the category to check
     * @param id   the ID of the category to exclude from the check
     * @return true if a category with the given name exists (excluding the specified ID), false otherwise
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}