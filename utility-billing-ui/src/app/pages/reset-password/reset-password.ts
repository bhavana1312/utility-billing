import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword {
  form: any;
  loading = false;
  strength = 0;
  showPassword = false;
  resetToken = '';

  rules = {
    lowercase: false,
    uppercase: false,
    number: false,
    symbol: false,
    length: false,
  };

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private toast: ToastrService
  ) {
    this.resetToken = this.route.snapshot.queryParamMap.get('token') || '';

    this.form = this.fb.group({
      newPassword: ['', Validators.required],
      confirmPassword: ['', Validators.required],
    });
  }

  checkStrength() {
    const p = this.form.value.newPassword || '';
    this.rules.lowercase = /[a-z]/.test(p);
    this.rules.uppercase = /[A-Z]/.test(p);
    this.rules.number = /\d/.test(p);
    this.rules.symbol = /[^A-Za-z0-9]/.test(p);
    this.rules.length = p.length >= 8;
    this.strength = Object.values(this.rules).filter((v) => v).length;
  }

  passwordsMatch() {
    return this.form.value.newPassword === this.form.value.confirmPassword;
  }

  isValid() {
    return this.form.valid && this.strength === 5 && this.passwordsMatch() && !!this.resetToken;
  }

  submit() {
    if (!this.isValid()) {
      this.toast.error('Please meet all password requirements');
      return;
    }

    this.loading = true;

    this.http
      .post('http://localhost:9090/auth/reset-password', {
        resetToken: this.resetToken,
        newPassword: this.form.value.newPassword,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.toast.success('Password reset successful. Redirecting to login...');
          setTimeout(() => this.router.navigate(['/login']), 2000);
        },
        error: (err) => {
          this.loading = false;
          this.toast.error(err?.error?.message || 'Failed to reset password');
        },
      });
  }
}
