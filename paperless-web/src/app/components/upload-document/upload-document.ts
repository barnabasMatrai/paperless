import {
  Component,
  DestroyRef,
  HostListener,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { DocumentService } from '../../services/document.service';
import { ReminderService } from '../../services/reminder.service';
import { ToastService } from '../../services/toast.service';
import { toLocalDateTime } from '../../utils/date';
import {
  DOCUMENT_TYPES,
  DOCUMENT_TYPE_LABELS,
  DocumentPublic,
  DocumentType,
} from '../../models/document';
import { ReminderPublic } from '../../models/reminder';

const MAX_FILE_SIZE = 20 * 1024 * 1024;
const ACCEPTED_EXTENSIONS = ['pdf', 'docx', 'jpg', 'jpeg', 'png'];

/** datetime-local erwartet yyyy-MM-ddTHH:mm */
const toInputDateTime = (value: string | Date) =>
  toLocalDateTime(typeof value === 'string' ? new Date(value) : value).slice(0, 16);

/**
 * Dialog zum Hochladen eines neuen Dokuments. Wird `document` übergeben,
 * bearbeitet er stattdessen dieses Dokument (Dateiname, Typ, Erinnerung).
 */
@Component({
  selector: 'app-upload-document',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './upload-document.html',
  styleUrls: ['./upload-document.css'],
})
export class UploadDocumentComponent implements OnInit {
  private fb = inject(FormBuilder);
  private documentService = inject(DocumentService);
  private reminderService = inject(ReminderService);
  private toastService = inject(ToastService);

  readonly document = input<DocumentPublic | null>(null);

  readonly closed = output<void>();
  readonly saved = output<DocumentPublic>();

  readonly isEdit = computed(() => this.document() !== null);

  readonly types = DOCUMENT_TYPES;
  readonly typeLabels = DOCUMENT_TYPE_LABELS;
  readonly accept = ACCEPTED_EXTENSIONS.map((ext) => '.' + ext).join(',');
  readonly minDueDate = toInputDateTime(new Date());

  readonly file = signal<File | null>(null);
  readonly fileError = signal<string | null>(null);
  readonly dragOver = signal(false);
  readonly loading = signal(false);
  readonly submitError = signal<string | null>(null);

  uploadForm = this.fb.nonNullable.group({
    filename: ['', Validators.required],
    type: this.fb.nonNullable.control<DocumentType | ''>('', Validators.required),
    reminderEnabled: [false],
    reminderDueDate: [''],
    reminderDescription: [''],
  });

  constructor() {
    this.reminderEnabled.valueChanges
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe((enabled) => this.toggleReminderValidators(enabled));
  }

  ngOnInit(): void {
    const document = this.document();
    if (!document) {
      return;
    }

    this.uploadForm.setValue({
      filename: document.filename,
      type: document.type,
      reminderEnabled: !!document.reminder,
      reminderDueDate: document.reminder ? toInputDateTime(document.reminder.dueDate) : '',
      reminderDescription: document.reminder?.description ?? '',
    });
  }

  get filename() {
    return this.uploadForm.controls.filename;
  }

  get type() {
    return this.uploadForm.controls.type;
  }

  get reminderEnabled() {
    return this.uploadForm.controls.reminderEnabled;
  }

  get reminderDueDate() {
    return this.uploadForm.controls.reminderDueDate;
  }

  get reminderDescription() {
    return this.uploadForm.controls.reminderDescription;
  }

  @HostListener('document:keydown.escape')
  close(): void {
    if (!this.loading()) {
      this.closed.emit();
    }
  }

  onFileInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectFile(input.files?.[0] ?? null);
    input.value = '';
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(true);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(false);
    this.selectFile(event.dataTransfer?.files[0] ?? null);
  }

  removeFile(): void {
    this.file.set(null);
    this.filename.reset();
  }

  formatSize(bytes: number): string {
    if (bytes < 1024 * 1024) {
      return `${Math.max(1, Math.round(bytes / 1024))} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  fileExtension(filename: string): string {
    return filename.split('.').pop()?.toUpperCase() ?? '';
  }

  onSubmit(): void {
    const existing = this.document();

    if (!existing && !this.file()) {
      this.fileError.set('Bitte eine Datei auswählen.');
    }

    if (this.uploadForm.invalid || (!existing && !this.file())) {
      this.uploadForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.submitError.set(null);

    const { filename, type } = this.uploadForm.getRawValue();
    const body = {
      filename: filename.trim(),
      type: type as DocumentType,
      uploadDate: existing?.uploadDate ?? toLocalDateTime(new Date()),
    };

    // TODO: Datei selbst hochladen, sobald das Backend sie speichert (siehe DocumentService)
    const request = existing
      ? this.documentService.update(existing.id, body)
      : this.documentService.create(body);

    request
      .pipe(
        switchMap((document) =>
          this.saveReminder(document.id, existing?.reminder ?? null).pipe(
            map((reminder): DocumentPublic => ({ ...document, reminder })),
            catchError(() => {
              this.toastService.show({
                type: 'error',
                title: 'Erinnerung nicht gespeichert',
                message: existing
                  ? 'Das Dokument wurde gespeichert, die Erinnerung aber nicht.'
                  : 'Das Dokument wurde hochgeladen, die Erinnerung aber nicht angelegt.',
              });
              return of({ ...document, reminder: existing?.reminder ?? null });
            }),
          ),
        ),
      )
      .subscribe({
        next: (document) => {
          this.loading.set(false);
          this.saved.emit(document);
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          this.submitError.set(this.errorMessage(error));
        },
      });
  }

  /** Legt die Erinnerung an, ändert oder löscht sie – je nach Formular und bisherigem Stand. */
  private saveReminder(
    documentId: number,
    current: ReminderPublic | null,
  ): Observable<ReminderPublic | null> {
    const { reminderEnabled, reminderDueDate, reminderDescription } = this.uploadForm.getRawValue();

    if (!reminderEnabled) {
      return current ? this.reminderService.delete(current.id).pipe(map(() => null)) : of(null);
    }

    const dueDate = toLocalDateTime(new Date(reminderDueDate));
    const description = reminderDescription.trim();

    if (!current) {
      return this.reminderService.create({ documentId, dueDate, description, notified: false });
    }

    const unchanged =
      toInputDateTime(current.dueDate) === reminderDueDate && current.description === description;
    if (unchanged) {
      return of(current);
    }

    // Geänderte Erinnerung soll erneut gemeldet werden
    return this.reminderService.update(current.id, { dueDate, description, notified: false });
  }

  private toggleReminderValidators(enabled: boolean): void {
    if (enabled) {
      // Eine bestehende, unveränderte Erinnerung darf in der Vergangenheit liegen
      const current = this.document()?.reminder;
      const allowed = current ? toInputDateTime(current.dueDate) : null;
      this.reminderDueDate.setValidators([Validators.required, futureDateValidator(allowed)]);
      this.reminderDescription.setValidators([Validators.required, Validators.maxLength(255)]);
    } else {
      this.reminderDueDate.clearValidators();
      this.reminderDescription.clearValidators();
      this.reminderDueDate.reset();
      this.reminderDescription.reset();
    }

    this.reminderDueDate.updateValueAndValidity();
    this.reminderDescription.updateValueAndValidity();
  }

  private selectFile(file: File | null): void {
    this.fileError.set(null);

    if (!file) {
      return;
    }

    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!ACCEPTED_EXTENSIONS.includes(extension)) {
      this.fileError.set('Nur PDF, DOCX, JPG oder PNG erlaubt.');
      return;
    }

    if (file.size > MAX_FILE_SIZE) {
      this.fileError.set('Die Datei darf maximal 20 MB groß sein.');
      return;
    }

    this.file.set(file);
    this.filename.setValue(file.name);
  }

  private errorMessage(error: HttpErrorResponse): string {
    switch (error.status) {
      case 0:
        return 'Server nicht erreichbar.';
      case 401:
      case 403:
        return 'Nicht angemeldet oder keine Berechtigung.';
      case 400:
        return 'Ungültige Eingaben.';
      case 404:
        return 'Dokument nicht gefunden.';
      default:
        return this.isEdit()
          ? 'Speichern fehlgeschlagen. Bitte erneut versuchen.'
          : 'Upload fehlgeschlagen. Bitte erneut versuchen.';
    }
  }
}

function futureDateValidator(allowedValue: string | null): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value || control.value === allowedValue) {
      return null;
    }
    return new Date(control.value) > new Date() ? null : { pastDate: true };
  };
}
