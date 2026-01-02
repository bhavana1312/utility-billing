import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountsOfficerDashboard } from './accounts-officer-dashboard';

describe('AccountsOfficerDashboard', () => {
  let component: AccountsOfficerDashboard;
  let fixture: ComponentFixture<AccountsOfficerDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsOfficerDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountsOfficerDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
