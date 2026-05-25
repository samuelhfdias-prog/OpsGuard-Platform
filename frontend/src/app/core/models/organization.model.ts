export interface Organization {
  id: number;
  name: string;
  cnpj: string;
  address: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface OrganizationRequest {
  name: string;
  cnpj: string;
  address?: string;
}
