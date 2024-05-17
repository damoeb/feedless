import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NativeFeedComponent } from './native-feed.component';
import { NativeFeedModule } from './native-feed.module';
import { AppTestModule } from '../../app-test.module';

describe('RemoteFeedComponent', () => {
  let component: NativeFeedComponent;
  let fixture: ComponentFixture<NativeFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NativeFeedModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NativeFeedComponent);
    component = fixture.componentInstance;

    // component.handler = new WizardHandler(
    //   defaultWizardContext,
    //   feedService,
    //   serverSettingsService,
    // );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
