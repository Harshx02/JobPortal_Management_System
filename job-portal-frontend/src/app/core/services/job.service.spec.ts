import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { JobService } from './job.service';
import { environment } from '../../../environments/environment';
import { JobResponseDto, JobRequestDto, JobFilterDto, Page } from '../models/job.model';

describe('JobService', () => {
  let service: JobService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/api/jobs`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [JobService]
    });
    service = TestBed.inject(JobService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all jobs with default params', () => {
    const mockPage: Page<JobResponseDto> = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 9,
      number: 0,
      first: true,
      last: true
    };

    service.getAllJobs().subscribe(res => {
      expect(res).toEqual(mockPage);
    });

    const req = httpMock.expectOne(req => 
      req.url === apiUrl && 
      req.params.get('page') === '0' && 
      req.params.get('size') === '9'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should get job by id', () => {
    const mockJob: Partial<JobResponseDto> = { id: 1, title: 'Test Job' };

    service.getJobById(1).subscribe(job => {
      expect(job.id).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockJob);
  });

  it('should search jobs with filters', () => {
    const filter: JobFilterDto = { title: 'Angular' };
    const mockPage: Page<JobResponseDto> = { 
      content: [], 
      totalElements: 0, 
      totalPages: 0, 
      size: 9, 
      number: 0,
      first: true,
      last: true 
    };

    service.searchJobs(filter).subscribe(res => {
      expect(res).toEqual(mockPage);
    });

    const req = httpMock.expectOne(req => req.url === `${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(filter);
    req.flush(mockPage);
  });

  it('should create a job', () => {
    const dto: JobRequestDto = { title: 'New Job', companyName: 'Co', location: 'Loc', salary: 100, experience: 1, description: 'Desc' };
    const mockRes: Partial<JobResponseDto> = { id: 100, ...dto };

    service.createJob(dto).subscribe(res => {
      expect(res.id).toBe(100);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(mockRes);
  });

  it('should update a job', () => {
    const dto: JobRequestDto = { title: 'Updated Job', companyName: 'Co', location: 'Loc', salary: 100, experience: 1, description: 'Desc' };
    const mockRes: Partial<JobResponseDto> = { id: 1, ...dto };

    service.updateJob(1, dto).subscribe(res => {
      expect(res.title).toBe('Updated Job');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockRes);
  });

  it('should delete a job', () => {
    service.deleteJob(1).subscribe(res => {
      expect(res.message).toBe('Deleted');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ message: 'Deleted' });
  });
});
