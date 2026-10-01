package pt.andrerodrigues.faturacao.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pt.andrerodrigues.faturacao.expense.domain.Expense;

import java.util.Optional;

/**
 * REPOSITORY - acesso à tabela expenses no PostgreSQL.
 *
 * Fala com:     a base de dados (através do Hibernate)
 * Trabalha com: Expense
 * É usado por:  ExpenseService (só por ele)
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @EntityGraph(attributePaths = {"category"})
    Optional<Expense> findWithCategoryById(Long id);

    @Override
    @EntityGraph(attributePaths = {"category"})
    Page<Expense> findAll(Specification<Expense> spec, Pageable pageable);
}