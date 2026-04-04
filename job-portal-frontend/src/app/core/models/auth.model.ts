export interface AuthResponse {
  token: string;
  name: string;
  email: string;
  role: 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
  message: string;
  userId?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'JOB_SEEKER' | 'RECRUITER';
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
  profileImageUrl?: string;
  phone?: string;
  bio?: string;
  skills?: string;
  location?: string;
  createdAt?: string;
}

export interface UpdateProfileRequest {
  name?: string;
  phone?: string;
  bio?: string;
  skills?: string;
  location?: string;
}

export type UserRole = 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';

export interface AuthState {
  user: AuthResponse | null;
  token: string | null;
  isLoggedIn: boolean;
}
