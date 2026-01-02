import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AccountsOfficerSidebar } from '../accounts-officer-sidebar/accounts-officer-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, AccountsOfficerSidebar],
  templateUrl: './payments.html',
  styleUrl: './payments.css',
})
export class Payments {
  payments: any[] = [];
  filtered: any[] = [];

  mode = '';
  search = '';

  modes = ['ONLINE', 'CASH', 'CHEQUE'];

  currentPage = 0;
  totalPages = 0;
  pageSize = 10;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadPayments();
  }

  loadPayments(page: number = 0) {
    this.http
      .get<any>(
        `http://localhost:9090/payments?page=${page}&size=${this.pageSize}&search=${this.search}&mode=${this.mode}`
      )
      .subscribe({
        next: (res) => {
          this.payments = res.content;
          this.currentPage = res.number;
          this.totalPages = res.totalPages;
          this.applyFilters();
        },
        error: () => this.toast.error('Failed to load payments'),
      });
  }

  applyFilters(resetPage: boolean = false) {
    if (resetPage) {
      this.currentPage = 0;
    }

    this.filtered = this.payments.filter((p) => {
      const matchesMode = !this.mode || p.mode === this.mode;
      const matchesSearch =
        !this.search ||
        p.billId?.toLowerCase().includes(this.search.toLowerCase()) ||
        p.email?.toLowerCase().includes(this.search.toLowerCase());
      return matchesMode && matchesSearch;
    });
  }

  downloadInvoice(p: any) {
    this.http
      .get(`http://localhost:9090/payments/${p.id}/invoice`, {
        responseType: 'blob',
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `invoice-${p.billId}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: () => this.toast.error('Invoice download failed'),
      });
  }

  nextPage() {
    if (this.currentPage + 1 < this.totalPages) {
      this.loadPayments(this.currentPage + 1);
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.loadPayments(this.currentPage - 1);
    }
  }
}
