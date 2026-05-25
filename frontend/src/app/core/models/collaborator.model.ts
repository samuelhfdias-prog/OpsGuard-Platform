export interface Collaborator {
  id: number;
  name: string;
  cpf: string;
  email: string;
  position: string;
  organizationId: number;
  organizationName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CollaboratorRequest {
  name: string;
  cpf: string;
  email: string;
  position: string;
  organizationId: number;
}
