import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddReading } from './add-reading';

describe('AddReading', () => {
  let component: AddReading;
  let fixture: ComponentFixture<AddReading>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddReading]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddReading);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
