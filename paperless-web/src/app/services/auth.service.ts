import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of, finalize, map } from 'rxjs';
import { UserLoginPublic } from '../dtos/in/user-login-public';
import { AuthCreate } from '../dtos/out/auth-create';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/auth';

  readonly currentUser = signal<UserLoginPublic | null>(null);
  readonly isAuthenticated = signal(false);
  readonly isLoading = signal(false);

  login(credentials: AuthCreate): Observable<UserLoginPublic> {
    this.isLoading.set(true);

    return this.http
      .post<UserLoginPublic>(
        `${this.apiUrl}/login`,
        credentials,
        { withCredentials: true },
      )
      .pipe(
        tap((user) => {
          this.currentUser.set(user);
          this.isAuthenticated.set(true);
        }),
        finalize(() => this.isLoading.set(false)),
      );
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(
        `${this.apiUrl}/logout`,
        {},
        { withCredentials: true },
      )
      .pipe(
        tap(() => this.clearAuth()),
        catchError((error) => {
          this.clearAuth();
          throw error;
        }),
      );
  }

  private clearAuth(): void {
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }

  checkAuth(): Observable<boolean> {
    return this.http
      .get<UserLoginPublic>(
        `${this.apiUrl}/me`,
        { withCredentials: true },
      )
      .pipe(
        tap((user) => {
          this.currentUser.set(user);
          this.isAuthenticated.set(true);
        }),
        map(() => true),
        catchError(() => {
          this.currentUser.set(null);
          this.isAuthenticated.set(false);
          return of(false);
        }),
      );
  }
}
