package pt.andrerodrigues.faturacao.invoice;

import jakarta.validation.Valid;
import pt.andrerodrigues.faturacao.invoice.dto.CancelRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceResponse;
import pt.andrerodrigues.faturacao.invoice.dto.PaymentRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * CONTROLLER - porta de entrada HTTP (/api/invoices).
 * CRUD do rascunho + ações do ciclo de vida (/issue, /pay, /cancel).
 *
 * Fala com:     InvoiceService (só com ele)
 * Recebe:       InvoiceRequest, PaymentRequest, CancelRequest
 * Devolve:      InvoiceResponse
 * É usado por:  clientes externos (Postman, Angular)
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse created = service.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public InvoiceResponse update(@PathVariable Long id, @Valid @RequestBody InvoiceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/issue")
    public InvoiceResponse issue(@PathVariable Long id) {
        return service.issue(id);
    }

    @PostMapping("/{id}/pay")
    public InvoiceResponse pay(@PathVariable Long id,
                               @Valid @RequestBody(required = false) PaymentRequest request) {
        return service.markAsPaid(id, request);
    }

    @PostMapping("/{id}/cancel")
    public InvoiceResponse cancel(@PathVariable Long id,
                                  @Valid @RequestBody CancelRequest request) {
        return service.cancel(id, request);
    }
}