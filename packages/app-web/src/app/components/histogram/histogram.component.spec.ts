import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistogramComponent } from './histogram.component';
import { AppTestModule } from '../../app-test.module';

describe('HistogramComponent', () => {
  let component: HistogramComponent;
  let fixture: ComponentFixture<HistogramComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistogramComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(HistogramComponent);
    component = fixture.componentInstance;
    component.data = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
