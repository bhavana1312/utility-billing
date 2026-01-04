import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { RouterModule } from '@angular/router';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  form: any;
  loading = false;
  success = false;

  constructor(private fb: FormBuilder, private http: HttpClient, private toast: ToastrService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  submit() {
    if (this.form.invalid) {
      this.toast.error('Please enter a valid email address');
      return;
    }

    this.loading = true;

    this.http
      .post('http://localhost:9090/auth/forgot-password', {
        email: this.form.value.email,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.toast.success('Password reset link sent to your email');
          this.form.reset();
        },
        error: (err) => {
          this.loading = false;
          this.toast.error(err?.error?.message || 'Failed to send reset link');
        },
      });
  }
}
