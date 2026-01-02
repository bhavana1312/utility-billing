import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { AccountsOfficerSidebar } from '../accounts-officer-sidebar/accounts-officer-sidebar';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule, AccountsOfficerSidebar],
  templateUrl: './accounts-officer-dashboard.html',
  styleUrl: './accounts-officer-dashboard.css',
})
export class AccountsOfficerDashboard implements AfterViewInit {
  @ViewChild('sidebar') sidebar: any;

  isSidebarCollapsed = false;
  today = new Date();

  payments: any[] = [];
  recentPayments: any[] = [];

  totalCollected = 0;
  offlinePaymentsCount = 0;

  constructor(private http: HttpClient) {
    this.loadPayments();
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderChart(), 300);
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
  }

  loadPayments() {
    this.http.get<any[]>('http://localhost:9090/payments').subscribe({
      next: (res) => {
        this.payments = res;
        this.calculateStats();
        this.prepareRecentPayments();
        this.renderChart();
      },
    });
  }

  calculateStats() {
    this.totalCollected = this.payments.reduce((sum, p) => sum + p.amount, 0);

    this.offlinePaymentsCount = this.payments.filter(
      (p) => p.mode === 'CASH' || p.mode === 'CHEQUE'
    ).length;
  }

  prepareRecentPayments() {
    this.recentPayments = [...this.payments]
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 5)
      .map((p) => ({
        consumerName: p.email,
        method: p.mode,
        amount: p.amount,
      }));
  }

  renderChart() {
    if (!this.payments.length) return;

    const modeMap: any = { ONLINE: 0, CASH: 0, CHEQUE: 0 };
    this.payments.forEach((p) => modeMap[p.mode]++);

    const canvas = document.getElementById('paymentChart') as HTMLCanvasElement;
    if (!canvas) return;

    new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: Object.keys(modeMap),
        datasets: [
          {
            data: Object.values(modeMap),
            backgroundColor: ['#2563eb', '#16a34a', '#f97316'],
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' },
        },
      },
    });
  }
}
