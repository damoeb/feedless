import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PlanColumnComponent } from './plan-column.component';
import { PlanColumnModule } from './plan-column.module';

describe('PlanColumnComponent', () => {
  let component: PlanColumnComponent;
  let fixture: ComponentFixture<PlanColumnComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [PlanColumnComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PlanColumnComponent);
    component = fixture.componentInstance;
    component.featureGroups = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
