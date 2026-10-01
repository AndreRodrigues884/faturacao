package pt.andrerodrigues.faturacao.invoice.numbering;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * REPOSITORY - acesso à tabela invoice_series, com lock para numeração segura.
 *
 * Fala com:     a base de dados (SQL nativo e JPQL com lock)
 * Trabalha com: InvoiceSeries
 * É usado por:  InvoiceNumberGenerator (só por ele)
 */
public interface InvoiceSeriesRepository extends JpaRepository<InvoiceSeries, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO invoice_series (prefix, fiscal_year, last_number)
            VALUES (:prefix, :fiscalYear, 0)
            ON CONFLICT (prefix, fiscal_year) DO NOTHING
            """, nativeQuery = true)
    void createIfMissing(@Param("prefix") String prefix, @Param("fiscalYear") int fiscalYear);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSeries s WHERE s.prefix = :prefix AND s.fiscalYear = :fiscalYear")
    Optional<InvoiceSeries> findForUpdate(@Param("prefix") String prefix, @Param("fiscalYear") int fiscalYear);
}