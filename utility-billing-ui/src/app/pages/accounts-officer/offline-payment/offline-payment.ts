import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AccountsOfficerSidebar } from '../accounts-officer-sidebar/accounts-officer-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, AccountsOfficerSidebar],
  templateUrl: './offline-payment.html',
  styleUrl: './offline-payment.css',
})
export class OfflinePayment {
  bills: any[] = [];
  selectedBill: any = null;
  paymentMode: 'CASH' | 'CHEQUE' = 'CASH';
  loading = false;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadBills();
  }

  loadBills() {
    this.http.get<any[]>('http://localhost:9090/billing').subscribe({
      next: (res) => {
        this.bills = res.filter((b) => b.status === 'DUE' || b.status === 'OVERDUE');
      },
      error: () => this.toast.error('Failed to load bills'),
    });
  }

  selectBill(b: any) {
    this.selectedBill = b;
    this.paymentMode = 'CASH';
  }

  completePayment() {
    if (!this.selectedBill) {
      this.toast.warning('Select a bill');
      return;
    }

    this.loading = true;

    this.http
      .post('http://localhost:9090/payments/offline', {
        billId: this.selectedBill.billId,
        mode: this.paymentMode,
      })
      .subscribe({
        next: () => {
          this.toast.success('Payment completed successfully');
          this.loading = false;
          this.selectedBill = null;
          this.loadBills();
        },
        error: (err) => {
          this.loading = false;
          this.toast.error(err?.error?.message || 'Payment failed');
        },
      });
  }
}
