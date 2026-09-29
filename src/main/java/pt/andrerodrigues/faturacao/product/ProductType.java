package pt.andrerodrigues.faturacao.product;

/**
 * ENUM - os tipos possíveis de artigo: produto físico ou serviço.
 *
 * Fala com:     ninguém
 * É usado por:  Product (campo type), ProductRequest, ProductResponse
 */
public enum ProductType {
    PRODUCT,
    SERVICE
}