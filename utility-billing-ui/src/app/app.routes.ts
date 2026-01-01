import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Tariffs } from './pages/tariffs/tariffs';
import { TariffDetails } from './pages/tariff-details/tariff-details';
import { ConsumerRequest } from './pages/consumer-request/consumer-request';
import { LoginComponent } from './pages/login/login';
import { UserDashboard } from './pages/dashboards/user-dashboard/user-dashboard';
import { AdminDashboard } from './pages/admin/admin-dashboard/admin-dashboard';
import { BillingDashboard } from './pages/dashboards/billing-dashboard/billing-dashboard';
import { AccountsDashboard } from './pages/dashboards/accounts-dashboard/accounts-dashboard';
import { authGuard } from './core/auth/auth-guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'tariffs', component: Tariffs },
  { path: 'tariffs/:utility/:plan', component: TariffDetails },
  { path: 'consumer-request', component: ConsumerRequest },
  { path: 'login', component: LoginComponent },

  { path: 'user', component: UserDashboard, canActivate: [authGuard] },
  { path: 'admin', component: AdminDashboard, canActivate: [authGuard] },
  { path: 'billing', component: BillingDashboard, canActivate: [authGuard] },
  { path: 'accounts', component: AccountsDashboard, canActivate: [authGuard] },
  {
    path: 'admin/utilities',
    loadComponent: () =>
      import('./pages/admin/manage-utilities/manage-utilities').then((m) => m.ManageUtilities),
    canActivate: [authGuard],
  },
  {
    path: 'admin/consumers',
    loadComponent: () =>
      import('./pages/admin/manage-consumers/manage-consumers').then((m) => m.ManageConsumers),
    canActivate: [authGuard],
  },
];
