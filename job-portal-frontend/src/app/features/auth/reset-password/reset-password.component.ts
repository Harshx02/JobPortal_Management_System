import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center px-4 py-12"
         style="background:linear-gradient(135deg,#eff6ff 0%,#f0f9ff 50%,#ede9fe 100%)">

      <div class="w-full max-w-md animate-fade-in-up">
        <div class="text-center mb-8">
          <div class="inline-flex w-14 h-14 rounded-2xl items-center justify-center text-white font-bold text-xl mb-4"
               style="background:linear-gradient(135deg,#2563eb,#0ea5e9)">JP</div>
          <h1 class="text-3xl font-bold text-gray-900">Reset Password</h1>
          <p class="text-gray-500 mt-1">Set a new password for {{ email }}</p>
        </div>

        <div class="card p-8 shadow-xl">
          @if (error()) {
            <div class="mb-5 p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-center gap-2 animate-fade-in">
              <svg class="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
              </svg>
              {{ error() }}
            </div>
          }

          <form [formGroup]="resetForm" (ngSubmit)="resetPassword()" novalidate>
            <div class="mb-5">
              <label class="form-label" for="password">New Password</label>
              <input id="password" type="password" formControlName="newPassword"
                     class="form-input" placeholder="••••••••"
                     [class.border-red-400]="newPasswordControl?.invalid && newPasswordControl?.touched" />
              @if (newPasswordControl?.errors?.['required'] && newPasswordControl?.touched) {
                <p class="text-red-500 text-xs mt-1">New password is required.</p>
              }
              @if (newPasswordControl?.errors?.['minlength'] && newPasswordControl?.touched) {
                <p class="text-red-500 text-xs mt-1">Password must be at least 6 characters.</p>
              }
            </div>

            <div class="mb-6">
              <label class="form-label" for="confirmPassword">Confirm Password</label>
              <input id="confirmPassword" type="password" formControlName="confirmPassword"
                     class="form-input" placeholder="••••••••"
                     [class.border-red-400]="resetForm.errors?.['mismatch'] && confirmPasswordControl?.touched" />
              @if (resetForm.errors?.['mismatch'] && confirmPasswordControl?.touched) {
                <p class="text-red-500 text-xs mt-1">Passwords do not match.</p>
              }
            </div>

            <button type="submit" class="btn-primary w-full justify-center py-3 text-base"
                    [disabled]="loading() || resetForm.invalid">
              @if (loading()) {
                <div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                Resetting Password...
              } @else {
                Reset Password
              }
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  email: string = '';
  resetForm = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });

  loading = signal(false);
  error = signal<string | null>(null);

  ngOnInit() {
    this.email = this.route.snapshot.queryParams['email'];
    if (!this.email) {
      this.router.navigate(['/auth/forgot-password']);
    }
  }

  get newPasswordControl() { return this.resetForm.get('newPassword'); }
  get confirmPasswordControl() { return this.resetForm.get('confirmPassword'); }

  passwordMatchValidator(g: any) {
    return g.get('newPassword').value === g.get('confirmPassword').value
      ? null : { mismatch: true };
  }

  resetPassword() {
    if (this.resetForm.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    const normalizedEmail = this.email?.trim().toLowerCase();
    const payload = {
      email: normalizedEmail,
      newPassword: this.resetForm.value.newPassword!
    };

    this.authService.resetPassword(payload).subscribe({
      next: () => {
        this.loading.set(false);
        alert('Password reset successfully! Please login with your new password.');
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Reset Password Error:', err);
        this.error.set(err.error?.error || err.error || 'Failed to reset password.');
      }
    });
  }
}
