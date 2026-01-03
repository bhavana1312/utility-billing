import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './sidebar/sidebar';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth/auth';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, Sidebar],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayout {
  sidebarCollapsed = true;

  constructor(private auth: AuthService) {}

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  onSidebarToggle(state: boolean) {
    this.sidebarCollapsed = state;
  }

  roleLabel() {
    const role = this.auth.getUserRole();
    if (role === 'ROLE_ADMIN') return 'Admin';
    if (role === 'ROLE_BILLING_OFFICER') return 'Billing';
    if (role === 'ROLE_ACCOUNTS_OFFICER') return 'Accounts';
    return 'Consumer';
  }
}
