import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NativeFeedComponent } from './native-feed.component';
import { AppTestModule } from '../../app-test.module';

describe('NativeFeedComponent', () => {
  let component: NativeFeedComponent;
  let fixture: ComponentFixture<NativeFeedComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [NativeFeedComponent, AppTestModule.withDefaults()],
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
