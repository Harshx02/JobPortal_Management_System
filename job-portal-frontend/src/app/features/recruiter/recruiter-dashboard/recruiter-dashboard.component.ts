import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { JobService } from '../../../core/services/job.service';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { JobResponseDto, Page } from '../../../core/models/job.model';
import { JobApplicationResponse, ApplicationStatus } from '../../../core/models/application.model';
import { Subject, takeUntil } from 'rxjs';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent, PaginationComponent],
  templateUrl: './recruiter-dashboard.component.html'
})
export class RecruiterDashboardComponent implements OnInit, OnDestroy {
  myJobs       = signal<JobResponseDto[]>([]);
  jobsTotalPages = signal(0);
  jobsPage       = signal(0);
  
  applications = signal<JobApplicationResponse[]>([]);
  appsTotalPages = signal(0);
  appsPage       = signal(0);
  
  selectedJobId= signal<number | null>(null);
  loading      = signal(true);
  appsLoading  = signal(false);
  error        = signal('');
  successMsg   = signal('');
  deleteConfirmId = signal<number | null>(null);

  private destroy$ = new Subject<void>();

  constructor(
    private jobService: JobService,
    private appService: ApplicationService,
    public  auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadJobs();
  }

  loadJobs(page = this.jobsPage()) {
    this.loading.set(true);
    // Updated JobService already uses true pagination
    this.jobService.getAllJobs(page, 10)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => { 
          this.myJobs.set(res.content); 
          this.jobsTotalPages.set(res.totalPages);
          this.loading.set(false); 
        },
        error: ()  => { this.error.set('Could not load jobs.'); this.loading.set(false); }
      });
  }

  onJobsPageChange(p: number) {
    this.jobsPage.set(p);
    this.loadJobs(p);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  viewApplicants(jobId: number, page = 0) {
    this.selectedJobId.set(jobId);
    this.appsPage.set(page);
    this.appsLoading.set(true);
    
    this.appService.getJobApplications(jobId, page, 5)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => { 
          this.applications.set(res.content); 
          this.appsTotalPages.set(res.totalPages);
          this.appsLoading.set(false); 
        },
        error: ()  => { this.appsLoading.set(false); }
      });
  }

  onAppsPageChange(p: number) {
    if (this.selectedJobId()) {
      this.viewApplicants(this.selectedJobId()!, p);
    }
  }

  updateStatus(appId: number, status: ApplicationStatus) {
    this.appService.updateStatus(appId, status).subscribe({
      next: () => {
        this.successMsg.set(`Application marked as ${status}`);
        if (this.selectedJobId()) this.viewApplicants(this.selectedJobId()!);
        setTimeout(() => this.successMsg.set(''), 3000);
      }
    });
  }

  deleteJob(id: number) {
    this.jobService.deleteJob(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
      next: () => {
        this.loadJobs(); // Refresh page
        this.deleteConfirmId.set(null);
        if (this.selectedJobId() === id) { this.selectedJobId.set(null); this.applications.set([]); }
      }
    });
  }

  editJob(id: number)  { this.router.navigate(['/recruiter/edit-job', id]); }
  postNew()            { this.router.navigate(['/recruiter/post-job']); }

  formatSalary(s: number): string {
    if (s >= 100000) return `₹${(s / 100000).toFixed(1)}L`;
    if (s >= 1000)   return `₹${(s / 1000).toFixed(0)}K`;
    return `₹${s}`;
  }

  statusColor(s: ApplicationStatus): string {
    switch (s) {
      case 'ACCEPTED':     return 'badge-green';
      case 'REJECTED':     return 'badge-red';
      case 'UNDER_REVIEW': return 'badge-yellow';
      default:             return 'badge-blue';
    }
  }
}
