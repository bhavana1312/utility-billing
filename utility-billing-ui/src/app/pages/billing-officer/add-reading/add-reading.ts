import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-reading.html',
  styleUrl: './add-reading.css',
})
export class AddReading {
  meters: any[] = [];
  selectedMeter: any = null;
  readingValue: number | null = null;
  loading = false;

  constructor(private http: HttpClient, private toast: ToastrService) {
    this.loadMeters();
  }

  loadMeters() {
    this.http.get<any[]>('http://localhost:9090/meters/all').subscribe({
      next: (res) => {
        const activeMeters = res.filter((m) => m.active);

        activeMeters.forEach((meter) => {
          this.getConsumer(meter.consumerId).subscribe({
            next: (consumer) => {
              meter.consumerName = consumer.fullName;
              meter.email = consumer.email;
            },
            error: () => {
              meter.consumerName = 'Unknown';
              meter.email = '-';
            },
          });
        });

        this.meters = activeMeters;
      },
      error: () => this.toast.error('Failed to load connections'),
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
          this.toast.success('Reading added successfully');
          this.loading = false;
          this.generateBill();
        },
        error: () => {
          this.loading = false;
          this.toast.error('Failed to add reading');
        },
      });
  }

  getConsumer(id: string) {
    return this.http.get<any>(`http://localhost:9090/consumers/${id}`);
  }

  generateBill() {
    this.http
      .post('http://localhost:9090/billing/generate', {
        meterNumber: this.selectedMeter.meterNumber,
      })
      .subscribe({
        next: () => {
          this.toast.success('Bill generated');
          this.loadMeters();
          this.selectedMeter = null;
          this.readingValue = null;
        },
        error: () => this.toast.error('Bill generation failed'),
      });
  }
  closePanel() {
    this.selectedMeter = null;
    this.readingValue = null;
  }
}
