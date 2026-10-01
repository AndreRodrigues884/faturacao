package pt.andrerodrigues.faturacao.expense;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.andrerodrigues.faturacao.common.PageResponse;
import pt.andrerodrigues.faturacao.expense.domain.PaymentMethod;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseFilter;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseRequest;
import pt.andrerodrigues.faturacao.expense.dto.ExpenseResponse;

import java.net.URI;
import java.time.LocalDate;

/**
 * CONTROLLER - porta de entrada HTTP (/api/expenses).
 *
 * Fala com:     ExpenseService (só com ele)
 * Recebe:       ExpenseRequest, filtros pelo URL
 * Devolve:      ExpenseResponse, PageResponse<ExpenseResponse>
 * É usado por:  clientes externos (Postman, Angular)
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ExpenseResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "expenseDate", direction = Sort.Direction.DESC) Pageable pageable) {

        return service.search(new ExpenseFilter(categoryId, from, to, paymentMethod, search), pageable);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse created = service.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}