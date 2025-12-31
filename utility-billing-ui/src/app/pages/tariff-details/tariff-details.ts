import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './tariff-details.html',
  styleUrl: './tariff-details.css',
})
export class TariffDetails {
  utility = '';
  plan = '';
  tariff: any;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private toast: ToastrService
  ) {
    this.utility = this.route.snapshot.paramMap.get('utility')!;
    this.plan = this.route.snapshot.paramMap.get('plan')!;
    this.fetchTariff();
  }

  fetchTariff() {
    this.http
      .get(`http://localhost:9090/utilities/tariffs/${this.utility}/plans/${this.plan}`)
      .subscribe({
        next: (res) => {
          this.tariff = res;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.toast.error('Failed to load tariff details');
        },
      });
  }
}
