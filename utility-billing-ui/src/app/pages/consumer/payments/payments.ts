import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';
import { AuthService } from '../../../core/auth/auth';
import { ToastrService } from 'ngx-toastr';

@Component({
  standalone: true,
  imports: [CommonModule, ConsumerSidebar],
  templateUrl: './payments.html',
  styleUrl: './payments.css',
})
export class Payments {
  @ViewChild(ConsumerSidebar) sidebar!: ConsumerSidebar;

  isSidebarCollapsed = false;
  today = new Date();

  payments: any[] = [];

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadPayments();
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
  }

  loadPayments() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any[]>(`http://localhost:9090/payments/history/${consumerId}`).subscribe({
      next: (res) => (this.payments = res),
      error: () => this.toast.error('Failed to load payments'),
    });
  }

  downloadInvoice(paymentId: string) {
    this.http
      .get(`http://localhost:9090/payments/${paymentId}/invoice`, {
        responseType: 'blob',
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `invoice-${paymentId}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: () => this.toast.error('Failed to download invoice'),
      });
  }
}
