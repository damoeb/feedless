import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IconComponent } from './icon.component';
import { AppTestModule } from '@feedless/testing';

describe('BubbleComponent', () => {
  let component: IconComponent;
  let fixture: ComponentFixture<IconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IconComponent, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(IconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.innerHTML.trim()).toEqual(
      `<span class="bubble-wrapper"><span class="bubble blue"></span></span>`,
    );
  });
});
