import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { UntoldNotesMenuComponent } from './untold-notes-menu.component';

describe('UntoldNotesMenuComponent', () => {
  let component: UntoldNotesMenuComponent;
  let fixture: ComponentFixture<UntoldNotesMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [UntoldNotesMenuComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(UntoldNotesMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
