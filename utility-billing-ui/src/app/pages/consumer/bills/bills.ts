import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/auth/auth';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, ConsumerSidebar],
  templateUrl: './bills.html',
  styleUrl: './bills.css',
})
export class Bills {
  @ViewChild(ConsumerSidebar) sidebar!: ConsumerSidebar;

  isSidebarCollapsed = false;
  today = new Date();

  bills: any[] = [];
  selectedBill: any = null;
  paymentId = '';
  otp = '';
  showOtp = false;

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadBills();
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
  }

  loadBills() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any[]>(`http://localhost:9090/billing/${consumerId}`).subscribe({
      next: (res) => (this.bills = res),
      error: () => this.toast.error('Failed to load bills'),
    });
  }

  initiate(b: any) {
    this.http
      .post<any>('http://localhost:9090/payments/initiate', {
        billId: b.billId,
      })
      .subscribe({
        next: (res) => {
          this.paymentId = res.id;
          this.showOtp = true;
          this.toast.info('OTP sent to your email');
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Payment initiation failed');
        },
      });
  }

  otpTouched = false;

  confirm() {
    this.otpTouched = true;
    if (this.otp.length !== 6) return;

    this.http
      .post('http://localhost:9090/payments/confirm', {
        paymentId: this.paymentId,
        otp: this.otp,
      })
      .subscribe({
        next: () => {
          this.toast.success('Payment successful');
          this.showOtp = false;
          this.otp = '';
          this.otpTouched = false;
          this.loadBills();
        },
        error: (err) => {
          this.toast.error(err?.error?.message || 'Invalid OTP');
        },
      });
  }

  closeOtp() {
    this.showOtp = false;
    this.otp = '';
    this.otpTouched = false;
  }
}
