/**
 * MODELOS - espelham os DTOs do backend: CategoryResponse e CategoryRequest.
 *
 * Fala com:     ninguém (só tipos)
 * É usado por:  CategoryService, CategoryList, CategoryFormDialog
 */

export interface Category {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
}

export interface CategoryRequest {
  name: string;
  description: string | null;
}