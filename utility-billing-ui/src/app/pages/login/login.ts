import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { AuthService } from '../../core/auth/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  loading = false;

  form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly toastr: ToastrService
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  submit() {
    if (this.form.invalid) {
      this.toastr.warning('Please fill in all required fields');
      return;
    }

    this.loading = true;
    this.toastr.info('Logging in...');

    this.auth.login(this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.toastr.success('Login successful');

        const role = this.auth.getUserRole();
        this.router.navigate(role === 'ADMIN' ? ['/admin'] : ['/consumer']);
      },
      error: (err) => {
        this.loading = false;
        this.toastr.error(err?.error?.message || 'Invalid credentials');
      },
    });
  }
}
