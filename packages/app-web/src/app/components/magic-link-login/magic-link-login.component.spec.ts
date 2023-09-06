import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MagicLinkLoginComponent } from './magic-link-login.component';
import { AppTestModule } from '../../app-test.module';
import { MagicLinkLoginModule } from './magic-link-login.module';

describe('MagicLinkLoginComponent', () => {
  let component: MagicLinkLoginComponent;
  let fixture: ComponentFixture<MagicLinkLoginComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [MagicLinkLoginModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(MagicLinkLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
