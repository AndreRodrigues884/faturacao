/**
 * MODELO - formato dos erros da API (ProblemDetail, RFC 9457), igual para todos os endpoints.
 *
 * Fala com:     ninguém (só tipos)
 * É usado por:  componentes que mostram mensagens de erro (ex: Login)
 */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}