import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { BillingSidebar } from '../billing-sidebar/billing-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, BillingSidebar],
  templateUrl: './bills.html',
  styleUrl: './bills.css',
})
export class Bills {
  bills: any[] = [];
  filtered: any[] = [];

  status = '';
  utility = '';
  tariffPlan = '';

  utilities = ['WATER', 'ELECTRICITY', 'GAS'];
  tariffPlans = ['DOMESTIC', 'COMMERCIAL', 'INDUSTRIAL'];
  statuses = ['DUE', 'PAID', 'OVERDUE'];

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadBills();
  }

  loadBills() {
    const url = this.status
      ? `http://localhost:9090/billing?status=${this.status}`
      : 'http://localhost:9090/billing';

    this.http.get<any[]>(url).subscribe({
      next: (res) => {
        this.bills = res;
        console.log(this.bills);
        this.applyFilters();
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
    this.loadBills();
  }
}
