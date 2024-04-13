import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SegmentedOutputComponent } from './segmented-output.component';
import { SegmentedOutputModule } from './segmented-output.module';

describe('SegmentedOutputComponent', () => {
  let component: SegmentedOutputComponent;
  let fixture: ComponentFixture<SegmentedOutputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SegmentedOutputModule],
    }).compileComponents();

    fixture = TestBed.createComponent(SegmentedOutputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
