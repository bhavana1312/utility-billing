import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { AuthService } from '../../../core/auth/auth';
import { HttpClient } from '@angular/common/http';

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

  consumer: any = null;

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly http: HttpClient
  ) {
    this.loadConsumer();
  }

  toggleSidebar() {
    this.collapsed = !this.collapsed;
    this.sidebarToggle.emit(this.collapsed);
  }

  loadConsumer() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any>(`http://localhost:9090/consumers/${consumerId}`).subscribe((res) => {
      console.log(res);
      this.consumer = res;
      console.log(this.consumer);
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
