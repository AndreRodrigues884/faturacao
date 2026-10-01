package pt.andrerodrigues.faturacao.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO DE SAÍDA GENÉRICO - uma página de resultados, para qualquer tipo de dados.
 * Ex: PageResponse<InvoiceSummaryResponse> é uma página de resumos de faturas.
 *
 * Fala com:     Page (do Spring Data, de onde copia os dados)
 * É usado por:  qualquer service que devolva listas paginadas (ex: InvoiceService)
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}