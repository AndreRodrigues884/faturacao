/**
 * ENTIDADE - representa uma linha da tabela categories.
 *
 * Fala com:     ninguém (não depende de nenhum outro ficheiro)
 * É usado por:  CategoryRepository (lê/grava), CategoryService (cria/altera),
 *               CategoryResponse (é convertida em DTO)
 *
 * Nunca sai do backend: a API devolve sempre CategoryResponse.
 */

package pt.andrerodrigues.faturacao.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Represents a category entity in the system.
 * This class is mapped to the "categories" table in the database.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    /**
     * Updates the category's name and description.
     *
     * @param name        the new name of the category
     * @param description the new description of the category
     */
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Converts this Category entity to a CategoryResponse object.
     *
     * @return a CategoryResponse representing this category
     */
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}