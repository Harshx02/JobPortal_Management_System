import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center px-4 py-12"
         style="background:linear-gradient(135deg,#eff6ff 0%,#f0f9ff 50%,#ede9fe 100%)">

      <div class="w-full max-w-md animate-fade-in-up">
        <div class="text-center mb-8">
          <div class="inline-flex w-14 h-14 rounded-2xl items-center justify-center text-white font-bold text-xl mb-4"
               style="background:linear-gradient(135deg,#2563eb,#0ea5e9)">JP</div>
          <h1 class="text-3xl font-bold text-gray-900">Verify OTP</h1>
          <p class="text-gray-500 mt-1">We've sent a 6-digit code to {{ email }}</p>
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

          <form [formGroup]="otpForm" (ngSubmit)="verifyOtp()" novalidate>
            <div class="mb-6">
              <label class="form-label" for="otp">Enter 6-digit OTP</label>
              <input id="otp" type="text" formControlName="otp"
                     maxlength="6"
                     class="form-input text-center text-2xl tracking-[0.5em] font-bold"
                     placeholder="000000"
                     [class.border-red-400]="otpControl?.invalid && otpControl?.touched" />
              @if (otpControl?.errors?.['required'] && otpControl?.touched) {
                <p class="text-red-500 text-xs mt-1 text-center">OTP is required.</p>
              }
              @if (otpControl?.errors?.['pattern'] && otpControl?.touched) {
                <p class="text-red-500 text-xs mt-1 text-center">OTP must be 6 digits.</p>
              }
            </div>

            <button type="submit" class="btn-primary w-full justify-center py-3 text-base"
                    [disabled]="loading() || otpForm.invalid">
              @if (loading()) {
                <div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                Verifying...
              } @else {
                Verify OTP
              }
            </button>
          </form>

          <div class="mt-6 text-center text-sm text-gray-600">
            Didn't receive code?
            <a (click)="resendOtp()" class="text-blue-600 font-semibold hover:underline ml-1 cursor-pointer">Resend OTP</a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class VerifyOtpComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  email: string = '';
  otpForm = this.fb.group({
    otp: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]]
  });

  loading = signal(false);
  error = signal<string | null>(null);

  ngOnInit() {
    this.email = this.route.snapshot.queryParams['email'];
    if (!this.email) {
      this.router.navigate(['/auth/forgot-password']);
    }
  }

  get otpControl() { return this.otpForm.get('otp'); }

  verifyOtp() {
    if (this.otpForm.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    const emailParam = this.route.snapshot.queryParams['email']?.trim().toLowerCase();
    const otp = this.otpForm.value.otp!;
    
    this.authService.verifyOtp(emailParam, otp).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/auth/reset-password'], { queryParams: { email: emailParam } });
      },
      error: (err) => {
        this.loading.set(false);
        console.error('Verify OTP Error:', err);
        this.error.set(err.error?.error || err.error || 'Invalid or expired OTP.');
      }
    });
  }

  resendOtp() {
    this.authService.forgotPassword(this.email).subscribe({
      next: () => this.error.set(null),
      error: () => this.error.set('Failed to resend OTP.')
    });
  }
}
