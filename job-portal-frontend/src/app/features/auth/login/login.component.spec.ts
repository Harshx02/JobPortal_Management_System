import { TestBed, ComponentFixture } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { expect, vi, describe, it, beforeEach } from 'vitest';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: any;
  let router: any;
  let activatedRoute: any;

  beforeEach(async () => {
    authService = {
      isLoggedIn: vi.fn().mockReturnValue(false),
      login: vi.fn(),
      userRole: vi.fn().mockReturnValue('JOB_SEEKER')
    };

    router = {
      navigate: vi.fn(),
      navigateByUrl: vi.fn()
    };

    activatedRoute = {
      snapshot: {
        queryParamMap: {
          get: vi.fn().mockReturnValue(null)
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // NORMAL WORKING TEST
  it('should redirect if login is successful', () => {
    authService.login.mockReturnValue(of({ token: 'mock-token' }));
    component.form.setValue({ email: 'test@test.com', password: 'password123' });

    component.submit();

    expect(authService.login).toHaveBeenCalled();
    expect(component.loading()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  // BOUNDARY VALUE TEST
  it('should not submit if form is invalid (boundary: empty fields)', () => {
    component.form.setValue({ email: '', password: '' });
    
    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(authService.login).not.toHaveBeenCalled();
  });

  // EXCEPTION HANDLING TEST
  it('should set error signal if login fails (exception handling)', () => {
    const errorMsg = 'Invalid credentials';
    authService.login.mockReturnValue(throwError(() => ({ error: { message: errorMsg } })));
    component.form.setValue({ email: 'wrong@test.com', password: 'password123' });

    component.submit();

    expect(authService.login).toHaveBeenCalled();
    expect(component.loading()).toBe(false);
    expect(component.error()).toBe(errorMsg);
  });

  it('should redirect based on returnUrl after login', () => {
    const returnUrl = '/jobs/1';
    activatedRoute.snapshot.queryParamMap.get.mockReturnValue(returnUrl);
    authService.login.mockReturnValue(of({ token: 'jwt' }));
    component.form.setValue({ email: 'test@test.com', password: 'password123' });

    component.submit();

    expect(router.navigateByUrl).toHaveBeenCalledWith(returnUrl);
  });

  it('should redirect to recruiter dashboard if role is RECRUITER', () => {
    authService.login.mockReturnValue(of({ token: 'jwt' }));
    authService.userRole.mockReturnValue('RECRUITER');
    component.form.setValue({ email: 'rec@test.com', password: 'password123' });

    component.submit();

    expect(router.navigate).toHaveBeenCalledWith(['/recruiter/dashboard']);
  });

  it('should use default error message if error object is empty', () => {
    authService.login.mockReturnValue(throwError(() => ({})));
    component.form.setValue({ email: 'test@test.com', password: 'password' });

    component.submit();

    expect(component.error()).toBe('Invalid email or password. Please try again.');
  });

  it('should have working getters for email and password', () => {
    expect(component.email).toBeTruthy();
    expect(component.password).toBeTruthy();
  });

  describe('Constructor redirection', () => {
    it('should redirect to dashboard on init if already logged in', () => {
      authService.isLoggedIn.mockReturnValue(true);
      authService.userRole.mockReturnValue('ADMIN');
      
      // We need to re-create the component to trigger the constructor logic after changing mocks
      fixture = TestBed.createComponent(LoginComponent);
      component = fixture.componentInstance;
      
      expect(router.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
    });
  });
});
