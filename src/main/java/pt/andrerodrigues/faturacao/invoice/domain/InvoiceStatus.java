package pt.andrerodrigues.faturacao.invoice.domain;

/**
 * ENUM - os estados possíveis de uma fatura.
 *   DRAFT     -> rascunho: pode ser alterada ou apagada, ainda não tem número
 *   ISSUED    -> emitida: tem número, já não pode ser alterada
 *   PAID      -> paga
 *   CANCELLED -> anulada (uma fatura emitida nunca se apaga, anula-se)
 *
 * Fala com:     ninguém
 * É usado por:  Invoice (campo status), InvoiceResponse
 */
public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PAID,
    CANCELLED
}