import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule, ConsumerSidebar],
  templateUrl: './consumer-dashboard.html',
  styleUrl: './consumer-dashboard.css',
})
export class ConsumerDashboard implements AfterViewInit {
  @ViewChild(ConsumerSidebar) sidebar!: ConsumerSidebar;

  isSidebarCollapsed = false;
  today = new Date();

  bills: any[] = [];
  payments: any[] = [];
  charts: any[] = [];

  constructor(private http: HttpClient) {
    this.loadBills();
    this.loadPayments();
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
    setTimeout(() => this.charts.forEach((c) => c.resize()), 310);
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderCharts(), 500);
  }

  loadBills() {
    this.http.get<any[]>('http://localhost:9090/billing').subscribe((res) => {
      this.bills = res;
      this.renderCharts();
    });
  }

  loadPayments() {
    this.http.get<any[]>('http://localhost:9090/payments').subscribe((res) => {
      this.payments = res;
      this.renderCharts();
    });
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    const pending = this.bills.filter((b) => b.status === 'PENDING').length;
    const paid = this.bills.filter((b) => b.status === 'PAID').length;

    const statusCtx = document.getElementById('billStatusChart') as HTMLCanvasElement;
    if (statusCtx) {
      this.charts.push(
        new Chart(statusCtx, {
          type: 'doughnut',
          data: {
            labels: ['Paid', 'Pending'],
            datasets: [
              {
                data: [paid, pending],
                backgroundColor: ['#22c55e', '#facc15'],
              },
            ],
          },
          options: { responsive: true, maintainAspectRatio: false },
        })
      );
    }

    const paymentCtx = document.getElementById('paymentChart') as HTMLCanvasElement;
    if (paymentCtx) {
      this.charts.push(
        new Chart(paymentCtx, {
          type: 'bar',
          data: {
            labels: ['Payments', 'Pending Bills'],
            datasets: [
              {
                data: [this.payments.length, pending],
                backgroundColor: ['#2563eb', '#f97316'],
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
          },
        })
      );
    }

    const consumptionCtx = document.getElementById('consumptionChart') as HTMLCanvasElement;
    if (consumptionCtx) {
      this.charts.push(
        new Chart(consumptionCtx, {
          type: 'line',
          data: {
            labels: this.bills.map((b) => b.month),
            datasets: [
              {
                data: this.bills.map((b) => b.units),
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37,99,235,0.1)',
                fill: true,
                tension: 0.4,
              },
            ],
          },
          options: { responsive: true, maintainAspectRatio: false },
        })
      );
    }
  }

  pay(id: string) {
    this.http
      .post('http://localhost:9090/payments/initiate', { billId: id })
      .subscribe(() => this.loadBills());
  }

  get pendingBills() {
    return this.bills.filter((b) => b.status === 'PENDING').length;
  }
}
