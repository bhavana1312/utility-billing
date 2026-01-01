import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageConsumers } from './manage-consumers';

describe('ManageConsumers', () => {
  let component: ManageConsumers;
  let fixture: ComponentFixture<ManageConsumers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageConsumers]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageConsumers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
