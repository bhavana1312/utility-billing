import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { AccountsOfficerSidebar } from '../accounts-officer-sidebar/accounts-officer-sidebar';

Chart.register(...registerables);

type PaymentMode = 'ONLINE' | 'CASH' | 'CHEQUE';
type PaymentStatus = 'SUCCESS' | 'FAILED' | 'INITIATED';

interface Payment {
  mode: PaymentMode;
  amount: number;
  status: PaymentStatus;
  email: string;
  completedAt: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, AccountsOfficerSidebar],
  templateUrl: './accounts-officer-dashboard.html',
  styleUrl: './accounts-officer-dashboard.css',
})
export class AccountsOfficerDashboard implements AfterViewInit {
  @ViewChild('sidebar') sidebar: any;

  today = new Date();
  isSidebarCollapsed = false;

  payments: Payment[] = [];
  recentPayments: { consumer: string; mode: PaymentMode; amount: number }[] = [];

  totalCollected = 0;
  offlinePaymentsCount = 0;
  successCount = 0;
  failedCount = 0;

  private charts: Chart[] = [];

  constructor(private http: HttpClient) {
    this.loadPayments();
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderAllCharts(), 300);
  }

  /* ✅ Sidebar toggle preserved */
  onSidebarToggle(collapsed: boolean) {
    this.isSidebarCollapsed = collapsed;
  }

  /* ✅ Fetch large page for dashboard analytics */
  loadPayments() {
    this.http.get<any>('http://localhost:9090/payments?page=0&size=1000').subscribe((res) => {
      this.payments = res.content;
      this.computeStats();
      this.prepareRecent();
      this.renderAllCharts();
    });
  }

  computeStats() {
    this.totalCollected = this.payments
      .filter((p) => p.status === 'SUCCESS')
      .reduce((sum, p) => sum + p.amount, 0);

    this.offlinePaymentsCount = this.payments.filter(
      (p) => p.mode === 'CASH' || p.mode === 'CHEQUE'
    ).length;

    this.successCount = this.payments.filter((p) => p.status === 'SUCCESS').length;

    this.failedCount = this.payments.filter((p) => p.status === 'FAILED').length;
  }

  prepareRecent() {
    this.recentPayments = [...this.payments]
      .filter((p) => p.status === 'SUCCESS')
      .sort((a, b) => new Date(b.completedAt).getTime() - new Date(a.completedAt).getTime())
      .slice(0, 6)
      .map((p) => ({
        consumer: p.email,
        mode: p.mode,
        amount: p.amount,
      }));
  }

  renderAllCharts() {
    if (!this.payments.length) return;

    /* ✅ Prevent Chart.js duplication */
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    this.renderModeChart();
    this.renderTimelineChart();
    this.renderAmountByMode();
    this.renderStatusChart();
    this.renderTopConsumers();
  }

  renderModeChart() {
    const modeMap: Record<PaymentMode, number> = {
      ONLINE: 0,
      CASH: 0,
      CHEQUE: 0,
    };

    this.payments.forEach((p) => modeMap[p.mode]++);

    this.charts.push(
      new Chart('modeChart', {
        type: 'doughnut',
        data: {
          labels: Object.keys(modeMap),
          datasets: [{ data: Object.values(modeMap) }],
        },
        options: {
          plugins: { legend: { position: 'bottom' } },
        },
      })
    );
  }

  renderTimelineChart() {
    const dateMap: Record<string, number> = {};

    this.payments
      .filter((p) => p.status === 'SUCCESS')
      .forEach((p) => {
        const d = new Date(p.completedAt).toLocaleDateString();
        dateMap[d] = (dateMap[d] || 0) + p.amount;
      });

    this.charts.push(
      new Chart('timelineChart', {
        type: 'line',
        data: {
          labels: Object.keys(dateMap),
          datasets: [
            {
              data: Object.values(dateMap),
              tension: 0.4,
              fill: true,
            },
          ],
        },
      })
    );
  }

  renderAmountByMode() {
    const amountMap: Record<PaymentMode, number> = {
      ONLINE: 0,
      CASH: 0,
      CHEQUE: 0,
    };

    this.payments
      .filter((p) => p.status === 'SUCCESS')
      .forEach((p) => {
        amountMap[p.mode] += p.amount;
      });

    this.charts.push(
      new Chart('amountModeChart', {
        type: 'bar',
        data: {
          labels: Object.keys(amountMap),
          datasets: [{ data: Object.values(amountMap) }],
        },
      })
    );
  }

  renderStatusChart() {
    const statusMap = {
      SUCCESS: 0,
      FAILED: 0,
    };

    this.payments.forEach((p) => {
      if (p.status === 'SUCCESS' || p.status === 'FAILED') {
        statusMap[p.status]++;
      }
    });

    this.charts.push(
      new Chart('statusChart', {
        type: 'doughnut',
        data: {
          labels: Object.keys(statusMap),
          datasets: [{ data: Object.values(statusMap) }],
        },
      })
    );
  }

  renderTopConsumers() {
    const consumerMap: Record<string, number> = {};

    this.payments
      .filter((p) => p.status === 'SUCCESS')
      .forEach((p) => {
        consumerMap[p.email] = (consumerMap[p.email] || 0) + p.amount;
      });

    const entries = Object.entries(consumerMap)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5);

    this.charts.push(
      new Chart('topConsumersChart', {
        type: 'bar',
        data: {
          labels: entries.map((e) => e[0]),
          datasets: [{ data: entries.map((e) => e[1]) }],
        },
        options: { indexAxis: 'y' },
      })
    );
  }
}
