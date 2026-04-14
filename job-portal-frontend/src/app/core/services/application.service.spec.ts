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
    const mockPage = {
      content: [{ id: 1, status: 'PENDING' }],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getUserApplications().subscribe(res => {
      expect(res.content.length).toBe(1);
      expect(res.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(req => req.url === `${apiUrl}/user/viewApplications`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get job applications by jobId', () => {
    const mockPage = {
      content: [{ id: 1, applicantName: 'John' }],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0
    };

    service.getJobApplications(101).subscribe(res => {
      expect(res.content[0].applicantName).toBe('John');
    });

    const req = httpMock.expectOne(req => req.url.includes(`${apiUrl}/jobApplications/101`));
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
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
