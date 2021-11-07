import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PageInspectionComponent } from './bucket-create.component';
import { PageInspectionModule } from './bucket-create.module';

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
