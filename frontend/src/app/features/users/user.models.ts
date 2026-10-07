import { Role } from '../../core/auth/auth.models';

/**
 * MODELOS - pedidos da gestão de utilizadores (espelham CreateUserRequest e UpdateUserRequest),
 * e as etiquetas dos papéis. O utilizador em si (User) já existe nos modelos da autenticação.
 *
 * Fala com:     Role (core/auth)
 * É usado por:  UserService, UserList, UserFormDialog
 */

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Administrador',
  USER: 'Utilizador',
};

export interface CreateUserRequest {
  email: string;
  name: string;
  password: string;
  role: Role;
}

export interface UpdateUserRequest {
  name: string;
  role: Role;
}