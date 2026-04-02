import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { JobService } from '../../../core/services/job.service';
import { AuthService } from '../../../core/services/auth.service';
import { JobRequestDto, JobResponseDto } from '../../../core/models/job.model';

@Component({
  selector: 'app-post-job',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './post-job.component.html'
})
export class PostJobComponent implements OnInit {
  form:    FormGroup;
  loading  = signal(false);
  error    = signal('');
  success  = signal('');
  editId   = signal<number | null>(null);
  isEdit   = signal(false);

  constructor(
    private fb: FormBuilder,
    private jobService: JobService,
    private auth: AuthService,
    private route: ActivatedRoute,
    public router: Router
  ) {
    this.form = this.fb.group({
      title:       ['', [Validators.required, Validators.minLength(3)]],
      companyName: ['', Validators.required],
      location:    ['', Validators.required],
      salary:      [null, [Validators.required, Validators.min(0)]],
      experience:  [null, [Validators.required, Validators.min(0)]],
      description: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editId.set(Number(id));
      this.isEdit.set(true);
      this.jobService.getJobById(Number(id)).subscribe({
        next: j => this.form.patchValue(j),
        error: () => this.error.set('Could not load job for editing.')
      });
    }
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set('');
    this.success.set('');

    const dto: JobRequestDto = this.form.value;
    const obs = this.isEdit()
      ? this.jobService.updateJob(this.editId()!, dto)
      : this.jobService.createJob(dto);

    obs.subscribe({
      next: (res) => {
        this.loading.set(false);
        this.success.set(this.isEdit() ? 'Job updated successfully!' : 'Job posted successfully!');
        setTimeout(() => this.router.navigate(['/recruiter/dashboard']), 1500);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Failed to save job. Please try again.');
      }
    });
  }

  get f() { return this.form.controls; }
}
