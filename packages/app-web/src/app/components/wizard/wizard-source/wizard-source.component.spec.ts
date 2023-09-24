import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardSourceComponent } from './wizard-source.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import { defaultWizardContext } from '../wizard/wizard.component';

describe('WizardSourceComponent', () => {
  let component: WizardSourceComponent;
  let fixture: ComponentFixture<WizardSourceComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WizardModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardSourceComponent);
    component = fixture.componentInstance;
    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);

    component.handler = new WizardHandler(
      defaultWizardContext,
      feedService,
      serverSettingsService,
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
