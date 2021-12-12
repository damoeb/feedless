import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PageInspectionComponent } from './page-inspection.component';
import { PageInspectionModule } from './page-inspection.module';

describe('BucketCreateComponent', () => {
  let component: PageInspectionComponent;
  let fixture: ComponentFixture<PageInspectionComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [PageInspectionModule],
      }).compileComponents();

      fixture = TestBed.createComponent(PageInspectionComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
