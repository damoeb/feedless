import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketEditComponent } from './bucket-edit.component';
import { BucketEditModule } from './bucket-edit.module';
import { AppTestModule } from '../../app-test.module';

describe('BucketEditComponent', () => {
  let component: BucketEditComponent<any>;
  let fixture: ComponentFixture<BucketEditComponent<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [BucketEditModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(BucketEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
