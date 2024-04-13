import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UntoldNotesMenuComponent } from './untold-notes-menu.component';
import { UntoldNotesProductModule } from '../untold-notes-product.module';
import { AppTestModule } from '../../../app-test.module';

describe('UntoldNotesMenuComponent', () => {
  let component: UntoldNotesMenuComponent;
  let fixture: ComponentFixture<UntoldNotesMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [UntoldNotesProductModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(UntoldNotesMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
