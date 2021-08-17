import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketCreateComponent } from './bucket-create.component';
import { BucketCreateModule } from './bucket-create.module';

describe('BucketCreateComponent', () => {
  let component: BucketCreateComponent;
  let fixture: ComponentFixture<BucketCreateComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [BucketCreateModule],
      }).compileComponents();

      fixture = TestBed.createComponent(BucketCreateComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
