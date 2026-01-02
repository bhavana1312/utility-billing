import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { BillingSidebar } from '../billing-sidebar/billing-sidebar';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule, BillingSidebar],
  templateUrl: './billing-officer-dashboard.html',
  styleUrl: './billing-officer-dashboard.css',
})
export class BillingDashboard implements AfterViewInit {
  @ViewChild(BillingSidebar) sidebar!: BillingSidebar;

  bills: any[] = [];
  charts: any[] = [];

  totalBills = 0;
  totalRevenue = 0;
  outstandingAmount = 0;

  isSidebarCollapsed = false;
  today = new Date();

  constructor(private http: HttpClient) {
    this.loadBills();
  }

  onSidebarToggle(collapsed: boolean) {
    this.isSidebarCollapsed = collapsed;
    setTimeout(() => {
      this.charts.forEach((c) => c?.resize());
    }, 310);
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderCharts(), 500);
  }

  loadBills() {
    this.http.get<any[]>('http://localhost:9090/billing').subscribe((res) => {
      this.bills = res;
      this.calculateStats();
      this.renderCharts();
    });
  }

  calculateStats() {
    this.totalBills = this.bills.length;

    this.totalRevenue = this.bills.reduce((sum, b) => sum + Number(b.totalAmount), 0);

    this.outstandingAmount = this.bills
      .filter((b) => b.status === 'DUE' || b.status === 'OVERDUE')
      .reduce((sum, b) => sum + Number(b.totalAmount), 0);
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    this.renderStatusChart();
    this.renderUtilityChart();
    this.renderTimeChart();
  }

  renderStatusChart() {
    const counts = this.bills.reduce((a, b) => {
      a[b.status] = (a[b.status] || 0) + 1;
      return a;
    }, {});

    const ctx = document.getElementById('statusChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['PAID', 'DUE', 'OVERDUE'],
          datasets: [
            {
              data: [counts['PAID'] || 0, counts['DUE'] || 0, counts['OVERDUE'] || 0],
              backgroundColor: ['#22c55e', '#facc15', '#ef4444'],
            },
          ],
        },
        options: { responsive: true, maintainAspectRatio: false },
      })
    );
  }

  renderUtilityChart() {
    const revenue: any = {};
    this.bills.forEach((b) => {
      revenue[b.utilityType] = (revenue[b.utilityType] || 0) + Number(b.totalAmount);
    });

    const ctx = document.getElementById('utilityChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: Object.keys(revenue),
          datasets: [
            {
              label: 'Revenue',
              data: Object.values(revenue),
              backgroundColor: '#2563eb',
            },
          ],
        },
        options: { responsive: true, maintainAspectRatio: false },
      })
    );
  }

  renderTimeChart() {
    const grouped: any = {};
    this.bills.forEach((b) => {
      const d = new Date(b.generatedAt).toISOString().split('T')[0];
      grouped[d] = (grouped[d] || 0) + 1;
    });

    const ctx = document.getElementById('timeChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: Object.keys(grouped),
          datasets: [
            {
              label: 'Bills Generated',
              data: Object.values(grouped),
              tension: 0.3,
              borderColor: '#2563eb',
            },
          ],
        },
        options: { responsive: true, maintainAspectRatio: false },
      })
    );
  }
}
