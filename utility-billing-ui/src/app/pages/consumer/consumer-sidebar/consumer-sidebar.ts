import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { AuthService } from '../../../core/auth/auth';

@Component({
  selector: 'app-consumer-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmDialog],
  templateUrl: './consumer-sidebar.html',
  styleUrl: './consumer-sidebar.css',
})
export class ConsumerSidebar {
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
