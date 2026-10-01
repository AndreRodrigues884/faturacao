package pt.andrerodrigues.faturacao.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.common.DuplicateResourceException;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * SERVICE - regras de negócio dos produtos e serviços.
 *
 * Fala com: ProductRepository (para ler e gravar)
 * Usa: Product (entidade), ProductRequest (dados recebidos),
 * ProductResponse (converte a entidade antes de devolver)
 * Lança: ResourceNotFoundException, DuplicateResourceException (pasta common)
 * É usado por: ProductController
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductResponse> findAll(boolean includeInactive) {
        List<Product> products = includeInactive
                ? repository.findAllByOrderByNameAsc()
                : repository.findByActiveTrueOrderByNameAsc();

        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String code = normalizeCode(request.code());

        if (repository.existsByCode(code)) {
            throw new DuplicateResourceException("Já existe um produto com o código " + code);
        }

        Product product = new Product(
                code,
                request.name().trim(),
                blankToNull(request.description()),
                request.type(),
                normalizePrice(request.unitPrice()),
                request.vatRate());

        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        String code = normalizeCode(request.code());

        if (repository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Já existe um produto com o código " + code);
        }

        product.update(
                code,
                request.name().trim(),
                blankToNull(request.description()),
                request.type(),
                normalizePrice(request.unitPrice()),
                request.vatRate());

        repository.flush();
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).deactivate();
    }

    @Transactional
    public ProductResponse activate(Long id) {
        Product product = getOrThrow(id);
        product.activate();
        repository.flush();
        return ProductResponse.from(product);
    }

    private Product getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto " + id + " não encontrado"));
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static BigDecimal normalizePrice(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}