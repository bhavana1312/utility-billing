import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';

@Component({
  standalone: true,
  imports: [CommonModule, ConfirmDialog],
  templateUrl: './manage-consumers.html',
  styleUrl: './manage-consumers.css',
})
export class ManageConsumers {
  consumers: any[] = [];

  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  expandedConsumerId: string | null = null;
  connectionsMap: { [key: string]: any[] } = {};
  utilitiesMap: { [key: string]: string[] } = {};

  showConfirm = false;
  selectedMeterNumber = '';
  selectedConsumerId = '';

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadConsumers();
  }

  loadConsumers() {
    this.http
      .get<any>(`http://localhost:9090/consumers?page=${this.page}&size=${this.size}`)
      .subscribe({
        next: (res) => {
          this.consumers = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
        },
        error: () => this.toast.error('Failed to load consumers'),
      });
  }

  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadConsumers();
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadConsumers();
    }
  }

  goToPage(p: number) {
    if (p >= 0 && p < this.totalPages) {
      this.page = p;
      this.loadConsumers();
    }
  }

  toggleConnections(consumerId: string) {
    if (this.expandedConsumerId === consumerId) {
      this.expandedConsumerId = null;
      return;
    }

    this.expandedConsumerId = consumerId;

    if (this.connectionsMap[consumerId]) return;

    this.http.get<any[]>(`http://localhost:9090/meters/consumer/${consumerId}`).subscribe({
      next: (res) => {
        const active = res.filter((m) => m.active === true);
        this.connectionsMap[consumerId] = active;
        this.utilitiesMap[consumerId] = [...new Set(active.map((m) => m.utilityType))];
      },
      error: () => this.toast.error('Failed to load active connections'),
    });
  }

  deactivateMeter(meterNumber: string, consumerId: string) {
    this.http.delete(`http://localhost:9090/meters/${meterNumber}`).subscribe({
      next: () => {
        this.connectionsMap[consumerId] = this.connectionsMap[consumerId].filter(
          (m) => m.meterNumber !== meterNumber
        );

        this.utilitiesMap[consumerId] = [
          ...new Set(this.connectionsMap[consumerId].map((m) => m.utilityType)),
        ];

        this.toast.success('Meter deactivated');
      },
      error: () => this.toast.error('Failed to deactivate meter'),
    });
  }

  openDeactivate(meterNumber: string, consumerId: string) {
    this.selectedMeterNumber = meterNumber;
    this.selectedConsumerId = consumerId;
    this.showConfirm = true;
  }

  confirmDeactivate() {
    this.deactivateMeter(this.selectedMeterNumber, this.selectedConsumerId);
    this.showConfirm = false;
  }

  cancelDeactivate() {
    this.showConfirm = false;
  }
}
