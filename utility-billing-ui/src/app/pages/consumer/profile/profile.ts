import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/auth/auth';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  today = new Date();

  consumer: any = null;
  showReset = false;
  strength = 0;

  showOld = false;
  showNew = false;
  showConfirm = false;

  password = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  };

  rules = {
    lowercase: false,
    uppercase: false,
    number: false,
    symbol: false,
    length: false,
  };

  constructor(private http: HttpClient, private auth: AuthService, private toast: ToastrService) {
    this.loadProfile();
  }

  loadProfile() {
    const id = this.auth.getConsumerId();
    if (!id) return;

    this.http.get<any>(`http://localhost:9090/consumers/${id}`).subscribe({
      next: (res) => (this.consumer = res),
      error: () => this.toast.error('Failed to load profile'),
    });
  }

  checkStrength() {
    const p = this.password.newPassword;
    this.rules.lowercase = /[a-z]/.test(p);
    this.rules.uppercase = /[A-Z]/.test(p);
    this.rules.number = /\d/.test(p);
    this.rules.symbol = /[^A-Za-z0-9]/.test(p);
    this.rules.length = p.length >= 8;
    this.strength = Object.values(this.rules).filter((v) => v).length;
  }

  isPasswordValid() {
    return this.strength === 5 && this.password.newPassword === this.password.confirmPassword;
  }

  changePassword() {
    if (!this.isPasswordValid()) {
      this.toast.error('Password does not meet requirements');
      return;
    }

    this.http
      .post('http://localhost:9090/auth/change-password', {
        username: this.consumer.fullName,
        oldPassword: this.password.oldPassword,
        newPassword: this.password.newPassword,
      })
      .subscribe({
        next: () => {
          this.toast.success('Password updated successfully');
          this.cancelReset();
        },
        error: (err) => {
          this.toast.error(err?.error || 'Password update failed');
        },
      });
  }

  cancelReset() {
    this.showReset = false;
    this.password = { oldPassword: '', newPassword: '', confirmPassword: '' };
    this.rules = {
      lowercase: false,
      uppercase: false,
      number: false,
      symbol: false,
      length: false,
    };
    this.showOld = false;
    this.showNew = false;
    this.showConfirm = false;
    this.strength = 0;
  }
}
