import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { JobService } from '../../../core/services/job.service';
import { ApplicationService } from '../../../core/services/application.service';
import { AuthService } from '../../../core/services/auth.service';
import { JobResponseDto, Page } from '../../../core/models/job.model';
import { JobApplicationResponse, ApplicationStatus } from '../../../core/models/application.model';

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './recruiter-dashboard.component.html'
})
export class RecruiterDashboardComponent implements OnInit {
  myJobs       = signal<JobResponseDto[]>([]);
  applications = signal<JobApplicationResponse[]>([]);
  selectedJobId= signal<number | null>(null);
  loading      = signal(true);
  appsLoading  = signal(false);
  error        = signal('');
  successMsg   = signal('');
  deleteConfirmId = signal<number | null>(null);

  constructor(
    private jobService: JobService,
    private appService: ApplicationService,
    public  auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.jobService.getAllJobs(0, 50).subscribe({
      next: page => { this.myJobs.set(page.content); this.loading.set(false); },
      error: ()  => { this.error.set('Could not load jobs.'); this.loading.set(false); }
    });
  }

  viewApplicants(jobId: number) {
    this.selectedJobId.set(jobId);
    this.appsLoading.set(true);
    this.applications.set([]);
    this.appService.getJobApplications(jobId).subscribe({
      next: data => { this.applications.set(data); this.appsLoading.set(false); },
      error: ()  => { this.appsLoading.set(false); }
    });
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
    this.jobService.deleteJob(id).subscribe({
      next: () => {
        this.myJobs.update(jobs => jobs.filter(j => j.id !== id));
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
