import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsumerSidebar } from './consumer-sidebar';

describe('ConsumerSidebar', () => {
  let component: ConsumerSidebar;
  let fixture: ComponentFixture<ConsumerSidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsumerSidebar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsumerSidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
