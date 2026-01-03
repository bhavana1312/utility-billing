import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = 'http://localhost:9090/auth';
  private readonly TOKEN_KEY = 'token';

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(data: any): Observable<any> {
    return this.http.post<any>(`${this.api}/login`, data).pipe(
      tap((res) => {
        localStorage.setItem(this.TOKEN_KEY, res.token);
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private getPayload(): any {
    const token = this.getToken();
    if (!token) return null;
    return JSON.parse(atob(token.split('.')[1]));
  }

  getUserRole(): string {
    return this.getPayload()?.roles?.[0] ?? '';
  }

  getConsumerId(): string | null {
    return this.getPayload()?.sub ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && !this.isTokenExpired();
  }

  isTokenExpired(): boolean {
    const payload = this.getPayload();
    if (!payload) return true;
    return Date.now() > payload.exp * 1000;
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  checkTokenExpiry() {
    if (this.isTokenExpired()) this.logout();
  }
}
