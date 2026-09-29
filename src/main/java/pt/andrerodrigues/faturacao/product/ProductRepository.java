package pt.andrerodrigues.faturacao.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * REPOSITORY - acesso à tabela products no PostgreSQL.
 *
 * Fala com:     a base de dados (através do Hibernate)
 * Trabalha com: Product (lê e grava entidades)
 * É usado por:  ProductService (só por ele)
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByNameAsc();

    List<Product> findByActiveTrueOrderByNameAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}