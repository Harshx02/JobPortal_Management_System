import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { ApplicationService } from '../../../core/services/application.service';
import { ApplicationResponse, ApplicationStatus } from '../../../core/models/application.model';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, NavbarComponent, RouterLink],
  templateUrl: './my-applications.component.html'
})
export class MyApplicationsComponent implements OnInit {
  applications = signal<ApplicationResponse[]>([]);
  loading      = signal(true);
  error        = signal('');

  constructor(
    private appService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.appService.getUserApplications().subscribe({
      next: data => { this.applications.set(data); this.loading.set(false); },
      error: ()  => { this.error.set('Could not load your applications.'); this.loading.set(false); }
    });
  }

  statusColor(s: ApplicationStatus): string {
    switch (s) {
      case 'ACCEPTED':     return 'badge-green';
      case 'REJECTED':     return 'badge-red';
      case 'UNDER_REVIEW': return 'badge-yellow';
      default:             return 'badge-blue';
    }
  }

  statusIcon(s: ApplicationStatus): string {
    switch (s) {
      case 'ACCEPTED':     return '✅';
      case 'REJECTED':     return '❌';
      case 'UNDER_REVIEW': return '🔍';
      default:             return '⏳';
    }
  }

  viewJob(jobId?: number) {
    if (jobId) {
      this.router.navigate(['/jobs', jobId]);
    }
  }

  getCount(status: string): number {
    return this.applications().filter(a => a.status === status).length;
  }
}
