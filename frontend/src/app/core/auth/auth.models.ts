/**
 * MODELOS - a forma dos dados de autenticação trocados com a API.
 * Espelham os DTOs do backend: LoginRequest, LoginResponse e UserResponse.
 *
 * Fala com:     ninguém (só tipos)
 * É usado por:  AuthService, Login, Shell
 */

export type Role = 'ADMIN' | 'USER';

export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}