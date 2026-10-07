package pt.andrerodrigues.faturacao.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.andrerodrigues.faturacao.dashboard.dto.DashboardResponse;

/**
 * CONTROLLER - porta de entrada HTTP do dashboard (/api/dashboard?year=2026).
 * Sem ano, usa o ano atual. Acessível a qualquer utilizador autenticado.
 *
 * Fala com:     DashboardService
 * É usado por:  clientes externos (Angular, página inicial)
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse get(@RequestParam(required = false) Integer year) {
        return service.forYear(year);
    }
}