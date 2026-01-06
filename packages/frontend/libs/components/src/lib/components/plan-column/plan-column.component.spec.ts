import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanColumnComponent } from './plan-column.component';

describe('PlanColumnComponent', () => {
  let component: PlanColumnComponent;
  let fixture: ComponentFixture<PlanColumnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanColumnComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PlanColumnComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('featureGroups', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
