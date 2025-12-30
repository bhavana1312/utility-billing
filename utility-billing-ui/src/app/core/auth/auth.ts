import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = 'http://localhost:9090/auth';

  constructor(private http: HttpClient) {}

  login(data: any) {
    this.http
      .post<any>(`${this.api}/login`, data)
      .subscribe((res) => localStorage.setItem('token', res.token));
  }

  logout() {
    localStorage.clear();
  }

  isLoggedIn() {
    return !!localStorage.getItem('token');
  }
}
