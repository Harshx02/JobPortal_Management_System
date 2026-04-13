import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService, AdminPageResponse, AdminReportResponse } from './admin.service';
import { environment } from '../../../environments/environment';
import { UserResponse } from '../models/auth.model';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/api/admin`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all users', () => {
    const mockUsers: Partial<UserResponse>[] = [{ id: 1, name: 'Admin', role: 'ADMIN' }];

    service.getAllUsers().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].role).toBe('ADMIN');
    });

    const req = httpMock.expectOne(`${apiUrl}/users`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUsers);
  });

  it('should get user by id', () => {
    const mockUser: Partial<UserResponse> = { id: 1, name: 'John Doe' };

    service.getUserById(1).subscribe(user => {
      expect(user.name).toBe('John Doe');
    });

    const req = httpMock.expectOne(`${apiUrl}/users/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('should delete user', () => {
    service.deleteUser(5).subscribe(res => {
      expect(res.message).toBe('User Deleted');
    });

    const req = httpMock.expectOne(`${apiUrl}/users/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ message: 'User Deleted' });
  });

  it('should get all jobs for admin', () => {
    const mockPage: AdminPageResponse = {
      content: [{ id: 1, title: 'Job', companyName: 'Co', location: 'Loc', salary: 100, experience: 1, description: 'D', recruiterId: 10, createdAt: '2023-01-01' }],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getAllJobs().subscribe(res => {
      expect(res.totalElements).toBe(1);
      expect(res.content[0].title).toBe('Job');
    });

    const req = httpMock.expectOne(`${apiUrl}/jobs`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get reports', () => {
    const mockReport: AdminReportResponse = { totalApplications: 100, activeUsers: 50 };

    service.getReports().subscribe(res => {
      expect(res['totalApplications']).toBe(100);
    });

    const req = httpMock.expectOne(`${apiUrl}/reports`);
    expect(req.request.method).toBe('GET');
    req.flush(mockReport);
  });
});
