import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { DocumentCreate, DocumentPublic, DocumentUpload } from '../models/document';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private http = inject(HttpClient);
  private baseUrl = `${API_URL}/documents`;

  getAll(): Observable<DocumentPublic[]> {
    return this.http.get<DocumentPublic[]>(this.baseUrl);
  }

  /** Schickt Datei und Metadaten als multipart/form-data in einem Request. */
  upload(file: File, metadata: DocumentUpload): Observable<DocumentPublic> {
    const body = new FormData();
    body.append('file', file);
    // Als JSON-Blob, damit Spring den Part per @RequestPart in das DTO umwandeln kann
    body.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return this.http.post<DocumentPublic>(this.baseUrl, body);
  }

  update(id: number, document: DocumentCreate): Observable<DocumentPublic> {
    return this.http.put<DocumentPublic>(`${this.baseUrl}/${id}`, document);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
