import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/auth/auth';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, ConsumerSidebar],
  templateUrl: './apply-connection.html',
  styleUrl: './apply-connection.css',
})
export class ApplyConnection {
  @ViewChild(ConsumerSidebar) sidebar!: ConsumerSidebar;

  isSidebarCollapsed = false;
  today = new Date();

  form = {
    utilityType: 'ELECTRICITY',
    tariffPlan: 'DOMESTIC',
  };

  applications: any[] = [];

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadApplications();
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
  }

  apply() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) {
      this.toast.warning('Consumer not found');
      return;
    }

    const payload = {
      consumerId,
      utilityType: this.form.utilityType,
      tariffPlan: this.form.tariffPlan,
    };

    this.http.post('http://localhost:9090/meters/connection-requests', payload).subscribe({
      next: () => {
        this.toast.success('Connection request submitted');
        this.loadApplications();
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Failed to submit request');
      },
    });
  }

  loadApplications() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any[]>('http://localhost:9090/meters/connection-requests').subscribe({
      next: (res) => {
        this.applications = res.filter((r) => r.consumerId === consumerId);
      },
      error: () => {
        this.toast.error('Failed to load applications');
      },
    });
  }
}
