import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';
import { AuthService } from './auth.service';
import { JobService } from './job.service';
import { ApplicationService } from './application.service';
import { AdminService } from './admin.service';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('NotificationService', () => {
  let service: NotificationService;
  let authMock: any;
  let jobMock: any;
  let appMock: any;
  let adminMock: any;

  beforeEach(() => {
    authMock = {
      userRole: signal(null)
    };
    jobMock = {
      getAllJobs: vi.fn().mockReturnValue(of({ content: [] }))
    };
    appMock = {
      getUserApplications: vi.fn().mockReturnValue(of([])),
      getJobApplications: vi.fn().mockReturnValue(of([]))
    };
    adminMock = {
      getAllUsers: vi.fn().mockReturnValue(of([]))
    };

    TestBed.configureTestingModule({
      providers: [
        NotificationService,
        { provide: AuthService, useValue: authMock },
        { provide: JobService, useValue: jobMock },
        { provide: ApplicationService, useValue: appMock },
        { provide: AdminService, useValue: adminMock }
      ]
    });

    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should not load notifications if no role is set', () => {
    authMock.userRole.set(null);
    service.refreshNotifications();
    expect(service.notifications().length).toBe(0);
  });

  it('should load JOB_SEEKER notifications', () => {
    authMock.userRole.set('JOB_SEEKER');
    appMock.getUserApplications.mockReturnValue(of([{ id: 1, status: 'ACCEPTED', job: { title: 'Angular Developer' } }]));
    jobMock.getAllJobs.mockReturnValue(of({ content: [{ id: 10, title: 'New Job', companyName: 'Google', createdAt: new Date().toISOString() }] }));

    service.refreshNotifications();

    expect(service.notifications().length).toBe(2);
    expect(service.unreadCount()).toBe(2);
    expect(service.notifications()[0].message).toContain('ACCEPTED');
  });

  it('should load RECRUITER notifications', () => {
    authMock.userRole.set('RECRUITER');
    jobMock.getAllJobs.mockReturnValue(of({ content: [{ id: 101, title: 'Manager' }] }));
    appMock.getJobApplications.mockReturnValue(of([{ id: 500, applicantName: 'Jane', jobId: 101, appliedAt: new Date().toISOString() }]));

    service.refreshNotifications();

    expect(service.notifications().length).toBe(1);
    expect(service.notifications()[0].message).toContain('Jane');
  });

  it('should load ADMIN notifications', () => {
    authMock.userRole.set('ADMIN');
    adminMock.getAllUsers.mockReturnValue(of([{ id: 9, name: 'New User', createdAt: new Date().toISOString() }]));

    service.refreshNotifications();

    expect(service.notifications().length).toBe(1);
    expect(service.notifications()[0].message).toContain('Registration');
  });

  it('should mark a notification as read', () => {
    authMock.userRole.set('ADMIN');
    adminMock.getAllUsers.mockReturnValue(of([{ id: 1, name: 'Test' }]));
    service.refreshNotifications();

    const id = service.notifications()[0].id;
    service.markAsRead(id);

    expect(service.notifications()[0].isRead).toBe(true);
    expect(service.unreadCount()).toBe(0);
  });

  it('should mark all notifications as read', () => {
    authMock.userRole.set('ADMIN');
    adminMock.getAllUsers.mockReturnValue(of([{ id: 1, name: 'T1' }, { id: 2, name: 'T2' }]));
    service.refreshNotifications();

    service.markAllAsRead();

    expect(service.unreadCount()).toBe(0);
    expect(service.notifications().every(n => n.isRead)).toBe(true);
  });
});
