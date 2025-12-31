import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth';
import { Router } from '@angular/router';

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

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
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
        this.success = 'Login successful';

        const role = this.auth.getUserRole();
        setTimeout(() => {
          if (role === 'ADMIN') this.router.navigate(['/admin']);
          else this.router.navigate(['/consumer']);
        }, 600);
      },
      error: (err) => {
        this.loading = false;
        this.apiError = err?.error?.message || 'Invalid credentials';
      },
    });
  }
}
