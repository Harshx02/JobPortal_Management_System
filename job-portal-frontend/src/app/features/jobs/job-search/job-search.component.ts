import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { JobCardComponent } from '../../../shared/components/job-card/job-card.component';
import { JobService } from '../../../core/services/job.service';
import { JobResponseDto, JobFilterDto, Page } from '../../../core/models/job.model';
import { debounceTime, Subject } from 'rxjs';

@Component({
  selector: 'app-job-search',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NavbarComponent, JobCardComponent],
  templateUrl: './job-search.component.html'
})
export class JobSearchComponent implements OnInit {
  jobs       = signal<JobResponseDto[]>([]);
  totalJobs  = signal(0);
  totalPages = signal(0);
  loading    = signal(true);
  error      = signal('');
  page       = signal(0);
  pageSize   = 9;
  Math       = Math;

  filterForm: FormGroup;
  private search$ = new Subject<void>();

  constructor(
    private jobService: JobService,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      title:        [''],
      skill:        [''],
      location:     [''],
      companyName:  [''],
      minSalary:    [null],
      maxSalary:    [null],
      minExperience:[null],
      maxExperience:[null]
    });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['keyword'])  this.filterForm.patchValue({ title: params['keyword'] });
      if (params['skill'])    this.filterForm.patchValue({ skill: params['skill'] });
      if (params['location']) this.filterForm.patchValue({ location: params['location'] });
      this.doSearch();
    });

    // Debounced live search
    this.search$.pipe(debounceTime(400)).subscribe(() => {
      this.page.set(0);
      this.doSearch();
    });
  }

  onFilterChange() { this.search$.next(); }

  doSearch(pg = this.page()) {
    this.loading.set(true);
    this.error.set('');
    const filter: JobFilterDto = this.buildFilter();

    this.jobService.searchJobs(filter, pg, this.pageSize).subscribe({
      next: (res: Page<JobResponseDto>) => {
        this.jobs.set(res.content);
        this.totalJobs.set(res.totalElements);
        this.totalPages.set(res.totalPages);
        this.loading.set(false);
      },
      error: () => { this.error.set('Failed to fetch jobs. Please try again.'); this.loading.set(false); }
    });
  }

  clearFilters() {
    this.filterForm.reset();
    this.page.set(0);
    this.doSearch(0);
  }

  goToPage(p: number) {
    this.page.set(p);
    this.doSearch(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  private buildFilter(): JobFilterDto {
    const v = this.filterForm.value;

    return {
      title:         v.title || undefined,
      skill:         v.skill || undefined,
      location:      v.location      || undefined,
      companyName:   v.companyName   || undefined,
      minSalary:     v.minSalary     || undefined,
      maxSalary:     v.maxSalary     || undefined,
      minExperience: v.minExperience || undefined,
      maxExperience: v.maxExperience || undefined,
    };
  }
}
