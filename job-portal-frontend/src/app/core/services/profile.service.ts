import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserResponse, UpdateProfileRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);
  // Auth API handles profile stuff
  private apiUrl = `${environment.apiUrl}/api/auth`;

  getProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/profile`);
  }

  updateProfile(data: UpdateProfileRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/users/profile`, data);
  }

  uploadProfileImage(userId: number, file: File): Observable<{ profileImageUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ profileImageUrl: string }>(`${this.apiUrl}/users/${userId}/profile-image`, formData);
  }
}
