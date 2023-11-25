import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketsModalComponent } from './buckets-modal.component';

describe('BucketCreateModalComponent', () => {
  let component: BucketsModalComponent;
  let fixture: ComponentFixture<BucketsModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [BucketsModalComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BucketsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
