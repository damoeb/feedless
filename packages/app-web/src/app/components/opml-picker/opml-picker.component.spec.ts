import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { OpmlPickerComponent } from './opml-picker.component';
import { OpmlPickerModule } from './opml-picker.module';

describe('BubbleComponent', () => {
  let component: OpmlPickerComponent;
  let fixture: ComponentFixture<OpmlPickerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [OpmlPickerModule],
    }).compileComponents();

    fixture = TestBed.createComponent(OpmlPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
