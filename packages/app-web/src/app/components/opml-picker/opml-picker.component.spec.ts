import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { OpmlPickerComponent } from './opml-picker.component';
import { OpmlPickerModule } from './opml-picker.module';
import { AppTestModule } from '../../app-test.module';

describe('OpmlPickerComponent', () => {
  let component: OpmlPickerComponent;
  let fixture: ComponentFixture<OpmlPickerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [OpmlPickerModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(OpmlPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
