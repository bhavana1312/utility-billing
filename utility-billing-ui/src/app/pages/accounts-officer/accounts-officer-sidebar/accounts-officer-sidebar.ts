import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-accounts-officer-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmDialog],
  templateUrl: './accounts-officer-sidebar.html',
  styleUrl: './accounts-officer-sidebar.css',
})
export class AccountsOfficerSidebar {
  @Output() sidebarToggle = new EventEmitter<boolean>();
  collapsed = false;
  showLogoutConfirm = false;

  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  toggleSidebar() {
    this.collapsed = !this.collapsed;
    this.sidebarToggle.emit(this.collapsed);
  }

  openLogoutConfirm() {
    this.showLogoutConfirm = true;
  }

  confirmLogout() {
    this.showLogoutConfirm = false;
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  cancelLogout() {
    this.showLogoutConfirm = false;
  }
}
