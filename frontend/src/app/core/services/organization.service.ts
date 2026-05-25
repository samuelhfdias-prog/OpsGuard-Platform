import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Organization, OrganizationRequest } from '../models/organization.model';

@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/organizations`;

  findAll(): Observable<Organization[]> {
    return this.http.get<Organization[]>(this.base);
  }

  findById(id: number): Observable<Organization> {
    return this.http.get<Organization>(`${this.base}/${id}`);
  }

  create(data: OrganizationRequest): Observable<Organization> {
    return this.http.post<Organization>(this.base, data);
  }

  update(id: number, data: OrganizationRequest): Observable<Organization> {
    return this.http.put<Organization>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
