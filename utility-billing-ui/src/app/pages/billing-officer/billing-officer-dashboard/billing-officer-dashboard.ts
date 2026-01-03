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

    const totalUnits = this.bills.reduce((sum, b) => sum + Number(b.unitsConsumed || 0), 0);
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
    const slabs: any = {};
    this.bills.forEach((b) => {
      slabs[b.tariffPlan] = (slabs[b.tariffPlan] || 0) + 1;
    });

    const ctx = document.getElementById('slabChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'pie',
        data: {
          labels: Object.keys(slabs),
          datasets: [{ data: Object.values(slabs) }],
        },
        options: { indexAxis: 'y', plugins: { legend: { display: false } } },
      })
    );
  }

  renderConsumptionTrend() {
    const dailyUnits: any = {};
    this.bills.forEach((b) => {
      const d = new Date(b.generatedAt).toISOString().split('T')[0];
      dailyUnits[d] = (dailyUnits[d] || 0) + Number(b.unitsConsumed);
    });

    const ctx = document.getElementById('consumptionChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: Object.keys(dailyUnits),
          datasets: [{ data: Object.values(dailyUnits), tension: 0.3 }],
        },
        options: { indexAxis: 'x', plugins: { legend: { display: false } } },
      })
    );
  }

  renderBillsPerDay() {
    const dailyBills: any = {};
    this.bills.forEach((b) => {
      const d = new Date(b.generatedAt).toISOString().split('T')[0];
      dailyBills[d] = (dailyBills[d] || 0) + 1;
    });

    const ctx = document.getElementById('billsPerDayChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: Object.keys(dailyBills),
          datasets: [{ data: Object.values(dailyBills) }],
        },
        options: { indexAxis: 'x', plugins: { legend: { display: false } } },
      })
    );
  }

  renderAvgUnitsPerDay() {
    const totals: any = {};
    const counts: any = {};

    this.bills.forEach((b) => {
      const d = new Date(b.generatedAt).toISOString().split('T')[0];
      totals[d] = (totals[d] || 0) + Number(b.unitsConsumed);
      counts[d] = (counts[d] || 0) + 1;
    });

    const averages = Object.keys(totals).reduce((a: any, d) => {
      a[d] = Math.round(totals[d] / counts[d]);
      return a;
    }, {});

    const ctx = document.getElementById('avgUnitsChart') as HTMLCanvasElement;
    if (!ctx) return;

    this.charts.push(
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: Object.keys(averages),
          datasets: [{ data: Object.values(averages), tension: 0.3 }],
        },
        options: { indexAxis: 'x', plugins: { legend: { display: false } } },
      })
    );
  }
}
