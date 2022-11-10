import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BubbleComponent } from './bubble.component';
import { BubbleModule } from './bubble.module';

describe('NotificationBubbleComponent', () => {
  let component: BubbleComponent;
  let fixture: ComponentFixture<BubbleComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [BubbleModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BubbleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
