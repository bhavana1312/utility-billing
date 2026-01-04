import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

type BillStatus = 'PAID' | 'DUE' | 'OVERDUE';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './accounts-officer-dashboard.html',
  styleUrl: './accounts-officer-dashboard.css',
})
export class AccountsOfficerDashboard implements AfterViewInit {
  today = new Date();

  payments: any[] = [];
  bills: { status: BillStatus; totalAmount: number; dueDate: string; consumerId: string }[] = [];
  consumers: Record<string, string> = {};

  totalCollected = 0;
  todayCollection = 0;
  outstandingAmount = 0;
  overdueCount = 0;

  charts: Chart[] = [];

  constructor(private http: HttpClient) {
    this.loadData();
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderCharts(), 300);
  }

  loadData() {
    this.http.get<any>('http://localhost:9090/payments?page=0&size=1000').subscribe((res) => {
      this.payments = res.content;
      this.calculateKpis();
      this.renderCharts();
    });

    this.http.get<any>('http://localhost:9090/billing?page=0&size=1000').subscribe((res) => {
      this.bills = res.content;
      this.calculateBillStats();
      this.loadConsumers();
      this.renderCharts();
    });
  }

  loadConsumers() {
    const ids = [...new Set(this.bills.map((b) => b.consumerId))];

    ids.forEach((id) => {
      if (this.consumers[id]) return;

      this.http.get<any>(`http://localhost:9090/consumers/${id}`).subscribe((res) => {
        this.consumers[id] = res.fullName;
        this.renderCharts();
      });
    });
  }

  calculateKpis() {
    const todayStr = new Date().toDateString();

    let total = 0;
    let todayTotal = 0;

    this.payments
      .filter((p) => p.status === 'SUCCESS')
      .forEach((p) => {
        total += p.amount;
        if (new Date(p.completedAt).toDateString() === todayStr) {
          todayTotal += p.amount;
        }
      });

    this.totalCollected = Number(total.toFixed(2));
    this.todayCollection = Number(todayTotal.toFixed(2));
  }

  calculateBillStats() {
    let outstanding = 0;
    let overdue = 0;

    this.bills.forEach((b) => {
      if (b.status !== 'PAID') outstanding += b.totalAmount;
      if (b.status === 'OVERDUE') overdue++;
    });

    this.outstandingAmount = Number(outstanding.toFixed(2));
    this.overdueCount = overdue;
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    if (this.payments.length) {
      this.renderCollectionTrend();
      this.renderModeChart();
    }

    if (this.bills.length) {
      this.renderBillStatus();
      this.renderDefaulters();
    }
  }

  renderCollectionTrend() {
    const map: Record<number, number> = {};

    this.payments
      .filter((p) => p.status === 'SUCCESS')
      .forEach((p) => {
        const t = new Date(p.completedAt).setHours(0, 0, 0, 0);
        map[t] = (map[t] || 0) + p.amount;
      });

    const entries = Object.entries(map)
      .map(([k, v]) => ({ time: +k, amount: Number(v.toFixed(2)) }))
      .sort((a, b) => a.time - b.time);

    this.charts.push(
      new Chart('collectionTrend', {
        type: 'line',
        data: {
          labels: entries.map((e) =>
            new Date(e.time).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [
            {
              data: entries.map((e) => e.amount),
              fill: true,
              tension: 0.4,
            },
          ],
        },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }

  renderModeChart() {
    const m = { ONLINE: 0, OFFLINE: 0 };

    this.payments.forEach((p) => {
      p.mode === 'ONLINE' ? m.ONLINE++ : m.OFFLINE++;
    });

    this.charts.push(
      new Chart('modeChart', {
        type: 'pie',
        data: { labels: Object.keys(m), datasets: [{ data: Object.values(m) }] },
      })
    );
  }

  renderBillStatus() {
    const s: { [k in BillStatus]: number } = { PAID: 0, DUE: 0, OVERDUE: 0 };

    this.bills.forEach((b) => s[b.status]++);

    this.charts.push(
      new Chart('billStatusChart', {
        type: 'bar',
        data: { labels: Object.keys(s), datasets: [{ data: Object.values(s) }] },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }

  renderDefaulters() {
    const map: Record<string, number> = {};

    this.bills
      .filter((b) => b.status === 'OVERDUE')
      .forEach((b) => {
        map[b.consumerId] = (map[b.consumerId] || 0) + b.totalAmount;
      });

    const entries = Object.entries(map)
      .map(([id, amount]) => ({
        name: this.consumers[id] || 'Loading...',
        amount: Number(amount.toFixed(2)),
      }))
      .sort((a, b) => b.amount - a.amount)
      .slice(0, 5);

    this.charts.push(
      new Chart('defaultersChart', {
        type: 'bar',
        data: {
          labels: entries.map((e) => e.name),
          datasets: [{ data: entries.map((e) => e.amount) }],
        },
        options: { indexAxis: 'y', plugins: { legend: { display: false } } },
      })
    );
  }
}
