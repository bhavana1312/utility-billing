import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { forkJoin } from 'rxjs';

Chart.register(...registerables);

interface PageResponse<T> {
  content: T[];
}

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements AfterViewInit {
  consumers: any[] = [];
  consumerMap: Record<string, any> = {};
  bills: any[] = [];
  charts: Chart[] = [];

  totalConsumers = 0;
  activeConnections = 0;
  monthlyRevenue = 0;
  overdueCount = 0;

  today = new Date();

  constructor(private http: HttpClient) {
    this.loadData();
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderCharts(), 500);
  }

  loadData() {
    this.http.get<any[]>('http://localhost:9090/consumers').subscribe((c) => {
      this.consumers = c;
      c.forEach((x) => (this.consumerMap[x.id] = x));
      this.totalConsumers = c.length;
      this.activeConnections = c.filter((x) => x.active).length;
      this.renderCharts();
    });

    this.http
      .get<PageResponse<any>>('http://localhost:9090/billing?page=0&size=1000')
      .subscribe((b) => {
        this.bills = b.content;
        this.loadMissingConsumers();
        this.calculateStats();
        this.renderCharts();
      });
  }

  loadMissingConsumers() {
    const ids = [
      ...new Set(this.bills.map((b) => b.consumerId).filter((id) => !this.consumerMap[id])),
    ];

    if (!ids.length) return;

    forkJoin(
      ids.map((id) => this.http.get<any>(`http://localhost:9090/consumers/${id}`))
    ).subscribe((res) => {
      res.forEach((c) => (this.consumerMap[c.id] = c));
      this.renderCharts();
    });
  }

  calculateStats() {
    const now = new Date();
    const month = now.getMonth();
    const year = now.getFullYear();

    this.monthlyRevenue = this.bills
      .filter((b) => {
        const d = new Date(b.generatedAt);
        return !isNaN(d.getTime()) && d.getMonth() === month && d.getFullYear() === year;
      })
      .reduce((s, b) => s + Number(b.totalAmount), 0);

    this.overdueCount = this.bills.filter((b) => b.status === 'OVERDUE').length;
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    this.renderConsumerGrowth();
    this.renderRevenueByUtility();
    this.renderTopConsumers();
    this.renderAverageBillValue();
  }

  renderConsumerGrowth() {
    const grouped: Record<string, number> = {};
    console.log(this.consumers);
    this.consumers.forEach((c) => {
      const dateValue = c.createdAt || c.createdDate || c.registeredAt;
      if (!dateValue) return;

      const d = new Date(dateValue);
      if (isNaN(d.getTime())) return;

      const key =
        d.getFullYear() +
        '-' +
        String(d.getMonth() + 1).padStart(2, '0') +
        '-' +
        String(d.getDate()).padStart(2, '0');

      grouped[key] = (grouped[key] || 0) + 1;
    });

    if (!Object.keys(grouped).length) {
      const now = new Date();
      const today =
        now.getFullYear() +
        '-' +
        String(now.getMonth() + 1).padStart(2, '0') +
        '-' +
        String(now.getDate()).padStart(2, '0');

      grouped[today] = this.consumers.length;
    }

    const labels = Object.keys(grouped).sort();
    const values = labels.map((l) => grouped[l]);

    const ctx = document.getElementById('consumerGrowthChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              data: values,
              fill: true,
              tension: 0.3,
            },
          ],
        },
        options: {
          plugins: {
            legend: { display: false },
          },
        },
      })
    );
  }

  renderRevenueByUtility() {
    const grouped: Record<string, number> = {};

    this.bills.forEach((b) => {
      grouped[b.utilityType] = (grouped[b.utilityType] || 0) + Number(b.totalAmount);
    });

    const ctx = document.getElementById('revenueUtilityChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: Object.keys(grouped),
          datasets: [{ data: Object.values(grouped) }],
        },
      })
    );
  }

  renderTopConsumers() {
    const totals: Record<string, number> = {};

    this.bills.forEach((b) => {
      totals[b.consumerId] = (totals[b.consumerId] || 0) + Number(b.totalAmount);
    });

    const top = Object.entries(totals)
      .map(([consumerId, amount]) => ({
        name: this.consumerMap[consumerId]?.fullName || consumerId,
        amount,
      }))
      .sort((a, b) => b.amount - a.amount)
      .slice(0, 5);

    const ctx = document.getElementById('topConsumersChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: top.map((x) => x.name),
          datasets: [{ data: top.map((x) => x.amount) }],
        },
        options: {
          indexAxis: 'y',
          plugins: { legend: { display: false } },
        },
      })
    );
  }

  renderAverageBillValue() {
    const totals: Record<string, number> = {};
    const counts: Record<string, number> = {};

    this.bills.forEach((b) => {
      if (!b.generatedAt) return;
      const d = new Date(b.generatedAt);
      if (isNaN(d.getTime())) return;

      const key = d.toISOString().split('T')[0];
      totals[key] = (totals[key] || 0) + Number(b.totalAmount);
      counts[key] = (counts[key] || 0) + 1;
    });

    const labels = Object.keys(totals).sort();
    const values = labels.map((l) => Math.round(totals[l] / counts[l]));

    const ctx = document.getElementById('avgBillChart') as HTMLCanvasElement;
    if (!ctx || !labels.length) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels,
          datasets: [{ data: values, tension: 0.3 }],
        },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }
}
