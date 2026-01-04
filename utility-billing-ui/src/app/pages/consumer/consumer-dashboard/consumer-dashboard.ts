import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { AuthService } from '../../../core/auth/auth';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './consumer-dashboard.html',
  styleUrl: './consumer-dashboard.css',
})
export class ConsumerDashboard implements AfterViewInit {
  today = new Date();

  bills: any[] = [];
  payments: any[] = [];
  charts: any[] = [];

  constructor(private http: HttpClient, private auth: AuthService) {
    this.loadBills();
    this.loadPayments();
  }

  ngAfterViewInit() {}

  loadBills() {
    const id = this.auth.getConsumerId();
    if (!id) return;

    this.http.get<any>(`http://localhost:9090/billing/${id}?page=0&size=50`).subscribe((r) => {
      this.bills = r.content;
      this.renderCharts();
    });
  }

  loadPayments() {
    const id = this.auth.getConsumerId();
    if (!id) return;

    this.http
      .get<any>(`http://localhost:9090/payments/history/${id}?page=0&size=50`)
      .subscribe((r) => {
        this.payments = r.content;
        this.renderCharts();
      });
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    const sorted = [...this.bills].sort(
      (a, b) => new Date(a.generatedAt).getTime() - new Date(b.generatedAt).getTime()
    );

    this.billAmountTrend(sorted);
    this.utilitySplit(sorted);
    this.billBreakdown(sorted);
    this.monthlyConsumption(sorted);
  }

  billAmountTrend(bills: any[]) {
    const ctx = document.getElementById('amountTrend') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: bills.map((b) =>
            new Date(b.generatedAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [
            {
              data: bills.map((b) => b.totalAmount),
              fill: true,
              tension: 0.4,
            },
          ],
        },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }

  utilitySplit(bills: any[]) {
    const ctx = document.getElementById('utilitySplit') as HTMLCanvasElement;
    if (!ctx) return;

    const utilities = ['ELECTRICITY', 'WATER', 'GAS'];
    const data = utilities.map((u) =>
      bills.filter((b) => b.utilityType === u).reduce((s, b) => s + b.unitsConsumed, 0)
    );

    this.charts.push(
      new Chart(ctx, {
        type: 'pie',
        data: { labels: utilities, datasets: [{ data }] },
        options: { responsive: true },
      })
    );
  }

  billBreakdown(bills: any[]) {
    const ctx = document.getElementById('billBreakdown') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: bills.map((b) =>
            new Date(b.generatedAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [
            { label: 'Fixed', data: bills.map((b) => b.fixedCharge), stack: 'a' },
            { label: 'Energy', data: bills.map((b) => b.energyCharge), stack: 'a' },
            { label: 'Tax', data: bills.map((b) => b.taxAmount), stack: 'a' },
          ],
        },
        options: { indexAxis: 'y', scales: { x: { stacked: true }, y: { stacked: true } } },
      })
    );
  }

  monthlyConsumption(bills: any[]) {
    const ctx = document.getElementById('consumptionTrend') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: bills.map((b) =>
            new Date(b.generatedAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [
            {
              data: bills.map((b) => b.unitsConsumed),
            },
          ],
        },
        options: { indexAxis: 'x', plugins: { legend: { display: false } } },
      })
    );
  }

  get totalBills() {
    return this.bills.length;
  }

  get dueAmount() {
    return this.bills
      .filter((b) => b.status === 'DUE' || b.status === 'OVERDUE')
      .reduce((s, b) => s + b.totalAmount, 0);
  }

  get nextDueDays() {
    const due = this.bills
      .filter((b) => b.status === 'DUE' || b.status === 'OVERDUE')
      .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());
    if (!due.length) return 0;
    return Math.ceil((new Date(due[0].dueDate).getTime() - Date.now()) / 86400000);
  }

  get lastPaymentStatus() {
    if (!this.payments.length) return '—';
    return this.payments[0].status;
  }
}
