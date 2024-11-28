import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NotificationsButtonComponent } from './notifications-button.component';
import { NotificationsButtonModule } from './notifications-button.module';
import { AppTestModule } from '../../app-test.module';

describe('DarkModeButtonComponent', () => {
  let component: NotificationsButtonComponent;
  let fixture: ComponentFixture<NotificationsButtonComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsButtonComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationsButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
