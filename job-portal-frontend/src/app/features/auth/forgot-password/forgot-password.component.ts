import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center px-4 py-12"
         style="background:linear-gradient(135deg,#eff6ff 0%,#f0f9ff 50%,#ede9fe 100%)">

      <div class="w-full max-w-md animate-fade-in-up">
        <div class="text-center mb-8">
          <div class="inline-flex w-14 h-14 rounded-2xl items-center justify-center text-white font-bold text-xl mb-4"
               style="background:linear-gradient(135deg,#2563eb,#0ea5e9)">JP</div>
          <h1 class="text-3xl font-bold text-gray-900">Forgot Password?</h1>
          <p class="text-gray-500 mt-1">Enter your email to receive an OTP</p>
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

          <form [formGroup]="forgotForm" (ngSubmit)="sendOtp()" novalidate>
            <div class="mb-6">
              <label class="form-label" for="email">Email address</label>
              <input id="email" type="email" formControlName="email"
                     class="form-input" placeholder="you@example.com"
                     [class.border-red-400]="emailControl?.invalid && emailControl?.touched" />
              @if (emailControl?.errors?.['required'] && emailControl?.touched) {
                <p class="text-red-500 text-xs mt-1">Email is required.</p>
              }
              @if (emailControl?.errors?.['email'] && emailControl?.touched) {
                <p class="text-red-500 text-xs mt-1">Enter a valid email address.</p>
              }
            </div>

            <button type="submit" class="btn-primary w-full justify-center py-3 text-base"
                    [disabled]="loading() || forgotForm.invalid">
              @if (loading()) {
                <div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                Sending OTP...
              } @else {
                Send OTP
              }
            </button>
          </form>

          <div class="mt-6 text-center text-sm text-gray-600">
            Remembered your password?
            <a routerLink="/auth/login" class="text-blue-600 font-semibold hover:underline ml-1">Back to Login</a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  forgotForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  loading = signal(false);
  error = signal<string | null>(null);

  get emailControl() { return this.forgotForm.get('email'); }

  sendOtp() {
    if (this.forgotForm.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    const email = this.forgotForm.value.email?.trim().toLowerCase();
    if (!email) return;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/auth/verify-otp'], { queryParams: { email } });
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Forgot Password Error:', err);
        this.error.set(err.error?.error || err.error || 'Failed to send OTP.');
      }
    });
  }
}
