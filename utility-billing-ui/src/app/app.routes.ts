import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { Home } from './pages/home/home';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: LoginComponent },
  // { path: 'consumer', component: ConsumerDashboardComponent, canActivate: [authGuard] },
  // { path: 'admin', component: AdminDashboardComponent, canActivate: [authGuard] },
];
