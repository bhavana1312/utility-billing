import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/auth/auth';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bills.html',
  styleUrl: './bills.css',
})
export class Bills {
  today = new Date();

  bills: any[] = [];
  page = 0;
  size = 10;
  totalPages = 0;

  utilityType = '';
  utilities = ['ELECTRICITY', 'WATER', 'GAS'];

  selectedBill: any = null;
  paymentId = '';
  otp = '';
  showOtp = false;
  otpTouched = false;

  loading = false;

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadBills();
  }

  loadBills() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.loading = true;
    let url = `http://localhost:9090/billing/${consumerId}?page=${this.page}&size=${this.size}`;

    this.http.get<any>(url).subscribe({
      next: (r) => {
        let data = r.content;
        if (this.utilityType) {
          data = data.filter((b: any) => b.utilityType === this.utilityType);
        }
        this.bills = data;
        this.totalPages = r.totalPages;
        this.loading = false;
      },
      error: () => {
        this.toast.error('Failed to load bills');
        this.loading = false;
      },
    });
  }

  onUtilityChange() {
    this.page = 0;
    this.loadBills();
  }

  next() {
    if (this.page + 1 < this.totalPages && !this.loading) {
      this.page++;
      this.loadBills();
    }
  }

  prev() {
    if (this.page > 0 && !this.loading) {
      this.page--;
      this.loadBills();
    }
  }

  goToPage(p: number) {
    if (p >= 0 && p < this.totalPages && !this.loading) {
      this.page = p;
      this.loadBills();
    }
  }

  initiate(b: any) {
    this.loading = true;
    this.selectedBill = b;
    console.log(b.billId);
    this.http
      .post<any>('http://localhost:9090/payments/initiate', {
        billId: b.billId,
      })
      .subscribe({
        next: (r) => {
          this.paymentId = r.id;
          this.showOtp = true;
          this.loading = false;
          this.toast.info('OTP sent to your email');
        },
        error: (e) => {
          this.toast.error(e?.error?.message || 'Payment initiation failed');
          this.loading = false;
        },
      });
  }

  confirm() {
    this.otpTouched = true;
    if (this.otp.length !== 6 || this.loading) return;

    this.loading = true;
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
          this.selectedBill = null;
          this.loadBills();
        },
        error: (e) => {
          this.toast.error(e?.error?.message || 'Invalid OTP');
          this.loading = false;
        },
      });
  }

  closeOtp() {
    if (this.loading) return;
    this.showOtp = false;
    this.otp = '';
    this.otpTouched = false;
    this.selectedBill = null;
  }
}
