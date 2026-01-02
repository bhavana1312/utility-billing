import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountsOfficerSidebar } from './accounts-officer-sidebar';

describe('AccountsOfficerSidebar', () => {
  let component: AccountsOfficerSidebar;
  let fixture: ComponentFixture<AccountsOfficerSidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsOfficerSidebar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountsOfficerSidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
