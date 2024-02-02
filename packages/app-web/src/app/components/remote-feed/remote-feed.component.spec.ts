import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedComponent } from './remote-feed.component';
import { RemoteFeedModule } from './remote-feed.module';
import { AppTestModule } from '../../app-test.module';

describe('RemoteFeedComponent', () => {
  let component: RemoteFeedComponent;
  let fixture: ComponentFixture<RemoteFeedComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RemoteFeedModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoteFeedComponent);
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
