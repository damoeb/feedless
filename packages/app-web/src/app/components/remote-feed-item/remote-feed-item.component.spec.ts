import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RemoteFeedItemComponent } from './remote-feed-item.component';
import { AppTestModule } from '../../app-test.module';
import { AppConfigService } from '../../services/app-config.service';

describe('RemoteFeedItemComponent', () => {
  let component: RemoteFeedItemComponent;
  let fixture: ComponentFixture<RemoteFeedItemComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [RemoteFeedItemComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.getAllAppConfigs = () => Promise.resolve([]);

    fixture = TestBed.createComponent(RemoteFeedItemComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('feedItem', {
      url: 'https://example.com',
      id: '1',
      publishedAt: 0,
      updatedAt: 0,
      createdAt: 0,
      title: '',
      text: '',
    });

    componentRef.setInput('feedItemIndex', 1);

    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
