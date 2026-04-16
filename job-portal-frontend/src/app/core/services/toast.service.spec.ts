import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ToastService, Toast } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add a toast when show() is called', () => {
    service.show('Test Message', 'success');
    const toasts = service.toasts();
    expect(toasts.length).toBe(1);
    expect(toasts[0].message).toBe('Test Message');
    expect(toasts[0].type).toBe('success');
  });

  it('should have default type "info"', () => {
    service.show('Default Info');
    expect(service.toasts()[0].type).toBe('info');
  });

  it('should add success toast via success()', () => {
    service.success('Success');
    expect(service.toasts()[0].type).toBe('success');
  });

  it('should add error toast via error()', () => {
    service.error('Error');
    expect(service.toasts()[0].type).toBe('error');
  });

  it('should add warning toast via warn()', () => {
    service.warn('Warning');
    expect(service.toasts()[0].type).toBe('warning');
  });

  it('should add info toast via info()', () => {
    service.info('Info');
    expect(service.toasts()[0].type).toBe('info');
  });

  it('should remove toast by id', () => {
    service.success('Message');
    const id = service.toasts()[0].id;
    service.remove(id);
    expect(service.toasts().length).toBe(0);
  });

  it('should auto-remove toast after 5 seconds', () => {
    vi.useFakeTimers();
    service.success('Auto remove');
    expect(service.toasts().length).toBe(1);
    
    vi.advanceTimersByTime(5000);
    expect(service.toasts().length).toBe(0);
    vi.useRealTimers();
  });
});
