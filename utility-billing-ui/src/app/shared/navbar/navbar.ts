import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../core/auth/auth';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  isLoggedIn() {
    return this.auth.isLoggedIn();
  }

  isAdminPage(): boolean {
    return this.router.url.includes('/admin');
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
      case 'ROLE_USER':
        return '/user';
      case 'ROLE_BILLING_OFFICER':
        return '/billing';
      case 'ROLE_ACCOUNTS_OFFICER':
        return '/accounts';
      default:
        return null;
    }
  }
}
