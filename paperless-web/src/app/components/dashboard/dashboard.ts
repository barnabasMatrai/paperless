import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { DatePipe, formatDate } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { UploadDocumentComponent } from '../upload-document/upload-document';
import {
  DOCUMENT_TYPES,
  DOCUMENT_TYPE_LABELS,
  DocumentPublic,
  DocumentType,
} from '../../models/document';
import { ReminderPublic } from '../../models/reminder';
import { ReminderService } from '../../services/reminder.service';
import { ToastService } from '../../services/toast.service';
import { toLocalDateTime } from '../../utils/date';
import { AuthService } from '../../services/auth.service';

export type FileType = 'PDF' | 'DOCX' | 'JPG' | 'PNG';

export interface PaperlessDocument {
  id: number;
  title: string;
  description: string;
  type: FileType;
  /** Dokumenttyp aus dem Backend (CONTRACT, INVOICE, ...) */
  category: DocumentType;
  uploadedAt: Date;
  reminder: ReminderPublic | null;
}

type DateFilter = 'all' | '7d' | '30d' | '365d';

const REMINDER_CHECK_INTERVAL = 15_000;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink, UploadDocumentComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class DashboardComponent {
  private reminderService = inject(ReminderService);
  private toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly pageSize = 10;

  readonly navItems = [
    { label: 'Documents', icon: 'home', active: true },
    { label: 'Upload', icon: 'upload', active: false },
    { label: 'Search', icon: 'search', active: false },
    { label: 'Settings', icon: 'settings', active: false },
  ];

  // TODO: durch DocumentService ersetzen, sobald REST-Endpunkt verfügbar ist
  readonly documents = signal<PaperlessDocument[]>([
    {
      id: 1,
      title: 'Rechnung_2024_0123.pdf',
      description: 'Lieferant Muster GmbH – Rechnung für Büromaterial',
      type: 'PDF',
      category: 'INVOICE',
      uploadedAt: new Date(2025, 1, 10, 14, 23),
      // Demo: wird ein paar Sekunden nach dem Laden fällig und als Toast angezeigt
      reminder: {
        id: 1,
        dueDate: toLocalDateTime(new Date(Date.now() + 10_000)),
        description: 'Rechnung bezahlen',
        notified: false,
      },
    },
    {
      id: 2,
      title: 'Projektplanung.docx',
      description: 'Plan für das Semesterprojekt inkl. Meilensteine',
      type: 'DOCX',
      category: 'REPORT',
      uploadedAt: new Date(2025, 1, 3, 9, 16),
      reminder: null,
    },
    {
      id: 3,
      title: 'Vertrag.pdf',
      description: 'Dienstleistungsvertrag mit Laufzeit 12 Monate',
      type: 'PDF',
      category: 'CONTRACT',
      uploadedAt: new Date(2025, 0, 28, 11, 2),
      reminder: {
        id: 2,
        dueDate: toLocalDateTime(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)),
        description: 'Kündigungsfrist prüfen',
        notified: false,
      },
    },
    {
      id: 4,
      title: 'Notizem.jpg',
      description: 'Handschriftliche Notizen aus dem Meeting',
      type: 'JPG',
      category: 'REPORT',
      uploadedAt: new Date(2025, 0, 20, 16, 45),
      reminder: null,
    },
    {
      id: 5,
      title: 'Technische_Dokumentation.pdf',
      description: 'Systemarchitektur und Komponentenübersicht',
      type: 'PDF',
      category: 'REPORT',
      uploadedAt: new Date(2025, 0, 15, 10, 37),
      reminder: null,
    },
  ]);

  readonly searchInput = signal('');
  readonly searchTerm = signal('');
  readonly typeFilter = signal<FileType | 'all'>('all');
  readonly categoryFilter = signal<DocumentType | 'all'>('all');
  readonly dateFilter = signal<DateFilter>('all');
  readonly selectedIds = signal<Set<number>>(new Set());
  readonly page = signal(1);
  readonly userMenuOpen = signal(false);
  readonly uploadOpen = signal(false);
  readonly editing = signal<PaperlessDocument | null>(null);

  readonly editingDocument = computed(() => {
    const doc = this.editing();
    return doc ? this.toPublicDocument(doc) : null;
  });

  readonly types = computed(() => [...new Set(this.documents().map((d) => d.type))].sort());

  readonly categoryLabels = DOCUMENT_TYPE_LABELS;

  readonly categories = DOCUMENT_TYPES;

  readonly filteredDocuments = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const type = this.typeFilter();
    const category = this.categoryFilter();
    const minDate = this.minDate(this.dateFilter());

    return this.documents().filter(
      (doc) =>
        (!term ||
          doc.title.toLowerCase().includes(term) ||
          doc.description.toLowerCase().includes(term) ||
          this.categoryLabels[doc.category].toLowerCase().includes(term)) &&
        (type === 'all' || doc.type === type) &&
        (category === 'all' || doc.category === category) &&
        (!minDate || doc.uploadedAt >= minDate),
    );
  });

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredDocuments().length / this.pageSize)),
  );

  readonly pages = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i + 1));

  readonly pagedDocuments = computed(() => {
    const start = (this.page() - 1) * this.pageSize;
    return this.filteredDocuments().slice(start, start + this.pageSize);
  });

  readonly rangeStart = computed(() =>
    this.filteredDocuments().length ? (this.page() - 1) * this.pageSize + 1 : 0,
  );

  readonly rangeEnd = computed(() =>
    Math.min(this.page() * this.pageSize, this.filteredDocuments().length),
  );

  readonly allSelected = computed(() => {
    const docs = this.pagedDocuments();
    return docs.length > 0 && docs.every((d) => this.selectedIds().has(d.id));
  });

  readonly someSelected = computed(
    () => !this.allSelected() && this.pagedDocuments().some((d) => this.selectedIds().has(d.id)),
  );

  constructor() {
    this.checkReminders();
    const interval = setInterval(() => this.checkReminders(), REMINDER_CHECK_INTERVAL);
    inject(DestroyRef).onDestroy(() => clearInterval(interval));
  }

  isOverdue(reminder: ReminderPublic): boolean {
    return new Date(reminder.dueDate) <= new Date();
  }

  onSearch(): void {
    this.searchTerm.set(this.searchInput());
    this.page.set(1);
  }

  onFilterChange(): void {
    this.page.set(1);
  }

  clearFilters(): void {
    this.searchInput.set('');
    this.searchTerm.set('');
    this.typeFilter.set('all');
    this.categoryFilter.set('all');
    this.dateFilter.set('all');
    this.page.set(1);
  }

  toggleSelection(id: number): void {
    this.selectedIds.update((ids) => {
      const next = new Set(ids);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  toggleAll(): void {
    const select = !this.allSelected();
    this.selectedIds.update((ids) => {
      const next = new Set(ids);
      this.pagedDocuments().forEach((d) => (select ? next.add(d.id) : next.delete(d.id)));
      return next;
    });
  }

  goToPage(page: number): void {
    this.page.set(Math.min(Math.max(1, page), this.totalPages()));
  }

  viewDocument(doc: PaperlessDocument): void {
    // TODO: Detailansicht öffnen
    console.log('View:', doc);
  }

  editDocument(doc: PaperlessDocument): void {
    this.editing.set(doc);
  }

  onEdited(saved: DocumentPublic): void {
    this.editing.set(null);

    this.documents.update((docs) =>
      docs.map((doc) => {
        if (doc.id !== saved.id) {
          return doc;
        }

        // Wurde die Erinnerung während des Bearbeitens schon gemeldet, nicht erneut melden
        const unchanged =
          saved.reminder &&
          doc.reminder?.id === saved.reminder.id &&
          doc.reminder.dueDate === saved.reminder.dueDate &&
          doc.reminder.description === saved.reminder.description;
        const reminder = unchanged
          ? { ...saved.reminder!, notified: saved.reminder!.notified || doc.reminder!.notified }
          : saved.reminder;

        return {
          ...doc,
          title: saved.filename,
          type: this.fileType(saved.filename),
          category: saved.type,
          reminder,
        };
      }),
    );

    this.toastService.show({ title: 'Änderungen gespeichert', message: saved.filename });
  }

  deleteDocument(doc: PaperlessDocument): void {
    // TODO: DocumentService.delete aufrufen
    this.documents.update((docs) => docs.filter((d) => d.id !== doc.id));
    this.selectedIds.update((ids) => {
      const next = new Set(ids);
      next.delete(doc.id);
      return next;
    });
    this.goToPage(this.page());
  }

  uploadDocument(): void {
    this.uploadOpen.set(true);
  }

  onUploaded(doc: DocumentPublic): void {
    this.uploadOpen.set(false);
    this.documents.update((docs) => [this.toTableDocument(doc), ...docs]);
    this.clearFilters();

    this.toastService.show({
      title: 'Dokument hochgeladen',
      message: doc.filename,
      detail: doc.reminder
        ? `Erinnerung am ${this.formatDueDate(doc.reminder.dueDate)}`
        : undefined,
    });
  }

  /** Zeigt fällige, noch nicht gemeldete Reminder als Toast und markiert sie als notified. */
  private checkReminders(): void {
    const now = new Date();
    const due = this.documents().filter(
      (doc) => doc.reminder && !doc.reminder.notified && new Date(doc.reminder.dueDate) <= now,
    );

    for (const doc of due) {
      const reminder: ReminderPublic = { ...doc.reminder!, notified: true };

      this.toastService.show({
        type: 'reminder',
        title: 'Erinnerung',
        message: reminder.description,
        detail: `${doc.title} · fällig ${this.formatDueDate(reminder.dueDate)}`,
        duration: 0,
      });

      this.documents.update((docs) => docs.map((d) => (d.id === doc.id ? { ...d, reminder } : d)));

      const { dueDate, description, notified } = reminder;
      this.reminderService.update(reminder.id, { dueDate, description, notified }).subscribe({
        error: () =>
          console.warn(`Reminder ${reminder.id} konnte nicht als notified gespeichert werden`),
      });
    }
  }

  private formatDueDate(dueDate: string): string {
    return formatDate(dueDate, 'dd.MM.yyyy, HH:mm', 'en-US');
  }

  // TODO: entfällt, sobald die Tabelle direkt mit DocumentPublic aus dem Backend arbeitet
  private toTableDocument(doc: DocumentPublic): PaperlessDocument {
    return {
      id: doc.id,
      title: doc.filename,
      description: '',
      type: this.fileType(doc.filename),
      category: doc.type,
      uploadedAt: new Date(doc.uploadDate),
      reminder: doc.reminder,
    };
  }

  private toPublicDocument(doc: PaperlessDocument): DocumentPublic {
    return {
      id: doc.id,
      filename: doc.title,
      type: doc.category,
      uploadDate: toLocalDateTime(doc.uploadedAt),
      reminder: doc.reminder,
    };
  }

  private fileType(filename: string): FileType {
    const extension = filename.split('.').pop()?.toUpperCase() ?? '';
    if (extension === 'JPEG') {
      return 'JPG';
    }
    return (['DOCX', 'JPG', 'PNG'] as FileType[]).includes(extension as FileType)
      ? (extension as FileType)
      : 'PDF';
  }

  private minDate(filter: DateFilter): Date | null {
    const days = { all: 0, '7d': 7, '30d': 30, '365d': 365 }[filter];
    return days ? new Date(Date.now() - days * 24 * 60 * 60 * 1000) : null;
  }

  logout(): void {
    this.userMenuOpen.set(false);

    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.router.navigate(['/login']);
      },
    });
  }
}
