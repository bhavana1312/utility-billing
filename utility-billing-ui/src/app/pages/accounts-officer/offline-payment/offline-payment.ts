import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, map } from 'rxjs';

interface Bill {
  billId: string;
  consumerId: string;
  status: string;
  consumerName?: string;
  email?: string;
  utilityType: string;
  tariffPlan: string;
  unitsConsumed: number;
  totalAmount: number;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
}

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './offline-payment.html',
  styleUrl: './offline-payment.css',
})
export class OfflinePayment {
  bills: Bill[] = [];
  filteredBills: Bill[] = [];
  selectedBill: Bill | null = null;

  paymentMode: 'CASH' | 'CHEQUE' = 'CASH';
  loading = false;

  search = '';
  statusFilter = '';

  page = 0;
  size = 10;
  totalPages = 0;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadBills();
  }

  loadBills(page: number = this.page) {
    this.page = page;

    this.http
      .get<PageResponse<Bill>>(`http://localhost:9090/billing?page=${this.page}&size=${this.size}`)
      .subscribe({
        next: (res) => {
          const dueBills = res.content.filter((b) => b.status === 'DUE' || b.status === 'OVERDUE');

          this.totalPages = res.totalPages;

          const requests = dueBills.map((b) =>
            this.http.get<any>(`http://localhost:9090/consumers/${b.consumerId}`).pipe(
              map(
                (c) =>
                  ({
                    ...b,
                    consumerName: c.fullName,
                    email: c.email,
                  } as Bill)
              )
            )
          );

          forkJoin<Bill[]>(requests).subscribe({
            next: (data) => {
              this.bills = data;
              this.applyFilters();
            },
            error: () => this.toast.error('Failed to load consumers'),
          });
        },
        error: () => this.toast.error('Failed to load bills'),
      });
  }

  applyFilters(resetPage: boolean = false) {
    if (resetPage) {
      this.loadBills(0);
      return;
    }

    this.filteredBills = this.bills.filter((b) => {
      const matchesName =
        !this.search ||
        b.consumerName?.toLowerCase().includes(this.search.toLowerCase()) ||
        b.email?.toLowerCase().includes(this.search.toLowerCase());

      const matchesStatus = this.statusFilter ? b.status === this.statusFilter : true;

      return matchesName && matchesStatus;
    });
  }

  pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  goToPage(p: number) {
    if (p >= 0 && p < this.totalPages) {
      this.loadBills(p);
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.loadBills(this.page - 1);
    }
  }

  nextPage() {
    if (this.page + 1 < this.totalPages) {
      this.loadBills(this.page + 1);
    }
  }

  selectBill(b: Bill) {
    this.selectedBill = b;
    this.paymentMode = 'CASH';
  }

  completePayment() {
    if (!this.selectedBill) return;

    this.loading = true;

    this.http
      .post('http://localhost:9090/payments/offline', {
        billId: this.selectedBill.billId,
        mode: this.paymentMode,
      })
      .subscribe({
        next: () => {
          this.toast.success('Payment completed');
          this.loading = false;
          this.selectedBill = null;
          this.loadBills(this.page);
        },
        error: (e) => {
          this.loading = false;
          this.toast.error(e?.error?.message || 'Payment failed');
        },
      });
  }
}
