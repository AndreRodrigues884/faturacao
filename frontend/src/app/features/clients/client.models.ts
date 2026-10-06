/**
 * MODELOS - espelham os DTOs do backend: ClientResponse e ClientRequest.
 *
 * Fala com:     ninguém (só tipos)
 * É usado por:  ClientService, ClientList, ClientFormDialog
 */

export interface Client {
  id: number;
  name: string;
  nif: string;
  email: string | null;
  phone: string | null;
  address: string | null;
  postalCode: string | null;
  city: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ClientRequest {
  name: string;
  nif: string;
  email: string | null;
  phone: string | null;
  address: string | null;
  postalCode: string | null;
  city: string | null;
}