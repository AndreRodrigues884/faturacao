package pt.andrerodrigues.faturacao.invoice.numbering;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * SERVICE - atribui o próximo número de uma série, sem saltos nem repetições.
 *
 * Fala com:     InvoiceSeriesRepository
 * É usado por:  InvoiceService (ao emitir uma fatura)
 *
 * Tem de correr DENTRO da transação da emissão (Propagation.MANDATORY):
 * se a emissão falhar, o número volta atrás no rollback e não fica nenhum salto.
 */
@Service
public class InvoiceNumberGenerator {

    private final InvoiceSeriesRepository repository;

    public InvoiceNumberGenerator(InvoiceSeriesRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public int next(String prefix, int fiscalYear) {
        repository.createIfMissing(prefix, fiscalYear);

        InvoiceSeries series = repository.findForUpdate(prefix, fiscalYear)
                .orElseThrow(() -> new IllegalStateException("Série " + prefix + " " + fiscalYear + " não encontrada"));

        return series.nextNumber();
    }
}