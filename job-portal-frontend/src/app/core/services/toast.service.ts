import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  toasts = signal<Toast[]>([]);
  private counter = 0;

  show(message: string, type: Toast['type'] = 'info') {
    const id = this.counter++;
    this.toasts.update(t => [...t, { id, message, type }]);

    // Auto-remove after 5 seconds
    setTimeout(() => this.remove(id), 5000);
  }

  success(msg: string) { this.show(msg, 'success'); }
  error(msg: string)   { this.show(msg, 'error'); }
  warn(msg: string)    { this.show(msg, 'warning'); }
  info(msg: string)    { this.show(msg, 'info'); }

  remove(id: number) {
    this.toasts.update(t => t.filter(x => x.id !== id));
  }
}
