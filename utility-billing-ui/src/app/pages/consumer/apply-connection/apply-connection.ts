import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/auth/auth';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './apply-connection.html',
  styleUrl: './apply-connection.css',
})
export class ApplyConnection {
  form = {
    utilityType: 'ELECTRICITY',
    tariffPlan: 'DOMESTIC',
  };

  applications: any[] = [];
  filteredApplications: any[] = [];
  utilityFilter = '';
  loading = false;

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadApplications();
  }

  apply() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) {
      this.toast.warning('Consumer not found');
      return;
    }

    this.loading = true;
    const payload = {
      consumerId,
      utilityType: this.form.utilityType,
      tariffPlan: this.form.tariffPlan,
    };

    this.http.post('http://localhost:9090/meters/connection-requests', payload).subscribe({
      next: () => {
        this.toast.success('Connection request submitted');
        this.loading = false;
        this.loadApplications();
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Failed to submit request');
        this.loading = false;
      },
    });
  }

  loadApplications() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http.get<any[]>('http://localhost:9090/meters/connection-requests').subscribe({
      next: (res) => {
        this.applications = res.filter((r) => r.consumerId === consumerId);
        this.applyFilter();
      },
      error: () => {
        this.toast.error('Failed to load applications');
      },
    });
  }

  applyFilter() {
    if (!this.utilityFilter) {
      this.filteredApplications = [...this.applications];
    } else {
      this.filteredApplications = this.applications.filter(
        (a) => a.utilityType === this.utilityFilter
      );
    }
  }
}
