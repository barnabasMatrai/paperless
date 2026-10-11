import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { ReminderCreate, ReminderPublic, ReminderUpdate } from '../models/reminder';

@Injectable({ providedIn: 'root' })
export class ReminderService {
  private http = inject(HttpClient);
  private baseUrl = `${API_URL}/reminders`;

  create(reminder: ReminderCreate): Observable<ReminderPublic> {
    return this.http.post<ReminderPublic>(this.baseUrl, reminder);
  }

  update(id: number, reminder: ReminderUpdate): Observable<ReminderPublic> {
    return this.http.put<ReminderPublic>(`${this.baseUrl}/${id}`, reminder);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
