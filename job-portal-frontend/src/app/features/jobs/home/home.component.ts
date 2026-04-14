import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { JobCardComponent } from '../../../shared/components/job-card/job-card.component';
import { JobService } from '../../../core/services/job.service';
import { JobResponseDto } from '../../../core/models/job.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent, JobCardComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  keyword  = '';
  location = '';
  jobs     = signal<JobResponseDto[]>([]);
  loading  = signal(true);
  error    = signal('');

  stats = [
    { label: 'Active Jobs', value: '10K+', icon: '💼' },
    { label: 'Companies', value: '2K+',  icon: '🏢' },
    { label: 'Hired Monthly', value: '5K+', icon: '🎯' },
    { label: 'Job Seekers', value: '50K+', icon: '👥' }
  ];

  constructor(private jobService: JobService, private router: Router) {}

  ngOnInit() {
    this.jobService.getAllJobs(0, 6).subscribe({
      next: page => { this.jobs.set(page.content); this.loading.set(false); },
      error: ()   => { this.error.set('Could not load jobs.'); this.loading.set(false); }
    });

    this.jobService.getPublicStats().subscribe({
      next: data => {
        this.stats = [
          { label: 'Active Jobs', value: this.formatNumber(data.activeJobs), icon: '💼' },
          { label: 'Companies', value: this.formatNumber(data.companies), icon: '🏢' },
          { label: 'Hired Monthly', value: this.formatNumber(data.hiredMonthly), icon: '🎯' },
          { label: 'Job Seekers', value: this.formatNumber(data.jobSeekers), icon: '👥' }
        ];
      },
      error: err => {
        console.error('Could not load stats', err);
      }
    });
  }

  private formatNumber(num: number): string {
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K+';
    }
    return num.toString();
  }

  search() {
    this.router.navigate(['/jobs'], {
      queryParams: {
        keyword: this.keyword || undefined,
        location: this.location || undefined
      }
    });
  }

  searchTag(tag: string) {
    let queryParams: any = {};
    if (['React', 'Java', 'Python', 'Data Science'].includes(tag)) {
       queryParams.skill = tag;
    } else if (tag === 'Remote') {
       queryParams.location = tag;
    } else {
       queryParams.keyword = tag;
    }
    this.router.navigate(['/jobs'], { queryParams });
  }
}
