import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AdminSidebar } from '../admin-sidebar/admin-sidebar';
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
  imports: [CommonModule, FormsModule, AdminSidebar],
  templateUrl: './manage-requests.html',
  styleUrl: './manage-requests.css',
})
export class ManageRequests {
  selectedStatus: 'ALL' | RequestStatus = 'ALL';

  consumerRequests: ConsumerRequest[] = [];
  connectionRequests: ConnectionRequest[] = [];
  filteredConnectionRequests: ConnectionRequest[] = [];

  consumerMap: Record<string, Consumer> = {};

  showRejectModal = false;
  rejectReason = '';
  rejectType: 'CONSUMER' | 'CONNECTION' | null = null;
  rejectId: string | null = null;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadConsumerRequests();
    this.loadConnectionRequests();
  }

  loadConsumerRequests() {
    const status = this.selectedStatus === 'ALL' ? '' : `?status=${this.selectedStatus}`;

    this.http.get<ConsumerRequest[]>(`http://localhost:9090/consumer-requests${status}`).subscribe({
      next: (res) => {
        this.consumerRequests = this.sortRequests(res);
      },
      error: () => this.toast.error('Failed to load consumer requests'),
    });
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
    const missingIds = ids.filter((id) => !this.consumerMap[id]);

    if (!missingIds.length) return;

    forkJoin(
      missingIds.map((id) => this.http.get<Consumer>(`http://localhost:9090/consumers/${id}`))
    ).subscribe({
      next: (res) => {
        res.forEach((c) => {
          this.consumerMap[c.id] = c;
        });
      },
      error: () => this.toast.error('Failed to load consumer details'),
    });
  }

  onStatusChange() {
    this.loadConsumerRequests();
    this.applyConnectionFilters();
  }

  applyConnectionFilters() {
    const filtered =
      this.selectedStatus === 'ALL'
        ? this.connectionRequests
        : this.connectionRequests.filter((r) => r.status === this.selectedStatus);

    this.filteredConnectionRequests = this.sortRequests(filtered);
  }

  sortRequests<T extends { status: RequestStatus; createdAt: string }>(list: T[]): T[] {
    const order: Record<RequestStatus, number> = {
      PENDING: 0,
      APPROVED: 1,
      REJECTED: 2,
    };

    return [...list].sort((a, b) => {
      const statusDiff = order[a.status] - order[b.status];
      if (statusDiff !== 0) return statusDiff;
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    });
  }

  approveConsumer(id: string) {
    this.http.post(`http://localhost:9090/consumers/from-request/${id}`, {}).subscribe({
      next: () => {
        this.toast.success('Consumer approved');
        this.loadConsumerRequests();
      },
      error: () => this.toast.error('Approval failed'),
    });
  }

  approveConnection(id: string) {
    this.http.post(`http://localhost:9090/meters/connection-requests/${id}/approve`, {}).subscribe({
      next: () => {
        this.toast.success('Connection approved');
        this.loadConnectionRequests();
      },
      error: () => this.toast.error('Approval failed'),
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
        this.toast.success('Request rejected');
        this.closeRejectModal();
        this.loadConsumerRequests();
        this.loadConnectionRequests();
      },
      error: () => this.toast.error('Rejection failed'),
    });
  }
}
