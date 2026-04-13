import { Injectable, signal, computed, inject } from '@angular/core';
import { Notification } from '../models/notification.model';
import { AuthService } from './auth.service';
import { JobService } from './job.service';
import { ApplicationService } from './application.service';
import { AdminService } from './admin.service';
import { ApplicationResponse, JobApplicationResponse } from '../models/application.model';
import { forkJoin, map, take } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private auth = inject(AuthService);
  private jobService = inject(JobService);
  private appService = inject(ApplicationService);
  private adminService = inject(AdminService);
  
  // Data State
  private _notifications = signal<Notification[]>([]);
  
  // Public State
  readonly notifications = computed(() => [...this._notifications()].sort((a,b) => b.timestamp.getTime() - a.timestamp.getTime()));
  readonly unreadCount = computed(() => this._notifications().filter(n => !n.isRead).length);

  constructor() {
    // Initial load
    this.refreshNotifications();
  }

  refreshNotifications() {
    const role = this.auth.userRole();
    if (!role) return;

    if (role === 'JOB_SEEKER') {
      this.loadJobSeekerNotifications();
    } else if (role === 'RECRUITER') {
      this.loadRecruiterNotifications();
    } else if (role === 'ADMIN') {
      this.loadAdminNotifications();
    }
  }

  private loadJobSeekerNotifications() {
    this.appService.getUserApplications().subscribe(page => {
      const notifications: Notification[] = page.content.slice(0, 5).map((app: ApplicationResponse) => ({
        id: `app-${app.id}`,
        message: `Your application for ${app.job?.title || 'Job'} is now: ${app.status}`,
        timestamp: new Date(),
        isRead: false,
        role: 'JOB_SEEKER',
        type: app.status === 'ACCEPTED' ? 'SUCCESS' : app.status === 'REJECTED' ? 'WARNING' : 'INFO'
      }));

      // Also get new jobs
      this.jobService.getAllJobs(0, 3).subscribe(page => {
        const jobNotifs: Notification[] = page.content.map(job => ({
          id: `job-${job.id}`,
          message: `New Job Posted: ${job.title} at ${job.companyName}`,
          timestamp: new Date(job.createdAt),
          isRead: false,
          role: 'JOB_SEEKER',
          type: 'INFO'
        }));
        this._notifications.set([...notifications, ...jobNotifs]);
      });
    });
  }

  private loadRecruiterNotifications() {
    // 1. Get recruiter's jobs
    this.jobService.getAllJobs(0, 10).subscribe(page => {
      const recruiterJobs = page.content;
      if (recruiterJobs.length === 0) return;

      // 2. Fetch applications for these jobs
      const requests = recruiterJobs.map(job => this.appService.getJobApplications(job.id));
      
      forkJoin(requests).subscribe(results => {
        const allApps = results.flatMap(page => page.content);
        const notifications: Notification[] = allApps.slice(0, 10).map((app: JobApplicationResponse) => {
          const job = recruiterJobs.find(j => j.id === app.jobId);
          return {
            id: `rec-app-${app.id}`,
            message: `New Applicant: ${app.applicantName || 'Someone'} applied for ${job?.title || 'your job'}`,
            timestamp: new Date(app.appliedAt),
            isRead: false,
            role: 'RECRUITER',
            type: 'INFO'
          };
        });
        this._notifications.set(notifications);
      });
    });
  }

  private loadAdminNotifications() {
    this.adminService.getAllUsers().subscribe(users => {
      const notifications: Notification[] = users.slice(-5).map(user => ({
        id: `user-${user.id}`,
        message: `New User Registration: ${user.name} joined!`,
        timestamp: new Date(user.createdAt || new Date()),
        isRead: false,
        role: 'ADMIN',
        type: 'SUCCESS'
      }));
      this._notifications.set(notifications);
    });
  }

  markAsRead(id: string) {
    this._notifications.update(list => 
      list.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
  }

  markAllAsRead() {
    this._notifications.update(list => 
      list.map(n => ({ ...n, isRead: true }))
    );
  }
}
