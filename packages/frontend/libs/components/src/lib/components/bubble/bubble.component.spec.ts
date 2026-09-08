import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BubbleComponent } from './bubble.component';
import { AppTestModule } from '@feedless/testing';

describe('BubbleComponent', () => {
  let component: BubbleComponent;
  let fixture: ComponentFixture<BubbleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BubbleComponent, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BubbleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const wrapper = compiled.querySelector('span.bubble-wrapper');
    expect(wrapper).toBeTruthy();
    const bubble = wrapper?.querySelector('span.bubble');
    expect(bubble?.classList.contains('blue')).toBe(true);
  });
});
