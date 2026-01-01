import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AdminSidebar } from '../admin-sidebar/admin-sidebar';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';

@Component({
  standalone: true,
  imports: [CommonModule, AdminSidebar, ConfirmDialog],
  templateUrl: './manage-consumers.html',
  styleUrl: './manage-consumers.css',
})
export class ManageConsumers {
  consumers: any[] = [];
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
    this.http.get<any[]>('http://localhost:9090/consumers').subscribe({
      next: (res) => (this.consumers = res),
      error: () => this.toast.error('Failed to load consumers'),
    });
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
