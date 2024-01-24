import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DarkModeButtonComponent } from './dark-mode-button.component';
import { DarkModeButtonModule } from './dark-mode-button.module';

describe('BubbleComponent', () => {
  let component: DarkModeButtonComponent;
  let fixture: ComponentFixture<DarkModeButtonComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [DarkModeButtonModule]
    }).compileComponents();

    fixture = TestBed.createComponent(DarkModeButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
