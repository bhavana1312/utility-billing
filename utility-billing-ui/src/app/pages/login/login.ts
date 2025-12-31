import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth';
// import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  loading = false;
  apiError = '';
  success = '';

  form;

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  submit() {
    this.apiError = '';
    this.success = '';

    if (this.form.invalid) return;

    this.loading = true;

    this.auth.login(this.form.value).subscribe({
      next: () => {
        this.loading = false;
        this.success = 'Login successful. Redirecting...';
        // this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.apiError = err?.error?.message || 'Invalid username or password';
        // this.cdr.detectChanges();
      },
    });
  }
}
