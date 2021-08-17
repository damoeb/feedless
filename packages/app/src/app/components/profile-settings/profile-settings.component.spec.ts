import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProfileSettingsComponent } from './profile-settings.component';
import { ProfileSettingsModule } from './profile-settings.module';

describe('SettingsComponent', () => {
  let component: ProfileSettingsComponent;
  let fixture: ComponentFixture<ProfileSettingsComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ProfileSettingsModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ProfileSettingsComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
