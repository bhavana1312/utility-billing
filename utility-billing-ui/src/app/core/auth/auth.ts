import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = 'http://localhost:9090/auth';

  constructor(private readonly http: HttpClient) {}

  login(data: any): Observable<any> {
    return this.http.post<any>(`${this.api}/login`, data).pipe(
      tap((res) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', this.getRole(res.token));
      })
    );
  }

  getRole(token: string) {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.roles?.[0];
  }

  getUserRole() {
    return localStorage.getItem('role');
  }

  isLoggedIn() {
    return !!localStorage.getItem('token');
  }

  logout() {
    localStorage.clear();
  }
}
