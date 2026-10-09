import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'reminder';

export interface Toast {
  id: number;
  type: ToastType;
  title: string;
  message: string;
  detail?: string;
}

export interface ToastOptions {
  type?: ToastType;
  title: string;
  message: string;
  detail?: string;
  /** Anzeigedauer in ms, 0 = bleibt bis zum Schließen */
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  private timers = new Map<number, ReturnType<typeof setTimeout>>();

  readonly toasts = signal<Toast[]>([]);

  show({ type = 'success', duration = 5000, ...content }: ToastOptions): number {
    const toast: Toast = { id: this.nextId++, type, ...content };
    this.toasts.update((toasts) => [...toasts, toast]);

    if (duration > 0) {
      this.timers.set(
        toast.id,
        setTimeout(() => this.dismiss(toast.id), duration),
      );
    }

    return toast.id;
  }

  dismiss(id: number): void {
    clearTimeout(this.timers.get(id));
    this.timers.delete(id);
    this.toasts.update((toasts) => toasts.filter((t) => t.id !== id));
  }
}
