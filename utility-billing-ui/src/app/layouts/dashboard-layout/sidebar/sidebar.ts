import { Component, EventEmitter, Output, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { SIDEBAR_CONFIG, UserRole } from './sidebar.config';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, ConfirmDialog],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit {
  @Output() sidebarToggle = new EventEmitter<boolean>();

  @Input() collapsed = false;
  showLogoutConfirm = false;
  config = SIDEBAR_CONFIG.ROLE_USER;

  consumer: any;

  constructor(private auth: AuthService, private router: Router, private http: HttpClient) {
    const role = this.auth.getUserRole() as UserRole;
    if (role && SIDEBAR_CONFIG[role]) {
      this.config = SIDEBAR_CONFIG[role];
    }
  }

  ngOnInit() {
    if (this.auth.getUserRole() === 'ROLE_USER') {
      this.loadConsumer();
    }
  }

  loadConsumer() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any>(`http://localhost:9090/consumers/${consumerId}`).subscribe((res) => {
      this.consumer = res;
    });
  }

  get initials() {
    if (!this.consumer?.fullName) return 'C';
    return this.consumer.fullName
      .split(' ')
      .map((n: string) => n[0])
      .join('')
      .substring(0, 2)
      .toUpperCase();
  }

  toggleSidebar() {
    this.sidebarToggle.emit(!this.collapsed);
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

  get isConsumer() {
    return this.auth.getUserRole() === 'ROLE_USER';
  }
}
