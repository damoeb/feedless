import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { HistogramComponent } from './histogram.component';
import { HistogramModule } from './histogram.module';
import { AppTestModule } from '../../app-test.module';
import { GqlHistogram } from '../../../generated/graphql';

describe('HistogramComponent', () => {
  let component: HistogramComponent;
  let fixture: ComponentFixture<HistogramComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [HistogramModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(HistogramComponent);
    component = fixture.componentInstance;
    component.data = { items: [] } as GqlHistogram;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
