import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';
import { Chart, registerables } from 'chart.js';
import { AuthService } from '../../../core/auth/auth';

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

  private billsLoaded = false;
  private paymentsLoaded = false;

  constructor(private http: HttpClient, private auth: AuthService) {
    this.loadBills();
    this.loadPayments();
  }

  ngAfterViewInit() {}

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
    setTimeout(() => this.charts.forEach((c) => c.resize()), 300);
  }

  loadBills() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any[]>(`http://localhost:9090/billing/${consumerId}`).subscribe((res) => {
      this.bills = res;
      this.billsLoaded = true;
      this.tryRenderCharts();
    });
  }

  loadPayments() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http
      .get<any[]>(`http://localhost:9090/payments/history/${consumerId}`)
      .subscribe((res) => {
        this.payments = res;
        this.paymentsLoaded = true;
        this.tryRenderCharts();
      });
  }

  tryRenderCharts() {
    if (this.billsLoaded && this.paymentsLoaded) {
      setTimeout(() => this.renderCharts(), 0);
    }
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    const due = this.bills.filter((b) => b.status === 'DUE' || b.status === 'OVERDUE').length;
    const paid = this.bills.filter((b) => b.status === 'PAID').length;

    const statusCtx = document.getElementById('billStatusChart') as HTMLCanvasElement;
    if (statusCtx) {
      this.charts.push(
        new Chart(statusCtx, {
          type: 'doughnut',
          data: {
            labels: ['Paid', 'Due / Overdue'],
            datasets: [
              {
                data: [paid, due],
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
                data: [this.payments.length, due],
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
            labels: this.bills.map((b) =>
              new Date(b.generatedAt).toLocaleString('default', { month: 'short' })
            ),
            datasets: [
              {
                data: this.bills.map((b) => b.unitsConsumed),
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

  pay(billId: string) {
    this.http.post('http://localhost:9090/payments/initiate', { billId }).subscribe(() => {
      this.loadBills();
      this.loadPayments();
    });
  }

  get pendingBills() {
    return this.bills.filter((b) => b.status === 'DUE' || b.status === 'OVERDUE').length;
  }
}
