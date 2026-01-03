import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsumerDashboard } from './consumer-dashboard';

describe('ConsumerDashboard', () => {
  let component: ConsumerDashboard;
  let fixture: ComponentFixture<ConsumerDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsumerDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsumerDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
