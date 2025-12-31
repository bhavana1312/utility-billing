import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TariffDetails } from './tariff-details';

describe('TariffDetails', () => {
  let component: TariffDetails;
  let fixture: ComponentFixture<TariffDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TariffDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TariffDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
