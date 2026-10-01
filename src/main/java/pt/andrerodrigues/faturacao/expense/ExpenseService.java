package pt.andrerodrigues.faturacao.expense;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.category.Category;
import pt.andrerodrigues.faturacao.category.CategoryRepository;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.common.PageResponse;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;
import pt.andrerodrigues.faturacao.common.SortValidator;
import pt.andrerodrigues.faturacao.expense.domain.Expense;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseFilter;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseRequest;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseResponse;

import java.util.Set;

/**
 * SERVICE - operações sobre despesas: listar com filtros, ver, criar, editar e apagar.
 *
 * Fala com:     ExpenseRepository, CategoryRepository
 * Usa:          Expense, ExpenseRequest, ExpenseFilter, ExpenseResponse, PageResponse
 * Lança:        ResourceNotFoundException, BusinessRuleException (pasta common)
 * É usado por:  ExpenseController
 */
@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("expenseDate", "totalAmount", "createdAt", "description");

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResponse<ExpenseResponse> search(ExpenseFilter filter, Pageable pageable) {
        SortValidator.validate(pageable, SORTABLE_FIELDS);

        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new BusinessRuleException("A data inicial não pode ser posterior à data final");
        }

        return PageResponse.from(
                expenseRepository.findAll(ExpenseSpecifications.withFilters(filter), pageable)
                        .map(ExpenseResponse::from));
    }

    public ExpenseResponse findById(Long id) {
        return ExpenseResponse.from(getOrThrow(id));
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = new Expense(
                findCategory(request.categoryId()),
                request.description().trim(),
                blankToNull(request.supplierName()),
                normalizeNif(request.supplierNif()),
                request.expenseDate(),
                request.totalAmount(),
                request.vatAmount(),
                request.paymentMethod(),
                blankToNull(request.notes())
        );

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = getOrThrow(id);

        expense.update(
                findCategory(request.categoryId()),
                request.description().trim(),
                blankToNull(request.supplierName()),
                normalizeNif(request.supplierNif()),
                request.expenseDate(),
                request.totalAmount(),
                request.vatAmount(),
                request.paymentMethod(),
                blankToNull(request.notes())
        );

        expenseRepository.flush();
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.delete(getOrThrow(id));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessRuleException("A categoria " + categoryId + " não existe"));
    }

    private Expense getOrThrow(Long id) {
        return expenseRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa " + id + " não encontrada"));
    }

    private static String normalizeNif(String nif) {
        if (nif == null || nif.isBlank()) {
            return null;
        }
        return nif.replaceAll("\\s", "");
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}