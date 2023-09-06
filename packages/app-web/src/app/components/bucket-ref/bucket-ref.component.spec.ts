import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketRefComponent } from './bucket-ref.component';
import { BucketRefModule } from './bucket-ref.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { BasicBucket } from '../../graphql/types';

describe('BucketRefComponent', () => {
  let component: BucketRefComponent;
  let fixture: ComponentFixture<BucketRefComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        BucketRefModule,
        AppTestModule.withDefaults(),
        RouterTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BucketRefComponent);
    component = fixture.componentInstance;
    component.bucket = {
      histogram: {
        items: [],
      },
    } as BasicBucket;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
