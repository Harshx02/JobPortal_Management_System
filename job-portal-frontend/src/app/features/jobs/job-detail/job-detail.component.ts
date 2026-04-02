import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { JobService } from '../../../core/services/job.service';
import { AuthService } from '../../../core/services/auth.service';
import { JobResponseDto } from '../../../core/models/job.model';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './job-detail.component.html'
})
export class JobDetailComponent implements OnInit {
  job     = signal<JobResponseDto | null>(null);
  loading = signal(true);
  error   = signal('');

  auth = inject(AuthService);
  isLoggedIn  = this.auth.isLoggedIn;
  userRole    = this.auth.userRole;
  canApply    = computed(() => this.userRole() === 'JOB_SEEKER');

  private id!: number;

  constructor(
    private jobService: JobService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.jobService.getJobById(this.id).subscribe({
      next: j  => { this.job.set(j); this.loading.set(false); },
      error: () => { this.error.set('Job not found or unavailable.'); this.loading.set(false); }
    });
  }

  apply() {
    if (!this.isLoggedIn()) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: `/apply/${this.id}` } });
    } else {
      this.router.navigate(['/apply', this.id]);
    }
  }

  goBack() { this.router.navigate(['/jobs']); }

  formatSalary(s: number): string {
    if (s >= 100000) return `₹${(s / 100000).toFixed(1)}L / year`;
    if (s >= 1000)   return `₹${(s / 1000).toFixed(0)}K / year`;
    return `₹${s} / year`;
  }
}
