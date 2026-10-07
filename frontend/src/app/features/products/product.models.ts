/**
 * MODELOS - espelham os DTOs do backend (ProductResponse, ProductRequest) e os enums
 * ProductType e VatRate, com as etiquetas em português para mostrar no ecrã.
 *
 * Fala com:     ninguém (só tipos e constantes)
 * É usado por:  ProductService, ProductList, ProductFormDialog
 */

export type ProductType = 'PRODUCT' | 'SERVICE';
export type VatRate = 'NORMAL' | 'INTERMEDIATE' | 'REDUCED';

export const PRODUCT_TYPE_LABELS: Record<ProductType, string> = {
  PRODUCT: 'Produto',
  SERVICE: 'Serviço',
};

export const VAT_RATE_LABELS: Record<VatRate, string> = {
  NORMAL: 'Normal (23%)',
  INTERMEDIATE: 'Intermédia (13%)',
  REDUCED: 'Reduzida (6%)',
};

export interface Product {
  id: number;
  code: string;
  name: string;
  description: string | null;
  type: ProductType;
  unitPrice: number;
  vatRate: VatRate;
  vatPercentage: number;
  vatAmount: number;
  priceWithVat: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductRequest {
  code: string;
  name: string;
  description: string | null;
  type: ProductType;
  unitPrice: number;
  vatRate: VatRate;
}