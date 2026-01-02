import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AdminSidebar } from '../admin-sidebar/admin-sidebar';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, AdminSidebar],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements AfterViewInit {
  @ViewChild(AdminSidebar) sidebar!: AdminSidebar;

  requests: any[] = [];
  consumers: any[] = [];
  meters: any[] = [];
  isSidebarCollapsed = false;
  today = new Date();

  selectedStatus: string = 'ALL';
  showRejectModal = false;
  rejectReason = '';
  rejectingRequestId: string | null = null;
  private charts: any[] = [];

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadRequests();
    this.loadConsumers();
    this.loadMeters();
  }

  onSidebarToggle(collapsed: boolean) {
    this.isSidebarCollapsed = collapsed;
    setTimeout(() => {
      this.charts.forEach((c) => {
        if (c) c.resize();
      });
    }, 310);
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
      error: () => this.toast.error('Failed to load requests'),
    });
  }

  loadMeters() {
    this.http.get<any[]>('http://localhost:9090/meters/all').subscribe({
      next: (res) => {
        this.meters = res;
        this.renderCharts();
      },
      error: () => this.toast.error('Failed to load meter count'),
    });
  }

  loadConsumers() {
    this.http.get<any[]>('http://localhost:9090/consumers').subscribe({
      next: (res) => {
        this.consumers = res;
        this.renderCharts();
      },
    });
  }

  renderCharts() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];

    const pending = this.requests.filter((r) => r.status === 'PENDING').length;
    const approved = this.requests.filter((r) => r.status === 'APPROVED').length;
    const rejected = this.requests.filter((r) => r.status === 'REJECTED').length;

    const statusCtx = document.getElementById('statusChart') as HTMLCanvasElement;
    if (statusCtx) {
      this.charts.push(
        new Chart(statusCtx, {
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
          options: { responsive: true, maintainAspectRatio: false },
        })
      );
    }

    const comparisonCtx = document.getElementById('comparisonChart') as HTMLCanvasElement;
    if (comparisonCtx) {
      this.charts.push(
        new Chart(comparisonCtx, {
          type: 'bar',
          data: {
            labels: ['Consumers', 'Connections', 'Pending'],
            datasets: [
              {
                data: [this.consumers.length, this.meters.length, pending],
                backgroundColor: ['#2563eb', '#22c55e', '#f97316'],
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
  }

  approve(id: string) {
    this.http.post(`http://localhost:9090/consumers/from-request/${id}`, {}).subscribe({
      next: () => {
        this.toast.success('Consumer approved');
        this.loadRequests();
        this.loadConsumers();
      },
    });
  }

  openRejectModal(id: string) {
    this.rejectingRequestId = id;
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectingRequestId = null;
    this.rejectReason = '';
  }

  confirmReject() {
    if (!this.rejectReason.trim()) return;
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
      });
  }

  get filteredRequests() {
    let list =
      this.selectedStatus === 'ALL'
        ? this.requests
        : this.requests.filter((r) => r.status === this.selectedStatus);
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  get pendingRequestsCount() {
    return this.requests.filter((r) => r.status === 'PENDING').length;
  }
}
