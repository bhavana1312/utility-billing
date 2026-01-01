import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AdminSidebar } from '../../../shared/admin-sidebar/admin-sidebar';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, AdminSidebar],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements AfterViewInit {
  requests: any[] = [];
  consumers: any[] = [];

  selectedStatus: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' = 'ALL';

  showRejectModal = false;
  rejectReason = '';
  rejectingRequestId: string | null = null;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadRequests();
    this.loadConsumers();
  }

  ngAfterViewInit() {
    setTimeout(() => this.renderCharts(), 500);
  }

  loadRequests() {
    this.http.get<any[]>('http://localhost:9090/consumer-requests').subscribe({
      next: (res) => {
        this.requests = res;
        this.renderCharts();
      },
      error: () => this.toast.error('Failed to load consumer requests'),
    });
  }

  loadConsumers() {
    this.http.get<any[]>('http://localhost:9090/consumers').subscribe({
      next: (res) => {
        this.consumers = res;
        this.renderCharts();
      },
      error: () => this.toast.error('Failed to load consumers'),
    });
  }

  renderCharts() {
    if (!this.requests.length) return;

    const pending = this.requests.filter((r) => r.status === 'PENDING').length;
    const approved = this.requests.filter((r) => r.status === 'APPROVED').length;
    const rejected = this.requests.filter((r) => r.status === 'REJECTED').length;

    new Chart('statusChart', {
      type: 'doughnut',
      data: {
        labels: ['Pending', 'Approved', 'Rejected'],
        datasets: [
          {
            data: [pending, approved, rejected],
            backgroundColor: ['#facc15', '#22c55e', '#ef4444'],
          },
        ],
      },
      options: {
        responsive: true,
        plugins: { legend: { position: 'bottom' } },
      },
    });

    new Chart('comparisonChart', {
      type: 'bar',
      data: {
        labels: ['Consumers', 'Pending Requests'],
        datasets: [
          {
            data: [this.consumers.length, pending],
            backgroundColor: ['#2563eb', '#f97316'],
          },
        ],
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
      },
    });

    const grouped = this.groupByDate(this.requests);

    new Chart('trendChart', {
      type: 'line',
      data: {
        labels: Object.keys(grouped),
        datasets: [
          {
            label: 'Requests',
            data: Object.values(grouped),
            borderColor: '#2563eb',
            tension: 0.4,
            fill: false,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
      },
    });
  }

  groupByDate(list: any[]) {
    const map: any = {};
    list.forEach((r) => {
      const d = new Date(r.createdAt).toLocaleDateString();
      map[d] = (map[d] || 0) + 1;
    });
    return map;
  }

  approve(id: string) {
    this.http.post(`http://localhost:9090/consumers/from-request/${id}`, {}).subscribe({
      next: () => {
        this.toast.success('Consumer approved');
        this.loadRequests();
        this.loadConsumers();
      },
      error: () => this.toast.error('Approval failed'),
    });
  }

  openRejectModal(id: string) {
    this.rejectingRequestId = id;
    this.rejectReason = '';
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectingRequestId = null;
    this.rejectReason = '';
  }

  confirmReject() {
    if (!this.rejectReason.trim() || !this.rejectingRequestId) {
      this.toast.warning('Please enter a rejection reason');
      return;
    }

    this.http
      .put(`http://localhost:9090/consumer-requests/${this.rejectingRequestId}/reject`, {
        reason: this.rejectReason,
      })
      .subscribe({
        next: () => {
          this.toast.success('Request rejected');
          this.loadRequests();
          this.closeRejectModal();
        },
        error: () => this.toast.error('Rejection failed'),
      });
  }

  get filteredRequests() {
    let list = [...this.requests];
    if (this.selectedStatus !== 'ALL') {
      list = list.filter((r) => r.status === this.selectedStatus);
    }
    return list.sort((a, b) => {
      if (a.status === 'PENDING' && b.status !== 'PENDING') return -1;
      if (a.status !== 'PENDING' && b.status === 'PENDING') return 1;
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    });
  }

  get pendingRequestsCount() {
    return this.requests.filter((r) => r.status === 'PENDING').length;
  }
}
