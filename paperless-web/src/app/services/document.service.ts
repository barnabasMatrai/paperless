import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { DocumentCreate, DocumentPublic } from '../models/document';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private http = inject(HttpClient);
  private baseUrl = `${API_URL}/documents`;

  getAll(): Observable<DocumentPublic[]> {
    return this.http.get<DocumentPublic[]>(this.baseUrl);
  }

  create(document: DocumentCreate): Observable<DocumentPublic> {
    return this.http.post<DocumentPublic>(this.baseUrl, document);
  }

  update(id: number, document: DocumentCreate): Observable<DocumentPublic> {
    return this.http.put<DocumentPublic>(`${this.baseUrl}/${id}`, document);
  }

  // TODO: Datei mitschicken, sobald /api/documents/upload die Datei speichert
  // uploadFile(file: File) {
  //   const body = new FormData();
  //   body.append('file', file);
  //   return this.http.post(`${this.baseUrl}/upload`, body);
  // }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
