import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InspectionComponent } from './inspection.component';
import { InspectionModule } from './inspection.module';

describe('BucketCreateComponent', () => {
  let component: InspectionComponent;
  let fixture: ComponentFixture<InspectionComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [InspectionModule],
      }).compileComponents();

      fixture = TestBed.createComponent(InspectionComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
