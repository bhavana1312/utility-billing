import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BillingOfficerDashboard } from './billing-officer-dashboard';

describe('BillingOfficerDashboard', () => {
  let component: BillingOfficerDashboard;
  let fixture: ComponentFixture<BillingOfficerDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BillingOfficerDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BillingOfficerDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
