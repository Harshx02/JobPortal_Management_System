import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApplicationService } from './application.service';
import { environment } from '../../../environments/environment';
import { ApplicationResponse, JobApplicationResponse } from '../models/application.model';

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/api/applications`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApplicationService]
    });
    service = TestBed.inject(ApplicationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should apply for a job using FormData', () => {
    const mockFile = new File(['resume content'], 'resume.pdf', { type: 'application/pdf' });
    const mockRes: Partial<ApplicationResponse> = { id: 1, status: 'PENDING' };

    service.applyForJob(101, mockFile).subscribe(res => {
      expect(res.id).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/apply`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTruthy();
    req.flush(mockRes);
  });

  it('should get user applications', () => {
    const mockApps: Partial<ApplicationResponse>[] = [{ id: 1, status: 'PENDING' }];

    service.getUserApplications().subscribe(res => {
      expect(res.length).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/user/viewApplications`);
    expect(req.request.method).toBe('GET');
    req.flush(mockApps);
  });

  it('should get job applications by jobId', () => {
    const mockApps: Partial<JobApplicationResponse>[] = [{ id: 1, applicantName: 'John' }];

    service.getJobApplications(101).subscribe(res => {
      expect(res[0].applicantName).toBe('John');
    });

    const req = httpMock.expectOne(`${apiUrl}/jobApplications/101`);
    expect(req.request.method).toBe('GET');
    req.flush(mockApps);
  });

  it('should update application status using PATCH', () => {
    const mockRes: Partial<ApplicationResponse> = { id: 1, status: 'ACCEPTED' };

    service.updateStatus(1, 'ACCEPTED').subscribe(res => {
      expect(res.status).toBe('ACCEPTED');
    });

    const req = httpMock.expectOne(req => 
      req.url === `${apiUrl}/jobApplication/1/status` && 
      req.params.get('status') === 'ACCEPTED'
    );
    expect(req.request.method).toBe('PATCH');
    req.flush(mockRes);
  });

  it('should get total application count', () => {
    service.getTotalCount().subscribe(count => {
      expect(count).toBe(50);
    });

    const req = httpMock.expectOne(`${apiUrl}/count`);
    expect(req.request.method).toBe('GET');
    req.flush(50);
  });
});
