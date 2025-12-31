import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = 'http://localhost:9090/auth';

  constructor(private http: HttpClient) {}

  login(data: any): Observable<any> {
    return this.http
      .post<any>(`${this.api}/login`, data)
      .pipe(tap((res) => localStorage.setItem('token', res.token)));
  }

  logout() {
    localStorage.clear();
  }

  isLoggedIn() {
    return !!localStorage.getItem('token');
  }
}
