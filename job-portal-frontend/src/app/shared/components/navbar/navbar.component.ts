import { Component, computed, signal, HostListener, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  mobileOpen = signal(false);
  scrolled    = signal(false);

  auth = inject(AuthService);
  themeService = inject(ThemeService);
  notificationService = inject(NotificationService);
  private router = inject(Router);

  isDarkMode = this.themeService.isDarkMode;
  notifications = this.notificationService.notifications;
  unreadCount = this.notificationService.unreadCount;
  showNotifications = signal(false);

  isLoggedIn  = this.auth.isLoggedIn;
  userName    = this.auth.userName;
  userRole    = this.auth.userRole;

  dashboardLink = computed(() => {
    switch (this.userRole()) {
      case 'RECRUITER': return '/recruiter/dashboard';
      case 'ADMIN':     return '/admin/dashboard';
      default:          return '/my-applications';
    }
  });

  constructor() {
    // Refresh notifications on component init
    this.notificationService.refreshNotifications();
  }

  @HostListener('window:scroll')
  onScroll() { this.scrolled.set(window.scrollY > 10); }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.notification-container')) {
      this.showNotifications.set(false);
    }
  }

  toggleMobile() { this.mobileOpen.update(v => !v); }
  closeMobile()  { this.mobileOpen.set(false); }
  
  toggleNotifications() {
    const newState = !this.showNotifications();
    this.showNotifications.set(newState);
    if (newState) {
      this.notificationService.refreshNotifications();
    }
  }

  logout() {
    this.auth.logout();
    this.closeMobile();
  }

  goToLogin()    { this.router.navigate(['/auth/login']);    }
  goToRegister() { this.router.navigate(['/auth/register']); }
}
