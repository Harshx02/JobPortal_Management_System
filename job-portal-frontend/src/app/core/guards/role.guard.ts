import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/auth.model';

export const roleGuard = (allowedRoles: UserRole[]): CanActivateFn => {
  return () => {
    const auth   = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
      router.navigate(['/auth/login']);
      return false;
    }

    const role = auth.userRole();
    if (role && allowedRoles.includes(role)) {
      return true;
    }

    // Redirect to appropriate dashboard
    switch (auth.userRole()) {
      case 'RECRUITER': router.navigate(['/recruiter/dashboard']); break;
      case 'ADMIN':     router.navigate(['/admin/dashboard']);     break;
      default:          router.navigate(['/jobs']);                break;
    }
    return false;
  };
};
