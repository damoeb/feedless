import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BubbleComponent } from './bubble.component';
import { BubbleModule } from './bubble.module';
import { AppTestModule } from '../../app-test.module';

describe('BubbleComponent', () => {
  let component: BubbleComponent;
  let fixture: ComponentFixture<BubbleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [BubbleModule, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BubbleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
