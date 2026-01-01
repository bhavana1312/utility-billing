import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenerateBill } from './generate-bill';

describe('GenerateBill', () => {
  let component: GenerateBill;
  let fixture: ComponentFixture<GenerateBill>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenerateBill]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenerateBill);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
