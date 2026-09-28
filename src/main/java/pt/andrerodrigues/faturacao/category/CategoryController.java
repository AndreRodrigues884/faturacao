/**
 * CONTROLLER - porta de entrada HTTP (/api/categories).
 *
 * Fala com:     CategoryService (só com ele)
 * Recebe:       CategoryRequest (JSON enviado pelo cliente)
 * Devolve:      CategoryResponse (JSON enviado ao cliente)
 * É usado por:  clientes externos (Postman, Angular)
 *
 * Nunca fala com o CategoryRepository nem usa a entidade Category.
 */

package pt.andrerodrigues.faturacao.category;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing categories.
 * Provides endpoints for CRUD operations on categories.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    /**
     * Retrieves a list of all categories.
     *
     * @return a list of CategoryResponse objects representing all categories
     */
    @GetMapping
    public List<CategoryResponse> list() {
        return service.findAll();
    }

    /**
     * Retrieves a specific category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return a CategoryResponse object representing the found category
     */
    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * Creates a new category based on the provided request.
     *
     * @param request the CategoryRequest object containing the details of the category to create
     * @return a ResponseEntity containing the created CategoryResponse and the location of the new resource
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = service.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Updates an existing category with the provided details.
     *
     * @param id      the ID of the category to update
     * @param request the CategoryRequest object containing the updated details
     * @return a CategoryResponse object representing the updated category
     */
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id the ID of the category to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}