import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Tariffs } from './pages/tariffs/tariffs';
import { TariffDetails } from './pages/tariff-details/tariff-details';
import { ConsumerRequest } from './pages/consumer-request/consumer-request';
import { LoginComponent } from './pages/login/login';
import { UserDashboard } from './pages/dashboards/user-dashboard/user-dashboard';
import { AdminDashboard } from './pages/admin/admin-dashboard/admin-dashboard';
import { AccountsDashboard } from './pages/dashboards/accounts-dashboard/accounts-dashboard';
import { authGuard } from './core/auth/auth-guard';

export const routes: Routes = [
  /* Public routes */
  { path: '', component: Home },
  { path: 'tariffs', component: Tariffs },
  { path: 'tariffs/:utility/:plan', component: TariffDetails },
  { path: 'consumer-request', component: ConsumerRequest },
  { path: 'login', component: LoginComponent },

  /* User dashboard */
  {
    path: 'user',
    component: UserDashboard,
    canActivate: [authGuard],
  },

  /* Accounts dashboard */
  {
    path: 'accounts',
    component: AccountsDashboard,
    canActivate: [authGuard],
  },

  /* Admin section */
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
          import('./pages/admin/manage-utilities/manage-utilities').then((m) => m.ManageUtilities),
      },
      {
        path: 'consumers',
        loadComponent: () =>
          import('./pages/admin/manage-consumers/manage-consumers').then((m) => m.ManageConsumers),
      },
      {
        path: 'requests',
        loadComponent: () =>
          import('./pages/admin/manage-requests/manage-requests').then((m) => m.ManageRequests),
      },
    ],
  },

  /* Billing officer section */
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
        path: 'generate',
        loadComponent: () =>
          import('./pages/billing-officer/generate-bill/generate-bill').then((m) => m.GenerateBill),
      },
    ],
  },

  /* Fallback */
  { path: '**', redirectTo: '' },
];
