import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';
import { AuthService } from '../../../core/auth/auth';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [CommonModule, ConsumerSidebar, FormsModule],
  templateUrl: './connections.html',
  styleUrl: './connections.css',
})
export class Connections {
  @ViewChild(ConsumerSidebar) sidebar!: ConsumerSidebar;

  isSidebarCollapsed = false;
  today = new Date();

  meters: any[] = [];
  search = '';

  constructor(private http: HttpClient, private auth: AuthService) {
    this.loadMeters();
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
  }

  loadMeters() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http
      .get<any[]>(`http://localhost:9090/meters/consumer/${consumerId}`)
      .subscribe((res) => (this.meters = res));
  }

  get filteredMeters() {
    if (!this.search) return this.meters;

    const s = this.search.toLowerCase();
    return this.meters.filter(
      (m) =>
        m.meterNumber?.toLowerCase().includes(s) ||
        m.utilityType?.toLowerCase().includes(s) ||
        m.tariffPlan?.toLowerCase().includes(s)
    );
  }
}
