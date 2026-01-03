import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, map } from 'rxjs';

interface Bill {
  billId: string;
  consumerId: string;
  utilityType: string;
  tariffPlan: string;
  status: string;
  consumerName?: string;
  email?: string;
  unitsConsumed?: number;
  totalAmount: number;
  generatedAt: Date;
  dueDate: Date;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  number: number;
}

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bills.html',
  styleUrl: './bills.css',
})
export class Bills {
  bills: Bill[] = [];
  filtered: Bill[] = [];

  status = '';
  utility = '';
  tariffPlan = '';

  utilities = ['WATER', 'ELECTRICITY', 'GAS'];
  tariffPlans = ['DOMESTIC', 'COMMERCIAL', 'INDUSTRIAL'];
  statuses = ['DUE', 'PAID', 'OVERDUE'];

  page = 0;
  size = 5;
  totalPages = 0;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadBills();
  }

  loadBills(page: number = this.page) {
    this.page = page;

    const params = new URLSearchParams({
      page: this.page.toString(),
      size: this.size.toString(),
    });

    if (this.status) {
      params.append('status', this.status);
    }

    this.http
      .get<PageResponse<Bill>>(`http://localhost:9090/billing?${params.toString()}`)
      .subscribe({
        next: (res) => {
          this.totalPages = res.totalPages;
          console.log(res.content);

          const consumerRequests = res.content.map((b) =>
            this.getConsumer(b.consumerId).pipe(
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

          forkJoin<Bill[]>(consumerRequests).subscribe({
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

  applyFilters() {
    this.filtered = this.bills.filter((b) => {
      return (
        (!this.utility || b.utilityType === this.utility) &&
        (!this.tariffPlan || b.tariffPlan === this.tariffPlan)
      );
    });
  }

  onFilterChange() {
    this.loadBills(0);
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

  getConsumer(id: string) {
    return this.http.get<any>(`http://localhost:9090/consumers/${id}`);
  }
}
