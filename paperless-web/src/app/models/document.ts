// Spiegelt at.technikum.paperless.model.DocumentType und die Document-DTOs im Backend

import { ReminderPublic } from './reminder';

export type DocumentType =
  'CONTRACT' | 'INVOICE' | 'CERTIFICATE' | 'LICENSE' | 'INSURANCE' | 'REPORT';

export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  CONTRACT: 'Vertrag',
  INVOICE: 'Rechnung',
  CERTIFICATE: 'Zertifikat',
  LICENSE: 'Lizenz',
  INSURANCE: 'Versicherung',
  REPORT: 'Bericht',
};

export const DOCUMENT_TYPES = Object.keys(DOCUMENT_TYPE_LABELS) as DocumentType[];

export interface DocumentCreate {
  filename: string;
  type: DocumentType;
  /** LocalDateTime im Backend, daher ohne Zeitzone: yyyy-MM-ddTHH:mm:ss */
  uploadDate: string;
}

/** Metadaten-Part beim Upload; uploadDate setzt das Backend */
export interface DocumentUpload {
  filename: string;
  type: DocumentType;
}

export interface DocumentPublic {
  id: number;
  filename: string;
  type: DocumentType;
  uploadDate: string;
  reminder: ReminderPublic | null;
}
