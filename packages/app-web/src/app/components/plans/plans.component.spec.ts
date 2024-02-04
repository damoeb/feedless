import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PlansComponent } from './plans.component';
import { PlansModule } from './plans.module';

describe('PlansComponent', () => {
  let component: PlansComponent;
  let fixture: ComponentFixture<PlansComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [PlansModule],
    }).compileComponents();

    fixture = TestBed.createComponent(PlansComponent);
    component = fixture.componentInstance;
    component.plans = [];
    component.featureGroups = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});