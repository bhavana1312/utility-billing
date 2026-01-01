import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageUtilities } from './manage-utilities';

describe('ManageUtilities', () => {
  let component: ManageUtilities;
  let fixture: ComponentFixture<ManageUtilities>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageUtilities]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageUtilities);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
