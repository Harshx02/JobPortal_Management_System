import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AdminService } from '../../../core/services/admin.service';
import { ApplicationService } from '../../../core/services/application.service';
import { UserResponse } from '../../../core/models/auth.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  users       = signal<UserResponse[]>([]);
  totalJobs   = signal(0);
  totalApps   = signal(0);
  activeTab   = signal<'users' | 'jobs'>('users');
  loading     = signal(true);
  error       = signal('');
  successMsg  = signal('');

  constructor(
    private adminService: AdminService,
    private appService: ApplicationService
  ) {}

  ngOnInit() {
    this.adminService.getAllUsers().subscribe({
      next: u  => { this.users.set(u); this.loading.set(false); },
      error: () => { this.error.set('Could not load users.'); this.loading.set(false); }
    });
    this.adminService.getAllJobs().subscribe({
      next: res => this.totalJobs.set(res.totalElements)
    });
    this.appService.getTotalCount().subscribe({
      next: n => this.totalApps.set(n)
    });
  }

  deleteUser(id: number) {
    this.adminService.deleteUser(id).subscribe({
      next: res => {
        this.users.update(u => u.filter(x => x.id !== id));
        this.successMsg.set(res.message || 'User deleted.');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => this.error.set('Failed to delete user.')
    });
  }

  roleColor(role: string): string {
    switch (role) {
      case 'ADMIN':     return 'badge-red';
      case 'RECRUITER': return 'badge-purple';
      default:          return 'badge-blue';
    }
  }

  get seekers()   { return this.users().filter(u => u.role === 'JOB_SEEKER').length; }
  get recruiters(){ return this.users().filter(u => u.role === 'RECRUITER').length; }
}
