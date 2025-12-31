import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-consumer-request',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './consumer-request.html',
  styleUrl: './consumer-request.css',
})
export class ConsumerRequest {
  loading = false;

  form;

  constructor(private fb: FormBuilder, private http: HttpClient, private toast: ToastrService) {
    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[6-9][0-9]{9}$')]],
      addressLine1: ['', [Validators.required, Validators.minLength(5)]],
      city: ['', Validators.required],
      state: ['', Validators.required],
      postalCode: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]],
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Please fix the errors in the form');
      return;
    }

    this.loading = true;

    this.http.post('http://localhost:9090/consumer-requests', this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.toast.success('Consumer request submitted successfully');
        this.form.reset();
      },
      error: (err) => {
        this.loading = false;
        this.toast.error(err?.error?.message || 'Failed to submit consumer request');
      },
    });
  }
}
