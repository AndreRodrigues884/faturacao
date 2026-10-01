package pt.andrerodrigues.faturacao.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.client.Client;
import pt.andrerodrigues.faturacao.client.ClientRepository;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;
import pt.andrerodrigues.faturacao.invoice.domain.Invoice;
import pt.andrerodrigues.faturacao.invoice.dto.CancelRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceLineRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceResponse;
import pt.andrerodrigues.faturacao.invoice.dto.PaymentRequest;
import pt.andrerodrigues.faturacao.invoice.numbering.InvoiceNumberGenerator;
import pt.andrerodrigues.faturacao.product.Product;
import pt.andrerodrigues.faturacao.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.andrerodrigues.faturacao.common.PageResponse;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceFilter;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceSummaryResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SERVICE - operações sobre faturas: rascunho (criar, editar, apagar) e ciclo
 * de vida
 * (emitir, pagar, anular). As regras da fatura vivem na entidade Invoice.
 *
 * Fala com: InvoiceRepository, ClientRepository, ProductRepository,
 * InvoiceNumberGenerator (números sequenciais)
 * Usa: Invoice, InvoiceRequest, PaymentRequest, CancelRequest, InvoiceResponse
 * Lança: ResourceNotFoundException, BusinessRuleException (pasta common)
 * É usado por: InvoiceController
 */
@Service
@Transactional(readOnly = true)
public class InvoiceService {

    private static final String DEFAULT_SERIES = "FT";
    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "issueDate", "dueDate", "number", "total",
            "status");

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final InvoiceNumberGenerator numberGenerator;

    public InvoiceService(InvoiceRepository invoiceRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            InvoiceNumberGenerator numberGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.numberGenerator = numberGenerator;
    }

    public InvoiceResponse findById(Long id) {
        return InvoiceResponse.from(getOrThrow(id));
    }

    public PageResponse<InvoiceSummaryResponse> search(InvoiceFilter filter, Pageable pageable) {
        validateSort(pageable);

        if (filter.issuedFrom() != null && filter.issuedTo() != null
                && filter.issuedFrom().isAfter(filter.issuedTo())) {
            throw new BusinessRuleException("A data inicial não pode ser posterior à data final");
        }

        LocalDate today = LocalDate.now(LISBON);

        Page<Invoice> page = invoiceRepository.findAll(
                InvoiceSpecifications.withFilters(filter, today), pageable);

        return PageResponse.from(page.map(invoice -> InvoiceSummaryResponse.from(invoice, today)));
    }

    // ---------- Rascunho ----------

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        Client client = findClient(request.clientId());

        Invoice invoice = new Invoice(client, request.dueDate(), blankToNull(request.notes()));
        addLines(invoice, request.lines());

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse update(Long id, InvoiceRequest request) {
        Invoice invoice = getOrThrow(id);
        Client client = findClient(request.clientId());

        invoice.updateHeader(client, request.dueDate(), blankToNull(request.notes()));
        invoice.clearLines();
        addLines(invoice, request.lines());

        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public void delete(Long id) {
        Invoice invoice = getOrThrow(id);
        invoice.ensureCanBeDeleted();
        invoiceRepository.delete(invoice);
    }

    // ---------- Ciclo de vida ----------

    @Transactional
    public InvoiceResponse issue(Long id) {
        Invoice invoice = getOrThrow(id);
        LocalDate today = LocalDate.now(LISBON);

        // 1. Verificar tudo ANTES de trancar o contador da série
        invoice.ensureCanBeIssued(today);

        // 2. Obter o número (tranca a série até ao fim desta transação)
        int number = numberGenerator.next(DEFAULT_SERIES, today.getYear());

        // 3. Emitir
        invoice.issue(DEFAULT_SERIES, today.getYear(), number, today);

        invoiceRepository.flush();
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse markAsPaid(Long id, PaymentRequest request) {
        Invoice invoice = getOrThrow(id);

        LocalDate paidDate = (request != null && request.paidDate() != null)
                ? request.paidDate()
                : LocalDate.now(LISBON);

        invoice.markAsPaid(paidDate);

        invoiceRepository.flush();
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse cancel(Long id, CancelRequest request) {
        Invoice invoice = getOrThrow(id);
        invoice.cancel(request.reason().trim());
        invoiceRepository.flush();
        return InvoiceResponse.from(invoice);
    }

    // ---------- Auxiliares ----------

    private void addLines(Invoice invoice, List<InvoiceLineRequest> lineRequests) {
        Set<Long> productIds = lineRequests.stream()
                .map(InvoiceLineRequest::productId)
                .collect(Collectors.toSet());

        Map<Long, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (InvoiceLineRequest lineRequest : lineRequests) {
            Product product = productsById.get(lineRequest.productId());
            if (product == null) {
                throw new BusinessRuleException("O produto " + lineRequest.productId() + " não existe");
            }
            invoice.addLine(product, lineRequest.quantity());
        }
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessRuleException("O cliente " + clientId + " não existe"));
    }

    private Invoice getOrThrow(Long id) {
        return invoiceRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura " + id + " não encontrada"));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static void validateSort(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "Não é possível ordenar por '" + order.getProperty() + "'. Campos permitidos: "
                                + SORTABLE_FIELDS);
            }
        });
    }
}