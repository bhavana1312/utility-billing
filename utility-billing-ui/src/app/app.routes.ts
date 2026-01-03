import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Tariffs } from './pages/tariffs/tariffs';
import { TariffDetails } from './pages/tariff-details/tariff-details';
import { ConsumerRequest } from './pages/consumer-request/consumer-request';
import { Login } from './pages/login/login';
import { AdminDashboard } from './pages/admin/admin-dashboard/admin-dashboard';
import { authGuard } from './core/guards/auth-guard';
import { ForgotPassword } from './pages/forgot-password/forgot-password';
import { ResetPassword } from './pages/reset-password/reset-password';
import { PublicLayout } from './layouts/public-layout/public-layout';
import { DashboardLayout } from './layouts/dashboard-layout/dashboard-layout';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', component: Home },
      { path: 'tariffs', component: Tariffs },
      { path: 'tariffs/:utility/:plan', component: TariffDetails },
      { path: 'consumer-request', component: ConsumerRequest },
      { path: 'login', component: Login },
      { path: 'forgot-password', component: ForgotPassword },
      { path: 'reset-password', component: ResetPassword },
    ],
  },
  {
    path: '',
    component: DashboardLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'admin',
        canActivate: [authGuard],
        children: [
          {
            path: '',
            component: AdminDashboard,
          },
          {
            path: 'utilities',
            loadComponent: () =>
              import('./pages/admin/manage-utilities/manage-utilities').then(
                (m) => m.ManageUtilities
              ),
          },
          {
            path: 'consumers',
            loadComponent: () =>
              import('./pages/admin/manage-consumers/manage-consumers').then(
                (m) => m.ManageConsumers
              ),
          },
          {
            path: 'requests',
            loadComponent: () =>
              import('./pages/admin/manage-requests/manage-requests').then((m) => m.ManageRequests),
          },
        ],
      },

      {
        path: 'billing',
        canActivate: [authGuard],
        children: [
          {
            path: '',
            loadComponent: () =>
              import(
                './pages/billing-officer/billing-officer-dashboard/billing-officer-dashboard'
              ).then((m) => m.BillingDashboard),
          },
          {
            path: 'add-reading',
            loadComponent: () =>
              import('./pages/billing-officer/add-reading/add-reading').then((m) => m.AddReading),
          },

          {
            path: 'bills',
            loadComponent: () => import('./pages/billing-officer/bills/bills').then((m) => m.Bills),
            canActivate: [authGuard],
          },
        ],
      },
      {
        path: 'accounts',
        children: [
          {
            path: '',
            loadComponent: () =>
              import(
                './pages/accounts-officer/accounts-officer-dashboard/accounts-officer-dashboard'
              ).then((m) => m.AccountsOfficerDashboard),
            canActivate: [authGuard],
          },
          {
            path: 'offline-payment',
            loadComponent: () =>
              import('./pages/accounts-officer/offline-payment/offline-payment').then(
                (m) => m.OfflinePayment
              ),
            canActivate: [authGuard],
          },
          {
            path: 'payments',
            loadComponent: () =>
              import('./pages/accounts-officer/payments/payments').then((m) => m.Payments),
            canActivate: [authGuard],
          },
        ],
      },

      {
        path: 'consumer',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./pages/consumer/consumer-dashboard/consumer-dashboard').then(
                (m) => m.ConsumerDashboard
              ),
            canActivate: [authGuard],
          },
          {
            path: 'connections',
            loadComponent: () =>
              import('./pages/consumer/connections/connections').then((m) => m.Connections),
            canActivate: [authGuard],
          },
          {
            path: 'apply-connection',
            loadComponent: () =>
              import('./pages/consumer/apply-connection/apply-connection').then(
                (m) => m.ApplyConnection
              ),
            canActivate: [authGuard],
          },
          {
            path: 'bills',
            loadComponent: () => import('./pages/consumer/bills/bills').then((m) => m.Bills),
            canActivate: [authGuard],
          },
          {
            path: 'payments',
            loadComponent: () =>
              import('./pages/consumer/payments/payments').then((m) => m.Payments),
            canActivate: [authGuard],
          },
          {
            path: 'profile',
            loadComponent: () => import('./pages/consumer/profile/profile').then((m) => m.Profile),
            canActivate: [authGuard],
          },
        ],
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
