import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JobResponseDto, JobRequestDto, JobFilterDto, Page } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class JobService {
  private apiUrl = `${environment.apiUrl}/api/jobs`;

  constructor(private http: HttpClient) {}

  // ── Public ──
  getAllJobs(page = 0, size = 9, sortBy = 'createdAt', direction = 'desc'): Observable<Page<JobResponseDto>> {
    const params = new HttpParams()
      .set('page', page).set('size', size)
      .set('sortBy', sortBy).set('direction', direction);
    return this.http.get<Page<JobResponseDto>>(this.apiUrl, { params });
  }

  getJobById(id: number): Observable<JobResponseDto> {
    return this.http.get<JobResponseDto>(`${this.apiUrl}/${id}`);
  }

  getPublicStats(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/api/admin/public/stats`);
  }

  searchJobs(
    filter: JobFilterDto,
    page = 0, size = 9,
    sortBy = 'createdAt', direction = 'desc'
  ): Observable<Page<JobResponseDto>> {
    const params = new HttpParams()
      .set('page', page).set('size', size)
      .set('sortBy', sortBy).set('direction', direction);
    return this.http.post<Page<JobResponseDto>>(`${this.apiUrl}/search`, filter, { params });
  }

  // ── Protected (Recruiter) ──
  createJob(dto: JobRequestDto): Observable<JobResponseDto> {
    return this.http.post<JobResponseDto>(this.apiUrl, dto);
  }

  updateJob(id: number, dto: JobRequestDto): Observable<JobResponseDto> {
    return this.http.put<JobResponseDto>(`${this.apiUrl}/${id}`, dto);
  }

  deleteJob(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }
}
