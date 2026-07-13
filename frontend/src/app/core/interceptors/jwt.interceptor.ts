import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();
  const isApiRequest = req.url === environment.apiUrl || req.url.startsWith(`${environment.apiUrl}/`);
  const isLoginRequest = req.url.endsWith('/auth/login');

  const request = token && isApiRequest
    ? req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    })
    : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isLoginRequest && auth.isAuthenticated()) {
        auth.logout();
      }
      return throwError(() => error);
    })
  );
};
