import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BillingSidebar } from './billing-sidebar';

describe('BillingSidebar', () => {
  let component: BillingSidebar;
  let fixture: ComponentFixture<BillingSidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BillingSidebar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BillingSidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
