import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar.component';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let authMock: any;
  let themeMock: any;
  let notificationMock: any;
  let routerMock: any;

  beforeEach(async () => {
    authMock = {
      isLoggedIn: signal(false),
      userName: signal(null),
      userRole: signal(null),
      logout: vi.fn()
    };
    themeMock = {
      isDarkMode: signal(false)
    };
    notificationMock = {
      notifications: signal([]),
      unreadCount: signal(0),
      refreshNotifications: vi.fn()
    };
    
    await TestBed.configureTestingModule({
      imports: [NavbarComponent, RouterTestingModule],
      providers: [
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authMock },
        { provide: ThemeService, useValue: themeMock },
        { provide: NotificationService, useValue: notificationMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    routerMock = TestBed.inject(Router);
    vi.spyOn(routerMock, 'navigate').mockImplementation(() => Promise.resolve(true));
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle mobile menu', () => {
    expect(component.mobileOpen()).toBe(false);
    component.toggleMobile();
    expect(component.mobileOpen()).toBe(true);
    component.closeMobile();
    expect(component.mobileOpen()).toBe(false);
  });

  it('should update scrolled state on window scroll', () => {
    expect(component.scrolled()).toBe(false);
    
    Object.defineProperty(window, 'scrollY', { value: 20, writable: true });
    window.dispatchEvent(new Event('scroll'));
    
    expect(component.scrolled()).toBe(true);

    window.scrollY = 0;
    window.dispatchEvent(new Event('scroll'));
    expect(component.scrolled()).toBe(false);
  });

  describe('dashboardLink computed property', () => {
    it('should return recruiter link for RECRUITER role', () => {
      authMock.userRole.set('RECRUITER');
      fixture.detectChanges();
      expect(component.dashboardLink()).toBe('/recruiter/dashboard');
    });

    it('should return admin link for ADMIN role', () => {
      authMock.userRole.set('ADMIN');
      fixture.detectChanges();
      expect(component.dashboardLink()).toBe('/admin/dashboard');
    });

    it('should return default link for others', () => {
      authMock.userRole.set('JOB_SEEKER');
      fixture.detectChanges();
      expect(component.dashboardLink()).toBe('/my-applications');
    });
  });

  it('should toggle notifications and refresh them', () => {
    expect(component.showNotifications()).toBe(false);
    component.toggleNotifications();
    expect(component.showNotifications()).toBe(true);
    expect(notificationMock.refreshNotifications).toHaveBeenCalled();
  });

  it('should close notifications when clicking outside', () => {
    component.showNotifications.set(true);
    fixture.detectChanges();
    
    // Simulate clicking away - we use a mock event to avoid document-level lifecycle errors
    component.onDocumentClick({ target: document.createElement('div') } as any);
    
    expect(component.showNotifications()).toBe(false);
  });

  it('should logout and close mobile menu', () => {
    component.mobileOpen.set(true);
    component.logout();
    expect(authMock.logout).toHaveBeenCalled();
    expect(component.mobileOpen()).toBe(false);
  });

  it('should navigate to login and register', () => {
    component.goToLogin();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/login']);
    
    component.goToRegister();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/register']);
  });
});
