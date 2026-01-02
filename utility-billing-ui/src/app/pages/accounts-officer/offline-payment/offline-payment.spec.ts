import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OfflinePayment } from './offline-payment';

describe('OfflinePayment', () => {
  let component: OfflinePayment;
  let fixture: ComponentFixture<OfflinePayment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OfflinePayment]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OfflinePayment);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
