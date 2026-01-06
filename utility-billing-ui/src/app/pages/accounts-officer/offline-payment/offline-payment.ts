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
}

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './offline-payment.html',
  styleUrl: './offline-payment.css',
})
export class OfflinePayment {
  allBills: Bill[] = [];
  filteredBills: Bill[] = [];
  pagedBills: Bill[] = [];

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

  loadBills() {
    this.http.get<PageResponse<Bill>>('http://localhost:9090/billing?page=0&size=100').subscribe({
      next: (res) => {
        const dueBills = res.content.filter((b) => b.status === 'OVERDUE' || b.status === 'DUE');

        if (dueBills.length === 0) {
          this.allBills = [];
          this.applyFilters(true);
          return;
        }

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
            this.allBills = data;
            this.applyFilters(true);
          },
          error: () => this.toast.error('Failed to load consumers'),
        });
      },
      error: () => this.toast.error('Failed to load bills'),
    });
  }

  applyFilters(resetPage: boolean = false) {
    if (resetPage) this.page = 0;

    this.filteredBills = this.allBills
      .filter((b) => {
        const matchesSearch =
          !this.search ||
          b.consumerName?.toLowerCase().includes(this.search.toLowerCase()) ||
          b.email?.toLowerCase().includes(this.search.toLowerCase());

        const matchesStatus = this.statusFilter ? b.status === this.statusFilter : true;

        return matchesSearch && matchesStatus;
      })
      .sort((a, b) => {
        if (a.status === b.status) return 0;
        if (a.status === 'OVERDUE') return -1;
        if (b.status === 'OVERDUE') return 1;
        return 0;
      });

    this.totalPages = Math.ceil(this.filteredBills.length / this.size);
    this.updatePagedBills();
  }

  updatePagedBills() {
    const start = this.page * this.size;
    const end = start + this.size;
    this.pagedBills = this.filteredBills.slice(start, end);
  }

  pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  goToPage(p: number) {
    if (p >= 0 && p < this.totalPages) {
      this.page = p;
      this.updatePagedBills();
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.updatePagedBills();
    }
  }

  nextPage() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.updatePagedBills();
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
          this.loadBills();
        },
        error: (e) => {
          this.loading = false;
          this.toast.error(e?.error?.message || 'Payment failed');
        },
      });
  }
}
