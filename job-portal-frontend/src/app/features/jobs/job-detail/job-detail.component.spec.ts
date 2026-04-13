// @vitest-environment jsdom
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { JobDetailComponent } from './job-detail.component';
import { JobService } from '../../../core/services/job.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { expect, vi, describe, it, beforeEach } from 'vitest';
import { signal } from '@angular/core';

describe('JobDetailComponent', () => {
  let component: JobDetailComponent;
  let fixture: ComponentFixture<JobDetailComponent>;
  let jobService: any;
  let authService: any;
  let router: any;
  let activatedRoute: any;

  beforeEach(() => {
    // Mock matchMedia for JSDOM compatibility (ThemeService requirement)
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation(query => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(), // deprecated
        removeListener: vi.fn(), // deprecated
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });

    jobService = {
      getJobById: vi.fn()
    };

    authService = {
      isLoggedIn: signal(false),
      userRole: signal('JOB_SEEKER'),
      userName: signal('Test User')
    };

    router = {
      navigate: vi.fn(),
      navigateByUrl: vi.fn(),
      events: of()
    };

    activatedRoute = {
      snapshot: {
        paramMap: {
          get: vi.fn().mockReturnValue('1')
        }
      }
    };

    TestBed.configureTestingModule({
      imports: [JobDetailComponent],
      providers: [
        { provide: JobService, useValue: jobService },
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: activatedRoute },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();
  });

  const setupJob = (jobData: any) => {
    jobService.getJobById.mockReturnValue(of(jobData));
    fixture = TestBed.createComponent(JobDetailComponent);
    component = fixture.componentInstance;
    
    // Explicitly set signals if they are managed by AuthService
    authService.isLoggedIn.set(true);
    authService.userRole.set('JOB_SEEKER');
    
    fixture.detectChanges();
  }

  it('should create and load job', () => {
    setupJob({ 
      id: 1, 
      title: 'Test Job', 
      companyName: 'Google', 
      location: 'Bangalore', 
      salary: 500000,
      experience: 2,
      description: 'Job Description'
    });
    expect(component).toBeTruthy();
    expect(component.job()?.title).toBe('Test Job');
    expect(component.loading()).toBe(false);
  });

  // NORMAL TEST: Formatter
  it('should format salary correctly (normal/boundary)', () => {
    setupJob({ 
      id: 1, 
      title: 'Test Job', 
      companyName: 'Google', 
      salary: 1500000 
    });
    expect(component.formatSalary(1500000)).toBe('₹15.0L / year');
    expect(component.formatSalary(50000)).toBe('₹50K / year');
    expect(component.formatSalary(500)).toBe('₹500 / year');
  });

  // EXCEPTION HANDLING TEST
  it('should set error signal if job loading fails', () => {
    jobService.getJobById.mockReturnValue(throwError(() => new Error('Not found')));
    fixture = TestBed.createComponent(JobDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.error()).toBe('Job not found or unavailable.');
    expect(component.loading()).toBe(false);
  });

  // NAVIGATION TEST (NORMAL)
  it('should navigate to login if applying while logged out', () => {
    setupJob({ 
      id: 1, 
      title: 'Test Job',
      companyName: 'Google'
    });
    authService.isLoggedIn.set(false);
    
    component.apply();

    expect(router.navigate).toHaveBeenCalledWith(['/auth/login'], expect.anything());
  });
});
