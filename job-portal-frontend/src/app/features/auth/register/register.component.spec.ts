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
});
