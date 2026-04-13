import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-5 right-5 z-[9999] flex flex-col gap-3">
      <div *ngFor="let toast of toastService.toasts()" 
           [ngClass]="{
             'bg-green-500': toast.type === 'success',
             'bg-red-500': toast.type === 'error',
             'bg-yellow-500': toast.type === 'warning',
             'bg-blue-500': toast.type === 'info'
           }"
           class="min-w-[300px] px-6 py-4 rounded-lg shadow-2xl text-white transform transition-all duration-300 animate-slide-in flex items-center justify-between">
        <span class="font-medium">{{ toast.message }}</span>
        <button (click)="toastService.remove(toast.id)" class="ml-4 hover:scale-110 transition-transform">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slide-in {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    .animate-slide-in {
      animation: slide-in 0.3s ease-out;
    }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);
}
