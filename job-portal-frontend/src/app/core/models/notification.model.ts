import { UserRole } from './auth.model';

export interface Notification {
  id: string;
  message: string;
  timestamp: Date;
  isRead: boolean;
  role: UserRole;
  type: 'SUCCESS' | 'INFO' | 'WARNING';
}
