import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ToastComponent } from './toast.component';
import { ToastService } from '../../../core/services/toast.service';
import { By } from '@angular/platform-browser';

describe('ToastComponent', () => {
  let component: ToastComponent;
  let fixture: ComponentFixture<ToastComponent>;
  let toastService: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToastComponent],
      providers: [ToastService]
    }).compileComponents();

    fixture = TestBed.createComponent(ToastComponent);
    component = fixture.componentInstance;
    toastService = TestBed.inject(ToastService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display toasts from service', () => {
    toastService.success('Success message');
    fixture.detectChanges();

    const toastElements = fixture.debugElement.queryAll(By.css('div.min-w-\\[300px\\]'));
    expect(toastElements.length).toBe(1);
    expect(toastElements[0].nativeElement.textContent).toContain('Success message');
  });

  it('should show correct background color per type', () => {
    toastService.success('Success');
    toastService.error('Error');
    toastService.warn('Warning');
    toastService.info('Info');
    fixture.detectChanges();

    const toasts = fixture.debugElement.queryAll(By.css('div.min-w-\\[300px\\]'));
    expect(toasts[0].nativeElement.classList).toContain('bg-green-500');
    expect(toasts[1].nativeElement.classList).toContain('bg-red-500');
    expect(toasts[2].nativeElement.classList).toContain('bg-yellow-500');
    expect(toasts[3].nativeElement.classList).toContain('bg-blue-500');
  });

  it('should remove toast when close button is clicked', () => {
    toastService.success('Dismiss me');
    fixture.detectChanges();

    const closeButton = fixture.debugElement.query(By.css('button'));
    closeButton.triggerEventHandler('click', null);
    fixture.detectChanges();

    const toastElements = fixture.debugElement.queryAll(By.css('div.min-w-\\[300px\\]'));
    expect(toastElements.length).toBe(0);
  });

  it('should auto-remove toast after 5 seconds', () => {
    vi.useFakeTimers();
    toastService.success('Auto remove');
    fixture.detectChanges();

    expect(toastService.toasts().length).toBe(1);

    vi.advanceTimersByTime(5000);
    fixture.detectChanges();

    expect(toastService.toasts().length).toBe(0);
    vi.useRealTimers();
  });
});
