export type ApplicationStatus = 'APPLIED' | 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'UNDER_REVIEW' | 'SHORTLISTED';

export interface ApplicationResponse {
  id: number;
  userId: number;
  resumeUrl: string;
  status: ApplicationStatus;
  appliedAt: string;
  job?: {
    id: number;
    title: string;
    companyName: string;
  };
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
