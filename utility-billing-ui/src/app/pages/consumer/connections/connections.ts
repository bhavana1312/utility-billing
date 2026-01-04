import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './connections.html',
  styleUrl: './connections.css',
})
export class Connections {
  meters: any[] = [];
  search = '';
  utilityFilter = '';

  constructor(private http: HttpClient, private auth: AuthService) {
    this.loadMeters();
  }

  loadMeters() {
    const consumerId = this.auth.getConsumerId();
    if (!consumerId) return;

    this.http
      .get<any[]>(`http://localhost:9090/meters/consumer/${consumerId}`)
      .subscribe((res) => (this.meters = res));
  }

  get filteredMeters() {
    return this.meters.filter((m) => {
      const searchStr = this.search.toLowerCase();
      const matchesSearch =
        !this.search ||
        m.meterNumber?.toLowerCase().includes(searchStr) ||
        m.tariffPlan?.toLowerCase().includes(searchStr);

      const matchesUtility = !this.utilityFilter || m.utilityType === this.utilityFilter;

      return matchesSearch && matchesUtility;
    });
  }
}
