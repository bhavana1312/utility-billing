import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { forkJoin } from 'rxjs';

type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

interface ConsumerRequest {
  id: string;
  fullName: string;
  email: string;
  addressLine1: string;
  city: string;
  status: RequestStatus;
  createdAt: string;
  rejectionReason?: string;
}

interface ConnectionRequest {
  id: string;
  consumerId: string;
  utilityType: string;
  tariffPlan: string;
  status: RequestStatus;
  createdAt: string;
  rejectionReason?: string;
}

interface Consumer {
  id: string;
  fullName: string;
  email: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-requests.html',
  styleUrl: './manage-requests.css',
})
export class ManageRequests {
  selectedStatus: 'ALL' | RequestStatus = 'ALL';

  allConsumerRequests: ConsumerRequest[] = [];
  filteredConsumerRequests: ConsumerRequest[] = [];
  pagedConsumerRequests: ConsumerRequest[] = [];

  connectionRequests: ConnectionRequest[] = [];
  filteredConnectionRequests: ConnectionRequest[] = [];
  pagedConnectionRequests: ConnectionRequest[] = [];

  consumerMap: Record<string, Consumer> = {};

  showRejectModal = false;
  rejectReason = '';
  rejectType: 'CONSUMER' | 'CONNECTION' | null = null;
  rejectId: string | null = null;

  loadingMap: Record<string, boolean> = {};

  consumerPage = 0;
  consumerSize = 6;
  consumerTotalPages = 0;

  connectionPage = 0;
  connectionSize = 6;
  connectionTotalPages = 0;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadConsumerRequests();
    this.loadConnectionRequests();
  }

  loadConsumerRequests() {
    const statusParam = this.selectedStatus === 'ALL' ? '' : `&status=${this.selectedStatus}`;
    this.http
      .get<any>(`http://localhost:9090/consumer-requests?page=0&size=100${statusParam}`)
      .subscribe({
        next: (res) => {
          this.allConsumerRequests = this.sortRequests(res.content);
          this.applyConsumerFilters(true);
        },
        error: () => this.toast.error('Failed to load consumer requests'),
      });
  }

  applyConsumerFilters(resetPage = false) {
    if (resetPage) this.consumerPage = 0;
    this.filteredConsumerRequests = this.allConsumerRequests;
    this.consumerTotalPages = Math.ceil(this.filteredConsumerRequests.length / this.consumerSize);
    this.updateConsumerPage();
  }

  updateConsumerPage() {
    const start = this.consumerPage * this.consumerSize;
    const end = start + this.consumerSize;
    this.pagedConsumerRequests = this.filteredConsumerRequests.slice(start, end);
  }

  loadConnectionRequests() {
    this.http
      .get<ConnectionRequest[]>('http://localhost:9090/meters/connection-requests')
      .subscribe({
        next: (res) => {
          this.connectionRequests = res;
          this.applyConnectionFilters();
          this.loadConsumersForConnections(res);
        },
        error: () => this.toast.error('Failed to load connection requests'),
      });
  }

  loadConsumersForConnections(requests: ConnectionRequest[]) {
    const ids = [...new Set(requests.map((r) => r.consumerId))];
    const missing = ids.filter((id) => !this.consumerMap[id]);
    if (!missing.length) return;
    forkJoin(
      missing.map((id) => this.http.get<Consumer>(`http://localhost:9090/consumers/${id}`))
    ).subscribe({
      next: (res) => {
        const map = { ...this.consumerMap };
        res.forEach((c) => (map[c.id] = c));
        this.consumerMap = map;
      },
      error: () => this.toast.error('Failed to load consumer details'),
    });
  }

  onStatusChange() {
    this.consumerPage = 0;
    this.connectionPage = 0;
    this.loadConsumerRequests();
    this.applyConnectionFilters();
  }

  applyConnectionFilters() {
    const list =
      this.selectedStatus === 'ALL'
        ? this.connectionRequests
        : this.connectionRequests.filter((r) => r.status === this.selectedStatus);
    this.filteredConnectionRequests = this.sortRequests(list);
    this.connectionTotalPages = Math.ceil(
      this.filteredConnectionRequests.length / this.connectionSize
    );
    this.updateConnectionPage();
  }

  updateConnectionPage() {
    const start = this.connectionPage * this.connectionSize;
    const end = start + this.connectionSize;
    this.pagedConnectionRequests = this.filteredConnectionRequests.slice(start, end);
  }

  sortRequests<T extends { status: RequestStatus; createdAt: string }>(list: T[]): T[] {
    const order = { PENDING: 0, APPROVED: 1, REJECTED: 2 };
    return [...list].sort((a, b) => {
      const s = order[a.status] - order[b.status];
      if (s !== 0) return s;
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    });
  }

  nextConsumerPage() {
    if (this.consumerPage < this.consumerTotalPages - 1) {
      this.consumerPage++;
      this.updateConsumerPage();
    }
  }

  prevConsumerPage() {
    if (this.consumerPage > 0) {
      this.consumerPage--;
      this.updateConsumerPage();
    }
  }

  goToConsumerPage(p: number) {
    if (p >= 0 && p < this.consumerTotalPages) {
      this.consumerPage = p;
      this.updateConsumerPage();
    }
  }

  nextConnectionPage() {
    if (this.connectionPage < this.connectionTotalPages - 1) {
      this.connectionPage++;
      this.updateConnectionPage();
    }
  }

  prevConnectionPage() {
    if (this.connectionPage > 0) {
      this.connectionPage--;
      this.updateConnectionPage();
    }
  }

  goToConnectionPage(p: number) {
    if (p >= 0 && p < this.connectionTotalPages) {
      this.connectionPage = p;
      this.updateConnectionPage();
    }
  }

  approveConsumer(id: string) {
    this.loadingMap[id] = true;
    this.http.post(`http://localhost:9090/consumers/approve/${id}`, {}).subscribe({
      next: () => {
        this.loadingMap[id] = false;
        this.toast.success('Consumer approved');
        this.loadConsumerRequests();
      },
      error: () => {
        this.loadingMap[id] = false;
        this.toast.error('Approval failed');
      },
    });
  }

  approveConnection(id: string) {
    this.loadingMap[id] = true;
    this.http.post(`http://localhost:9090/meters/connection-requests/${id}/approve`, {}).subscribe({
      next: () => {
        this.loadingMap[id] = false;
        this.toast.success('Connection approved');
        this.loadConnectionRequests();
      },
      error: () => {
        this.loadingMap[id] = false;
        this.toast.error('Approval failed');
      },
    });
  }

  openRejectModal(id: string, type: 'CONSUMER' | 'CONNECTION') {
    this.rejectId = id;
    this.rejectType = type;
    this.rejectReason = '';
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectId = null;
    this.rejectType = null;
    this.rejectReason = '';
  }

  confirmReject() {
    if (!this.rejectReason.trim() || !this.rejectId || !this.rejectType) {
      this.toast.warning('Please enter rejection reason');
      return;
    }
    this.loadingMap[this.rejectId] = true;
    const req =
      this.rejectType === 'CONSUMER'
        ? this.http.put(`http://localhost:9090/consumer-requests/${this.rejectId}/reject`, {
            reason: this.rejectReason,
          })
        : this.http.post(
            `http://localhost:9090/meters/connection-requests/${this.rejectId}/reject`,
            { reason: this.rejectReason }
          );
    req.subscribe({
      next: () => {
        this.loadingMap[this.rejectId!] = false;
        this.toast.success('Request rejected');
        this.closeRejectModal();
        this.loadConsumerRequests();
        this.loadConnectionRequests();
      },
      error: () => {
        this.loadingMap[this.rejectId!] = false;
        this.toast.error('Rejection failed');
      },
    });
  }
}
