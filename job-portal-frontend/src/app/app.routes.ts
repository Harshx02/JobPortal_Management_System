import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { deactivateGuard } from './core/guards/deactivate.guard';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  // Public – Auth
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
       {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      {
        path: 'verify-otp',
        loadComponent: () => import('./features/auth/verify-otp/verify-otp.component').then(m => m.VerifyOtpComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // Public – Home
  {
    path: 'home',
    loadComponent: () => import('./features/jobs/home/home.component').then(m => m.HomeComponent)
  },

  // Public – Jobs
  {
    path: 'jobs',
    loadComponent: () => import('./features/jobs/job-search/job-search.component').then(m => m.JobSearchComponent)
  },
  {
    path: 'jobs/:id',
    loadComponent: () => import('./features/jobs/job-detail/job-detail.component').then(m => m.JobDetailComponent)
  },

  // Protected – Apply
  {
    path: 'apply/:id',
    loadComponent: () => import('./features/applications/apply-job/apply-job.component').then(m => m.ApplyJobComponent),
    canActivate: [authGuard, roleGuard(['JOB_SEEKER'])],
    canDeactivate: [deactivateGuard]
  },

  // Protected – Profile
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile-page/profile-page.component').then(m => m.ProfilePageComponent),
    canActivate: [authGuard],
    canDeactivate: [deactivateGuard]
  },

  // Protected – Job Seeker Dashboard
  {
    path: 'my-applications',
    loadComponent: () => import('./features/applications/my-applications/my-applications.component').then(m => m.MyApplicationsComponent),
    canActivate: [authGuard, roleGuard(['JOB_SEEKER'])]
  },

  // Protected – Recruiter
  {
    path: 'recruiter',
    canActivate: [authGuard, roleGuard(['RECRUITER'])],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/recruiter/recruiter-dashboard/recruiter-dashboard.component').then(m => m.RecruiterDashboardComponent)
      },
      {
        path: 'post-job',
        loadComponent: () => import('./features/recruiter/post-job/post-job.component').then(m => m.PostJobComponent),
        canDeactivate: [deactivateGuard]
      },
      {
        path: 'edit-job/:id',
        loadComponent: () => import('./features/recruiter/post-job/post-job.component').then(m => m.PostJobComponent),
        canDeactivate: [deactivateGuard]
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Protected – Admin
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ADMIN'])],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Error Pages
  {
    path: '403',
    loadComponent: () => import('./shared/components/error-page/error-page.component').then(m => m.ErrorPageComponent),
    data: { code: '403', title: 'Forbidden', message: "You don't have permission to be here." }
  },
  {
    path: '404',
    loadComponent: () => import('./shared/components/error-page/error-page.component').then(m => m.ErrorPageComponent),
    data: { code: '404', title: 'Not Found', message: "Oops! This page doesn't exist." }
  },
  {
    path: '500',
    loadComponent: () => import('./shared/components/error-page/error-page.component').then(m => m.ErrorPageComponent),
    data: { code: '500', title: 'Server Error', message: "Our servers are taking a nap. Try again later." }
  },

  // Wildcard
  { path: '**', redirectTo: '/404' }
];
