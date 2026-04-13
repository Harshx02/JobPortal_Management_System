import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApplicationResponse, JobApplicationResponse, ApplicationStatus } from '../models/application.model';
import { Page } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private apiUrl = `${environment.apiUrl}/api/applications`;

  constructor(private http: HttpClient) {}

  // POST /api/applications/apply  (multipart/form-data)
  applyForJob(jobId: number, resume: File): Observable<ApplicationResponse> {
    const form = new FormData();
    form.append('jobId', jobId.toString());
    form.append('resume', resume, resume.name);
    return this.http.post<ApplicationResponse>(`${this.apiUrl}/apply`, form);
  }

  // GET /api/applications/user/viewApplications
  getUserApplications(page = 0, size = 10): Observable<Page<ApplicationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ApplicationResponse>>(`${this.apiUrl}/user/viewApplications`, { params });
  }

  // GET /api/applications/jobApplications/{jobId}
  getJobApplications(jobId: number, page = 0, size = 10): Observable<Page<JobApplicationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<JobApplicationResponse>>(`${this.apiUrl}/jobApplications/${jobId}`, { params });
  }

  // PATCH /api/applications/jobApplication/{id}/status
  updateStatus(id: number, status: ApplicationStatus): Observable<ApplicationResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<ApplicationResponse>(`${this.apiUrl}/jobApplication/${id}/status`, null, { params });
  }

  // GET /api/applications/count
  getTotalCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count`);
  }
}
