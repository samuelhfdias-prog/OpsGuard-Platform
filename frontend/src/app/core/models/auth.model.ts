export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  email: string;
  name: string;
  role: 'MANAGER' | 'OPERATOR';
  organizationId: number | null;
}

export interface AuthUser {
  token: string;
  email: string;
  name: string;
  role: 'MANAGER' | 'OPERATOR';
  organizationId: number | null;
}
