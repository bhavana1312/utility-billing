import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payments.html',
  styleUrl: './payments.css',
})
export class Payments {
  today = new Date();

  payments: any[] = [];
  page = 0;
  size = 10;
  totalPages = 0;

  utilityType = '';
  utilities = ['ELECTRICITY', 'WATER', 'GAS'];

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadPayments();
  }

  loadPayments() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    let url = `http://localhost:9090/payments/history/${consumerId}?page=${this.page}&size=${this.size}`;

    if (this.utilityType) {
      url += `&utilityType=${this.utilityType}`;
    }

    this.http.get<any>(url).subscribe({
      next: (r) => {
        this.payments = r.content;
        this.totalPages = r.totalPages;
      },
      error: () => this.toast.error('Failed to load payments'),
    });
  }

  onUtilityChange() {
    this.page = 0;
    this.loadPayments();
  }

  next() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadPayments();
    }
  }

  prev() {
    if (this.page > 0) {
      this.page--;
      this.loadPayments();
    }
  }

  goToPage(p: number) {
    if (p >= 0 && p < this.totalPages) {
      this.page = p;
      this.loadPayments();
    }
  }

  downloadInvoice(id: string) {
    this.http
      .get(`http://localhost:9090/payments/${id}/invoice`, { responseType: 'blob' })
      .subscribe((b) => {
        const url = URL.createObjectURL(b);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice-${id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      });
  }
}
