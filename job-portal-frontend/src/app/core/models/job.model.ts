export interface JobResponseDto {
  id: number;
  title: string;
  companyName: string;
  location: string;
  salary: number;
  experience: number;
  description: string;
  recruiterId: number;
  createdAt: string;
}

export interface JobRequestDto {
  title: string;
  companyName: string;
  location: string;
  salary: number;
  experience: number;
  description: string;
}

export interface JobFilterDto {
  title?: string;
  skill?: string;
  location?: string;
  companyName?: string;
  minSalary?: number;
  maxSalary?: number;
  minExperience?: number;
  maxExperience?: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
