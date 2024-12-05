import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SegmentedOutputComponent } from './segmented-output.component';

describe('SegmentedOutputComponent', () => {
  let component: SegmentedOutputComponent;
  let fixture: ComponentFixture<SegmentedOutputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SegmentedOutputComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SegmentedOutputComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('fields', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
