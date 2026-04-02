export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'UNDER_REVIEW';

export interface ApplicationResponse {
  id: number;
  jobId: number;
  userId: number;
  resumeUrl: string;
  status: ApplicationStatus;
  appliedAt: string;
  jobTitle?: string;
  companyName?: string;
}

export interface JobApplicationResponse {
  id: number;
  jobId: number;
  userId: number;
  resumeUrl: string;
  status: ApplicationStatus;
  appliedAt: string;
  applicantName?: string;
  applicantEmail?: string;
}
