import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplyConnection } from './apply-connection';

describe('ApplyConnection', () => {
  let component: ApplyConnection;
  let fixture: ComponentFixture<ApplyConnection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplyConnection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApplyConnection);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
