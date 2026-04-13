import { TestBed, ComponentFixture } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { expect, vi, describe, it, beforeEach } from 'vitest';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: any;
  let router: any;
  let activatedRoute: any;

  beforeEach(async () => {
    authService = {
      isLoggedIn: vi.fn().mockReturnValue(false),
      register: vi.fn(),
      userRole: vi.fn().mockReturnValue('JOB_SEEKER')
    };

    router = {
      navigate: vi.fn()
    };

    activatedRoute = {
      snapshot: {
        paramMap: {
          get: vi.fn().mockReturnValue(null)
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // NORMAL WORKING TEST
  it('should redirect if registration is successful', () => {
    authService.register.mockReturnValue(of({ token: 'mock-token' }));
    component.form.setValue({ 
        name: 'New User',
        email: 'new@test.com', 
        password: 'password123',
        role: 'JOB_SEEKER'
    });

    component.submit();

    expect(authService.register).toHaveBeenCalled();
    expect(component.loading()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  // BOUNDARY VALUE TEST
  it('should show error if name is too short (boundary)', () => {
    component.form.get('name')?.setValue('A');
    expect(component.form.get('name')?.invalid).toBe(true);
  });

  // EXCEPTION HANDLING TEST
  it('should set error signal if registration fails (exception handling)', () => {
    const errorMsg = 'Email already in use';
    authService.register.mockReturnValue(throwError(() => ({ error: { message: errorMsg } })));
    component.form.setValue({ 
        name: 'Existing User',
        email: 'exists@test.com', 
        password: 'password123',
        role: 'JOB_SEEKER'
    });

    component.submit();

    expect(authService.register).toHaveBeenCalled();
    expect(component.loading()).toBe(false);
    expect(component.error()).toBe(errorMsg);
  });

  it('should redirect to recruiter dashboard if role is RECRUITER', () => {
    authService.register.mockReturnValue(of({ token: 'jwt' }));
    authService.userRole.mockReturnValue('RECRUITER');
    component.form.setValue({ 
        name: 'Recruiter User',
        email: 'rec@test.com', 
        password: 'password123',
        role: 'RECRUITER'
    });

    component.submit();

    expect(router.navigate).toHaveBeenCalledWith(['/recruiter/dashboard']);
  });

  it('should redirect back to home if user is already logged in on init', () => {
    authService.isLoggedIn.mockReturnValue(true);
    
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    
    expect(router.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('should use default registration error message if response is empty', () => {
    authService.register.mockReturnValue(throwError(() => ({})));
    component.form.setValue({ 
        name: 'test', email: 'test@t.com', password: 'password', role: 'JOB_SEEKER' 
    });

    component.submit();

    expect(component.error()).toBe('Registration failed. Email may already be in use.');
  });

  it('should have working getters for form controls', () => {
    expect(component.name).toBeTruthy();
    expect(component.email).toBeTruthy();
    expect(component.password).toBeTruthy();
    expect(component.role).toBeTruthy();
  });
});
