export type UserRole =
  | 'ROLE_ADMIN'
  | 'ROLE_BILLING_OFFICER'
  | 'ROLE_ACCOUNTS_OFFICER'
  | 'ROLE_USER';

export const SIDEBAR_CONFIG: Record<
  UserRole,
  {
    brandIcon: string;
    brandText: string;
    brandAccent: string;
    userLabel: string;
    menu: {
      label: string;
      icon: string;
      route: string;
      exact?: boolean;
      badge?: boolean;
    }[];
  }
> = {
  ROLE_ADMIN: {
    brandIcon: '⚡',
    brandText: 'Utility',
    brandAccent: 'Admin',
    userLabel: 'Admin User',
    menu: [
      { label: 'Dashboard', icon: '🏠', route: '/admin', exact: true },
      { label: 'Manage Utilities', icon: '⚙️', route: '/admin/utilities' },
      { label: 'Requests', icon: '📩', route: '/admin/requests', badge: true },
      { label: 'Consumers', icon: '👥', route: '/admin/consumers' },
    ],
  },
  ROLE_BILLING_OFFICER: {
    brandIcon: '💳',
    brandText: 'Utility',
    brandAccent: 'Billing',
    userLabel: 'Billing Officer',
    menu: [
      { label: 'Dashboard', icon: '🏠', route: '/billing', exact: true },
      { label: 'Meter Readings', icon: '🧮', route: '/billing/add-reading' },
      { label: 'Bills', icon: '🧾', route: '/billing/bills' },
    ],
  },
  ROLE_ACCOUNTS_OFFICER: {
    brandIcon: '💼',
    brandText: 'Accounts',
    brandAccent: 'Officer',
    userLabel: 'Accounts Officer',
    menu: [
      { label: 'Dashboard', icon: '📊', route: '/accounts', exact: true },
      { label: 'Offline Payment', icon: '💵', route: '/accounts/offline-payment' },
      { label: 'Payments', icon: '🧾', route: '/accounts/payments' },
    ],
  },
  ROLE_USER: {
    brandIcon: '⚡',
    brandText: 'Utility',
    brandAccent: 'Consumer',
    userLabel: 'Consumer',
    menu: [
      { label: 'Dashboard', icon: '🏠', route: '/consumer', exact: true },
      { label: 'My Connections', icon: '🔌', route: '/consumer/connections' },
      { label: 'Apply Connection', icon: '📝', route: '/consumer/apply-connection' },
      { label: 'Bills', icon: '📄', route: '/consumer/bills' },
      { label: 'Payment History', icon: '💳', route: '/consumer/payments' },
      { label: 'Profile', icon: '👤', route: '/consumer/profile' },
    ],
  },
};
