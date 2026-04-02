import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { JobResponseDto } from '../../../core/models/job.model';

@Component({
  selector: 'app-job-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card p-6 flex flex-col gap-4 cursor-pointer animate-fade-in-up" (click)="viewJob()">
      <!-- Header -->
      <div class="flex items-start gap-4">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center font-bold text-lg text-white flex-shrink-0"
             [style.background]="getColor(job.companyName)">
          {{ job.companyName.charAt(0).toUpperCase() }}
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="font-semibold text-gray-900 text-base leading-tight truncate">{{ job.title }}</h3>
          <p class="text-sm text-gray-500 mt-0.5">{{ job.companyName }}</p>
        </div>
        <span class="badge badge-blue flex-shrink-0">Full-time</span>
      </div>

      <!-- Meta -->
      <div class="flex flex-wrap gap-3 text-sm text-gray-600">
        <span class="flex items-center gap-1.5">
          <svg class="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
          {{ job.location }}
        </span>
        <span class="flex items-center gap-1.5">
          <svg class="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          ₹{{ formatSalary(job.salary) }}
        </span>
        <span class="flex items-center gap-1.5">
          <svg class="w-4 h-4 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
          </svg>
          {{ job.experience }}+ yrs
        </span>
      </div>

      <!-- Description snippet -->
      <p class="text-sm text-gray-600 line-clamp-2 leading-relaxed">{{ job.description }}</p>

      <!-- Footer -->
      <div class="flex items-center justify-between pt-2 border-t border-gray-50">
        <span class="text-xs text-gray-400">{{ getAge(job.createdAt) }}</span>
        <button class="btn-primary text-sm py-2 px-4" (click)="viewJob(); $event.stopPropagation()">
          View Details →
        </button>
      </div>
    </div>
  `
})
export class JobCardComponent {
  @Input({ required: true }) job!: JobResponseDto;

  private colors = [
    'linear-gradient(135deg,#2563eb,#1d4ed8)',
    'linear-gradient(135deg,#059669,#047857)',
    'linear-gradient(135deg,#7c3aed,#6d28d9)',
    'linear-gradient(135deg,#db2777,#be185d)',
    'linear-gradient(135deg,#d97706,#b45309)',
    'linear-gradient(135deg,#0ea5e9,#0284c7)',
  ];

  constructor(private router: Router) {}

  viewJob() { this.router.navigate(['/jobs', this.job.id]); }

  getColor(name: string): string {
    const i = name.charCodeAt(0) % this.colors.length;
    return this.colors[i];
  }

  formatSalary(s: number): string {
    if (s >= 100000) return `${(s / 100000).toFixed(1)}L`;
    if (s >= 1000)   return `${(s / 1000).toFixed(0)}K`;
    return s.toString();
  }

  getAge(date: string): string {
    if (!date) return '';
    const diff = Date.now() - new Date(date).getTime();
    const days = Math.floor(diff / 86400000);
    if (days === 0) return 'Today';
    if (days === 1) return 'Yesterday';
    if (days < 7)  return `${days} days ago`;
    if (days < 30) return `${Math.floor(days / 7)} weeks ago`;
    return `${Math.floor(days / 30)} months ago`;
  }
}
