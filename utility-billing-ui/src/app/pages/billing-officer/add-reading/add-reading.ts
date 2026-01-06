import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { forkJoin } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-reading.html',
  styleUrl: './add-reading.css',
})
export class AddReading {
  meters: any[] = [];
  filteredMeters: any[] = [];
  selectedMeter: any = null;
  readingValue: number | null = null;
  loading = false;

  search = '';
  utilityFilter = '';

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadMeters();
  }

  loadMeters() {
    this.http.get<any[]>('http://localhost:9090/meters/all').subscribe({
      next: (res) => {
        const activeMeters = res.filter((m) => m.active);
        const requests = activeMeters.map((meter) =>
          this.http.get<any>(`http://localhost:9090/consumers/${meter.consumerId}`)
        );

        if (requests.length === 0) {
          this.meters = [];
          this.applyFilters();
          return;
        }

        forkJoin(requests).subscribe({
          next: (consumers) => {
            this.meters = activeMeters.map((meter, i) => ({
              ...meter,
              consumerName: consumers[i].fullName,
              email: consumers[i].email,
            }));
            this.applyFilters();
          },
          error: () => {
            this.meters = activeMeters.map((m) => ({
              ...m,
              consumerName: 'Unknown',
              email: '-',
            }));
            this.applyFilters();
          },
        });
      },
      error: () => this.toast.error('Failed to load connections'),
    });
  }

  applyFilters() {
    this.filteredMeters = this.meters.filter((m) => {
      const matchesSearch =
        !this.search ||
        m.consumerName?.toLowerCase().includes(this.search.toLowerCase()) ||
        m.meterNumber?.toLowerCase().includes(this.search.toLowerCase());

      const matchesUtility = !this.utilityFilter || m.utilityType === this.utilityFilter;

      return matchesSearch && matchesUtility;
    });
  }

  selectMeter(m: any) {
    this.selectedMeter = m;
    this.readingValue = null;
  }

  submitReading() {
    if (!this.selectedMeter || this.readingValue === null) {
      this.toast.warning('Select meter and enter reading');
      return;
    }

    this.loading = true;

    this.http
      .post('http://localhost:9090/meters/readings', {
        meterNumber: this.selectedMeter.meterNumber,
        readingValue: this.readingValue,
      })
      .subscribe({
        next: () => {
          this.generateBill();
        },
        error: (e) => {
          this.loading = false;
          this.toast.error(e?.error?.message || 'Failed to add reading');
        },
      });
  }

  private generateBill() {
    this.http
      .post('http://localhost:9090/billing/generate', {
        meterNumber: this.selectedMeter.meterNumber,
      })
      .subscribe({
        next: () => {
          this.toast.success('Reading added & bill generated');
          this.loading = false;
          this.loadMeters();
          this.closePanel();
        },
        error: (e) => {
          this.loading = false;
          this.toast.error(e?.error?.message || 'Reading saved but bill generation failed');
        },
      });
  }

  closePanel() {
    this.selectedMeter = null;
    this.readingValue = null;
  }
}
