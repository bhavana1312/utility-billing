import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountsDashboard } from './accounts-dashboard';

describe('AccountsDashboard', () => {
  let component: AccountsDashboard;
  let fixture: ComponentFixture<AccountsDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountsDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
