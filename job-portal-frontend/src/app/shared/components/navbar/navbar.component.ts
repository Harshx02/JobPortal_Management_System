import { Component, computed, signal, HostListener, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

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
  private router = inject(Router);

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

  constructor() {}

  @HostListener('window:scroll')
  onScroll() { this.scrolled.set(window.scrollY > 10); }

  toggleMobile() { this.mobileOpen.update(v => !v); }
  closeMobile()  { this.mobileOpen.set(false); }

  logout() {
    this.auth.logout();
    this.closeMobile();
  }

  goToLogin()    { this.router.navigate(['/auth/login']);    }
  goToRegister() { this.router.navigate(['/auth/register']); }
}
