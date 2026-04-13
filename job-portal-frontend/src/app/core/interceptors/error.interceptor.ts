import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 401:
            // Handled by Auth flow usually, but could redirect to login
            errorMessage = 'Session expired. Please login again.';
            // router.navigate(['/auth/login']);
            break;
          case 403:
            errorMessage = 'Access Forbidden: You do not have permission.';
            router.navigate(['/403']);
            break;
          case 404:
            errorMessage = 'Resource not found.';
            router.navigate(['/404']);
            break;
          case 500:
            errorMessage = 'Internal Server Error. Please try again later.';
            router.navigate(['/500']);
            break;
          default:
            errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
      }

      toast.error(errorMessage);
      return throwError(() => new Error(errorMessage));
    })
  );
};
