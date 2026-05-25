import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Collaborator, CollaboratorRequest } from '../models/collaborator.model';

@Injectable({ providedIn: 'root' })
export class CollaboratorService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/collaborators`;

  findAll(): Observable<Collaborator[]> {
    return this.http.get<Collaborator[]>(this.base);
  }

  findById(id: number): Observable<Collaborator> {
    return this.http.get<Collaborator>(`${this.base}/${id}`);
  }

  create(data: CollaboratorRequest): Observable<Collaborator> {
    return this.http.post<Collaborator>(this.base, data);
  }

  update(id: number, data: CollaboratorRequest): Observable<Collaborator> {
    return this.http.put<Collaborator>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
