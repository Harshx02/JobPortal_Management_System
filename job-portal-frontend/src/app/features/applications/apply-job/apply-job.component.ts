import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { ApplicationService } from '../../../core/services/application.service';
import { JobService } from '../../../core/services/job.service';
import { JobResponseDto } from '../../../core/models/job.model';
import { Subject, takeUntil } from 'rxjs';
import { HasUnsavedChanges } from '../../../core/guards/deactivate.guard';

@Component({
  selector: 'app-apply-job',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './apply-job.component.html'
})
export class ApplyJobComponent implements OnInit, OnDestroy, HasUnsavedChanges {
  job         = signal<JobResponseDto | null>(null);
  selectedFile = signal<File | null>(null);
  loading     = signal(false);
  jobLoading  = signal(true);
  success     = signal(false);
  error       = signal('');
  dragOver    = signal(false);

  private destroy$ = new Subject<void>();

  jobId!: number;

  constructor(
    private appService: ApplicationService,
    private jobService: JobService,
    private route: ActivatedRoute,
    public router: Router
  ) {}

  ngOnInit() {
    this.jobId = Number(this.route.snapshot.paramMap.get('id'));
    this.jobService.getJobById(this.jobId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: j  => { this.job.set(j); this.jobLoading.set(false); },
        error: () => { this.jobLoading.set(false); }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  hasUnsavedChanges(): boolean {
    if (this.success()) return false;
    return this.selectedFile() !== null;
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.setFile(input.files[0]);
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.dragOver.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.setFile(file);
  }

  private setFile(file: File) {
    const allowed = ['application/pdf', 'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!allowed.includes(file.type)) {
      this.error.set('Only PDF and Word documents are allowed.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.error.set('File size must be under 5 MB.');
      return;
    }
    this.error.set('');
    this.selectedFile.set(file);
  }

  submit() {
    if (!this.selectedFile()) { this.error.set('Please upload your resume.'); return; }
    this.loading.set(true);
    this.error.set('');

    this.appService.applyForJob(this.jobId, this.selectedFile()!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => { this.loading.set(false); this.success.set(true); },
        error: (err) => {
          this.loading.set(false);
          this.error.set(err?.error?.message || 'Application failed. You may have already applied.');
        }
      });
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
}
