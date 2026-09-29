package pt.andrerodrigues.faturacao.invoice;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * REPOSITORY - acesso às tabelas invoices e invoice_lines no PostgreSQL.
 *
 * Fala com:     a base de dados (através do Hibernate)
 * Trabalha com: Invoice (e as suas InvoiceLine, por cascata)
 * É usado por:  InvoiceService (só por ele)
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @EntityGraph(attributePaths = {"client", "lines"})
    Optional<Invoice> findWithDetailsById(Long id);
}