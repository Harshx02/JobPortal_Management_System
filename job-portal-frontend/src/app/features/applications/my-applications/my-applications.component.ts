import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { ApplicationService } from '../../../core/services/application.service';
import { ApplicationResponse, ApplicationStatus } from '../../../core/models/application.model';
import { Subject, takeUntil } from 'rxjs';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { Page } from '../../../core/models/job.model';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, NavbarComponent, RouterLink, PaginationComponent],
  templateUrl: './my-applications.component.html'
})
export class MyApplicationsComponent implements OnInit, OnDestroy {
  applications = signal<ApplicationResponse[]>([]);
  totalElements = signal(0);
  totalPages = signal(0);
  page = signal(0);
  loading      = signal(true);
  error        = signal('');

  private destroy$ = new Subject<void>();

  constructor(
    private appService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications(p = this.page()) {
    this.loading.set(true);
    this.appService.getUserApplications(p, 10)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res: Page<ApplicationResponse>) => {
          this.applications.set(res.content);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages);
          this.loading.set(false);
        },
        error: () => { this.error.set('Could not load your applications.'); this.loading.set(false); }
      });
  }

  onPageChange(p: number) {
    this.page.set(p);
    this.loadApplications(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
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
