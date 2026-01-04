import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './billing-officer-dashboard.html',
  styleUrl: './billing-officer-dashboard.css',
})
export class BillingDashboard implements AfterViewInit {
  bills: any[] = [];
  charts: Chart[] = [];

  metersAssigned = 0;
  readingsPending = 0;
  billsToday = 0;
  avgUnitsPerBill = 0;

  today = new Date();

  constructor(private http: HttpClient) {
    this.loadBills();
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderCharts(), 500);
  }

  loadBills() {
    this.http.get<any>('http://localhost:9090/billing?page=0&size=1000').subscribe((res) => {
      this.bills = res.content;
      this.calculateStats();
      this.renderCharts();
    });
  }

  calculateStats() {
    this.metersAssigned = new Set(this.bills.map((b) => b.meterNumber)).size;

    this.readingsPending = this.bills.filter(
      (b) => b.status === 'DUE' || b.status === 'OVERDUE'
    ).length;

    const todayStr = new Date().toISOString().split('T')[0];
    this.billsToday = this.bills.filter((b) => b.generatedAt?.startsWith(todayStr)).length;

    const totalUnits = this.bills.reduce((s, b) => s + Number(b.unitsConsumed || 0), 0);
    this.avgUnitsPerBill = this.bills.length ? Math.round(totalUnits / this.bills.length) : 0;
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    this.renderTariffSlabChart();
    this.renderConsumptionTrend();
    this.renderBillsPerDay();
    this.renderAvgUnitsPerDay();
  }

  renderTariffSlabChart() {
    const slabs: Record<string, number> = {};

    this.bills.forEach((b) => {
      slabs[b.tariffPlan] = (slabs[b.tariffPlan] || 0) + 1;
    });

    const ctx = document.getElementById('slabChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'pie',
        data: { labels: Object.keys(slabs), datasets: [{ data: Object.values(slabs) }] },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }

  renderConsumptionTrend() {
    const map: Record<number, number> = {};

    this.bills.forEach((b) => {
      const t = new Date(b.generatedAt).setHours(0, 0, 0, 0);
      map[t] = (map[t] || 0) + Number(b.unitsConsumed);
    });

    const entries = Object.entries(map)
      .map(([k, v]) => ({ time: +k, units: v }))
      .sort((a, b) => a.time - b.time);

    const ctx = document.getElementById('consumptionChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: entries.map((e) =>
            new Date(e.time).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [{ data: entries.map((e) => e.units) }],
        },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }

  renderBillsPerDay() {
    const map: Record<number, number> = {};

    this.bills.forEach((b) => {
      const t = new Date(b.generatedAt).setHours(0, 0, 0, 0);
      map[t] = (map[t] || 0) + 1;
    });

    const entries = Object.entries(map)
      .map(([k, v]) => ({ time: +k, count: v }))
      .sort((a, b) => a.time - b.time);

    const ctx = document.getElementById('billsPerDayChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: entries.map((e) =>
            new Date(e.time).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [{ data: entries.map((e) => e.count) }],
        },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }

  renderAvgUnitsPerDay() {
    const totals: Record<number, number> = {};
    const counts: Record<number, number> = {};

    this.bills.forEach((b) => {
      const t = new Date(b.generatedAt).setHours(0, 0, 0, 0);
      totals[t] = (totals[t] || 0) + Number(b.unitsConsumed);
      counts[t] = (counts[t] || 0) + 1;
    });

    const entries = Object.keys(totals)
      .map((t) => ({
        time: +t,
        avg: Math.round(totals[+t] / counts[+t]),
      }))
      .sort((a, b) => a.time - b.time);

    const ctx = document.getElementById('avgUnitsChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: entries.map((e) =>
            new Date(e.time).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })
          ),
          datasets: [{ data: entries.map((e) => e.avg), tension: 0.4, fill: true }],
        },
        options: { plugins: { legend: { display: false } } },
      })
    );
  }
}
