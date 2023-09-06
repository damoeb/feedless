import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketCreateModalComponent } from './bucket-create-modal.component';
import { BucketCreateModalModule } from './bucket-create-modal.module';

describe('BucketCreateModalComponent', () => {
  let component: BucketCreateModalComponent;
  let fixture: ComponentFixture<BucketCreateModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [BucketCreateModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BucketCreateModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
