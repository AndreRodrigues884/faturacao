/**
 * SERVICE - regras de negócio das categorias.
 *
 * Fala com:     CategoryRepository (para ler e gravar)
 * Usa:          Category (entidade), CategoryRequest (dados recebidos),
 *               CategoryResponse (converte a entidade antes de devolver)
 * Lança:        ResourceNotFoundException, DuplicateResourceException (pasta common)
 * É usado por:  CategoryController
 */

package pt.andrerodrigues.faturacao.category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.common.DuplicateResourceException;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
/**
 * Service class for managing categories.
 * Provides methods for CRUD operations on categories.
 */
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves all categories, ordered by name.
     *
     * @return a list of CategoryResponse objects representing all categories
     */
    public List<CategoryResponse> findAll() {
        return repository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return a CategoryResponse object representing the found category
     * @throws ResourceNotFoundException if no category with the given ID exists
     */
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    /**
     * Creates a new category based on the provided request.
     *
     * @param request the CategoryRequest object containing the details of the category to create
     * @return a CategoryResponse object representing the created category
     * @throws DuplicateResourceException if a category with the same name already exists
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();

        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Já existe uma categoria com o nome '" + name + "'");
        }

        Category saved = repository.save(new Category(name, request.description()));
        return CategoryResponse.from(saved);
    }

    /**
     * Updates an existing category with the provided details.
     *
     * @param id      the ID of the category to update
     * @param request the CategoryRequest object containing the updated details
     * @return a CategoryResponse object representing the updated category
     * @throws ResourceNotFoundException   if no category with the given ID exists
     * @throws DuplicateResourceException if a category with the same name already exists (excluding the current category)
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);
        String name = request.name().trim();

        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Já existe uma categoria com o nome '" + name + "'");
        }

        category.update(name, request.description());
        return CategoryResponse.from(category);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id the ID of the category to delete
     * @throws ResourceNotFoundException if no category with the given ID exists
     */
    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    /**
     * Retrieves a category by its ID or throws a ResourceNotFoundException if not found.
     *
     * @param id the ID of the category to retrieve
     * @return the Category object if found
     * @throws ResourceNotFoundException if no category with the given ID exists
     */
    private Category getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria " + id + " não encontrada"));
    }
}