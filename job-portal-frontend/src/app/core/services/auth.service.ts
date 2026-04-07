import { Injectable, signal, computed, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;

  // ── Signals for reactive state ──
  private _token    = signal<string | null>(this.readToken());
  private _user     = signal<AuthResponse | null>(this.readUser());

  readonly token    = this._token.asReadonly();
  readonly user     = this._user.asReadonly();
  readonly isLoggedIn = computed(() => !!this._token());
  readonly userRole   = computed(() => this._user()?.role ?? null);
  readonly userName   = computed(() => this._user()?.name ?? null);
  readonly userId     = computed(() => this._user()?.userId ?? this.readUserId() ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  // ── Auth endpoints ──
  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap(res => this.storeSession(res))
    );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload).pipe(
      tap(res => this.storeSession(res))
    );
  }

  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/profile`);
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, { responseType: 'text' });
  }

  verifyOtp(email: string, otp: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp }, { responseType: 'text' });
  }

  resetPassword(payload: any): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, payload, { responseType: 'text' });
  }


  logout(): void {
    localStorage.removeItem('jp_token');
    localStorage.removeItem('jp_user');
    localStorage.removeItem('jp_userId');
    this._token.set(null);
    this._user.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  // ── Private helpers ──
  private storeSession(res: AuthResponse): void {
    localStorage.setItem('jp_token', res.token);
    localStorage.setItem('jp_user', JSON.stringify(res));
    if (res.userId) {
      localStorage.setItem('jp_userId', res.userId.toString());
    }
    this._token.set(res.token);
    this._user.set(res);
  }

  private readToken(): string | null {
    return localStorage.getItem('jp_token');
  }

  private readUser(): AuthResponse | null {
    const raw = localStorage.getItem('jp_user');
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }

  private readUserId(): number | null {
    const raw = localStorage.getItem('jp_userId');
    return raw ? Number(raw) : null;
  }
}
