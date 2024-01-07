import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedComponent } from './remote-feed.component';
import { RemoteFeedModule } from './remote-feed.module';
import { AppTestModule } from '../../app-test.module';
import { FeedService } from '../../services/feed.service';
import { ServerSettingsService } from '../../services/server-settings.service';
import { WizardHandler } from '../wizard/wizard-handler';
import { defaultWizardContext } from '../wizard/wizard/wizard.component';

describe('RemoteFeedComponent', () => {
  let component: RemoteFeedComponent;
  let fixture: ComponentFixture<RemoteFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RemoteFeedModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoteFeedComponent);
    component = fixture.componentInstance;
    const feedService = TestBed.inject(FeedService);
    const serverSettingsService = TestBed.inject(ServerSettingsService);

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
