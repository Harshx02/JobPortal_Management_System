import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar.component';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal, Component } from '@angular/core';

@Component({ standalone: true, template: '' })
class DummyComponent {}

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
      imports: [
        NavbarComponent, 
        RouterTestingModule.withRoutes([
          { path: '**', component: DummyComponent }
        ])
      ],
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
    vi.spyOn(routerMock, 'navigateByUrl').mockImplementation(() => Promise.resolve(true));
    
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
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

  it('should not close notifications when clicking inside container', () => {
    component.showNotifications.set(true);
    fixture.detectChanges();
    
    const div = document.createElement('div');
    div.className = 'notification-container';
    component.onDocumentClick({ target: div } as any);
    
    expect(component.showNotifications()).toBe(true);
  });

  it('should toggle notifications off and not refresh', () => {
    notificationMock.refreshNotifications.mockClear();
    component.showNotifications.set(true);
    
    component.toggleNotifications(); // Should set it to false
    expect(component.showNotifications()).toBe(false);
    expect(notificationMock.refreshNotifications).not.toHaveBeenCalled();
  });

  it('should show "Post a Job" only for RECRUITER', () => {
    authMock.isLoggedIn.set(true);
    authMock.userRole.set('RECRUITER');
    fixture.detectChanges();
    let compiled = fixture.nativeElement;
    expect(compiled.textContent).toContain('Post a Job');

    authMock.userRole.set('JOB_SEEKER');
    fixture.detectChanges();
    expect(compiled.textContent).not.toContain('Post a Job');
  });

  it('should show unread count badge when count > 0', () => {
    authMock.isLoggedIn.set(true);
    notificationMock.unreadCount.set(5);
    fixture.detectChanges();
    let badge = fixture.nativeElement.querySelector('.bg-red-500');
    expect(badge).toBeTruthy();
    expect(badge.textContent).toContain('5');
  });

  it('should call themeService.toggleTheme on button click', () => {
    themeMock.toggleTheme = vi.fn();
    fixture.detectChanges();
    const themeBtn = fixture.nativeElement.querySelector('button[title*="Mode"]');
    themeBtn.click();
    expect(themeMock.toggleTheme).toHaveBeenCalled();
  });

  it('should close mobile menu when clicking navigation links', () => {
    component.mobileOpen.set(true);
    fixture.detectChanges();
    const spy = vi.spyOn(component, 'closeMobile');
    
    const homeLink = fixture.nativeElement.querySelector('a[routerLink="/home"]');
    homeLink.click();
    expect(spy).toHaveBeenCalled();
  });

  it('should call logout and close mobile on mobile logout button', () => {
    authMock.isLoggedIn.set(true);
    component.mobileOpen.set(true);
    fixture.detectChanges();
    
    const logoutBtn = Array.from(fixture.nativeElement.querySelectorAll('button'))
      .find((b: any) => b.textContent.includes('Logout')) as HTMLButtonElement;
    
    logoutBtn.click();
    expect(authMock.logout).toHaveBeenCalled();
    expect(component.mobileOpen()).toBe(false);
  });

  it('should cover notification type branches', () => {
    authMock.isLoggedIn.set(true);
    component.showNotifications.set(true);
    notificationMock.notifications.set([
      { id: 1, message: 'Success', type: 'SUCCESS', isRead: false, timestamp: new Date() },
      { id: 2, message: 'Warning', type: 'WARNING', isRead: false, timestamp: new Date() },
      { id: 3, message: 'Info', type: 'INFO', isRead: true, timestamp: new Date() }
    ]);
    fixture.detectChanges();
    
    const notes = fixture.nativeElement.querySelectorAll('.rounded-full');
    expect(notes.length).toBeGreaterThanOrEqual(3);
  });

  it('should handle null username in template', () => {
    authMock.isLoggedIn.set(true);
    authMock.userName.set(null);
    fixture.detectChanges();
    
    const avatar = fixture.nativeElement.querySelector('.bg-blue-600');
    expect(avatar.textContent).toContain('U');
  });

  it('should click mobile "Post a Job" link', () => {
    authMock.isLoggedIn.set(true);
    authMock.userRole.set('RECRUITER');
    component.mobileOpen.set(true);
    fixture.detectChanges();
    
    const postJobLink = Array.from(fixture.nativeElement.querySelectorAll('a'))
      .find((a: any) => a.textContent.includes('Post a Job') && a.classList.contains('justify-start')) as HTMLAnchorElement;
    
    expect(postJobLink).toBeTruthy();
    postJobLink.click();
    expect(component.mobileOpen()).toBe(false);
  });

  it('should click mobile "My Profile" link', () => {
    authMock.isLoggedIn.set(true);
    component.mobileOpen.set(true);
    fixture.detectChanges();
    
    const profileLink = Array.from(fixture.nativeElement.querySelectorAll('a'))
      .find((a: any) => a.textContent.includes('My Profile') && a.classList.contains('justify-start')) as HTMLAnchorElement;
    
    profileLink.click();
    expect(component.mobileOpen()).toBe(false);
  });
});
