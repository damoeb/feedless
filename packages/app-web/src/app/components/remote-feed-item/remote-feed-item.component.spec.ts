import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedItemComponent } from './remote-feed-item.component';
import { RemoteFeedItemModule } from './remote-feed-item.module';
import { AppTestModule } from '../../app-test.module';

describe('RemoteFeedItemComponent', () => {
  let component: RemoteFeedItemComponent;
  let fixture: ComponentFixture<RemoteFeedItemComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [RemoteFeedItemModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoteFeedItemComponent);
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
