import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BubbleComponent } from './bubble.component';
import { AppTestModule } from '../../app-test.module';

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
    expect(compiled.innerHTML.trim()).toEqual(
      `<span class="bubble-wrapper"><span class="bubble blue"></span></span>`
    );
  });
});
