package pt.andrerodrigues.faturacao.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.client.Client;
import pt.andrerodrigues.faturacao.client.ClientRepository;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;
import pt.andrerodrigues.faturacao.product.Product;
import pt.andrerodrigues.faturacao.product.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SERVICE - operações sobre faturas em rascunho (criar, ver, editar, apagar).
 * As regras da fatura em si vivem na entidade Invoice; aqui junta-se tudo.
 *
 * Fala com:     InvoiceRepository, ClientRepository, ProductRepository
 * Usa:          Invoice (entidade), InvoiceRequest (dados recebidos),
 *               InvoiceResponse (converte a entidade antes de devolver)
 * Lança:        ResourceNotFoundException, BusinessRuleException (pasta common)
 * É usado por:  InvoiceController
 */
@Service
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ClientRepository clientRepository,
                          ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    public InvoiceResponse findById(Long id) {
        return InvoiceResponse.from(getOrThrow(id));
    }

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
}