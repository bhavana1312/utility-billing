import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  constructor(private readonly auth: AuthService, private readonly router: Router) {
    console.log('navbar');
  }

  isLoggedIn() {
    return this.auth.isLoggedIn();
  }

  isDashboardRoute(): boolean {
    const url = this.router.url;
    return (
      url === '/admin' ||
      url.startsWith('/admin/') ||
      url === '/billing' ||
      url.startsWith('/billing/') ||
      url === '/accounts' ||
      url.startsWith('/accounts/') ||
      url === '/consumer' ||
      url.startsWith('/consumer/')
    );
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  getDashboardRoute(): string | null {
    const role = this.auth.getUserRole();
    switch (role) {
      case 'ROLE_ADMIN':
        return '/admin';
      case 'ROLE_BILLING_OFFICER':
        return '/billing';
      case 'ROLE_ACCOUNTS_OFFICER':
        return '/accounts';
      case 'ROLE_USER':
        return '/consumer';
      default:
        return null;
    }
  }
}
