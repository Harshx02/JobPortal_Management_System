import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserResponse } from '../models/auth.model';
import { Page } from '../models/job.model';

export interface AdminJobResponse {
  id: number;
  title: string;
  companyName: string;
  location: string;
  salary: number;
  experience: number;
  description: string;
  recruiterId: number;
  createdAt: string;
}

export interface AdminPageResponse {
  content: AdminJobResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface AdminReportResponse {
  [key: string]: unknown;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/users/${id}`);
  }

  deleteUser(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/users/${id}`);
  }

  getAllJobs(): Observable<AdminPageResponse> {
    return this.http.get<AdminPageResponse>(`${this.apiUrl}/jobs`);
  }

  getReports(): Observable<AdminReportResponse> {
    return this.http.get<AdminReportResponse>(`${this.apiUrl}/reports`);
  }
}
