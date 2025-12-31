import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsumerRequest } from './consumer-request';

describe('ConsumerRequest', () => {
  let component: ConsumerRequest;
  let fixture: ComponentFixture<ConsumerRequest>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsumerRequest]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsumerRequest);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
