import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser, LoginRequest, AuthResponse } from '../models/auth.model';

const STORAGE_KEY = 'opsguard_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http: HttpClient;
  private router: Router;

  private _user = signal<AuthUser | null>(this.loadFromStorage());

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => !!this._user());
  readonly isManager = computed(() => this._user()?.role === 'MANAGER');
  readonly currentOrganizationId = computed(() => this._user()?.organizationId ?? null);

  constructor(http: HttpClient, router: Router) {
    this.http = http;
    this.router = router;
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap(response => {
        const user: AuthUser = {
          token: response.token,
          email: response.email,
          name: response.name,
          role: response.role,
          organizationId: response.organizationId
        };
        this._user.set(user);
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user));
      })
    );
  }

  logout(): void {
    this._user.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._user()?.token ?? null;
  }

  private loadFromStorage(): AuthUser | null {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return null;

      const parsed = JSON.parse(raw) as Partial<AuthUser>;
      if (!this.isStoredUser(parsed) || this.isTokenExpired(parsed.token)) {
        sessionStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  private isStoredUser(value: Partial<AuthUser>): value is AuthUser {
    return typeof value.token === 'string' &&
      typeof value.email === 'string' &&
      typeof value.name === 'string' &&
      (value.role === 'MANAGER' || value.role === 'OPERATOR') &&
      (typeof value.organizationId === 'number' || value.organizationId === null);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1];
      if (!payload) return true;
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
      const { exp } = JSON.parse(atob(padded)) as { exp?: number };
      return typeof exp !== 'number' || exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }
}
