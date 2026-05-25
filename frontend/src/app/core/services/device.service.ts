import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Device, DeviceRequest } from '../models/device.model';

@Injectable({ providedIn: 'root' })
export class DeviceService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/devices`;

  findAll(): Observable<Device[]> {
    return this.http.get<Device[]>(this.base);
  }

  findById(id: number): Observable<Device> {
    return this.http.get<Device>(`${this.base}/${id}`);
  }

  create(data: DeviceRequest): Observable<Device> {
    return this.http.post<Device>(this.base, data);
  }

  update(id: number, data: DeviceRequest): Observable<Device> {
    return this.http.put<Device>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
