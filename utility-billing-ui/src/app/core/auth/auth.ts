import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = 'http://localhost:9090/auth';
  private readonly TOKEN_KEY = 'token';
  private readonly ROLE_KEY = 'role';

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(data: any): Observable<any> {
    return this.http.post<any>(`${this.api}/login`, data).pipe(
      tap((res) => {
        localStorage.setItem(this.TOKEN_KEY, res.token);
        localStorage.setItem(this.ROLE_KEY, this.getRole(res.token));
      })
    );
  }

  getRole(token: string): string {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.roles?.[0] ?? '';
    } catch {
      return '';
    }
  }

  getUserRole(): string | null {
    return localStorage.getItem(this.ROLE_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && !this.isTokenExpired();
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isTokenExpired(): boolean {
    try {
      const token = this.getToken();
      if (!token) return true;

      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp * 1000;
      return Date.now() > expiry;
    } catch {
      return true;
    }
  }

  checkTokenExpiry() {
    if (this.isTokenExpired()) {
      this.logout();
    }
  }
}
