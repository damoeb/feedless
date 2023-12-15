import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WizardBucketComponent } from './wizard-bucket.component';
import { WizardModule } from '../wizard.module';
import { AppTestModule } from '../../../app-test.module';
import { FeedService } from '../../../services/feed.service';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { WizardHandler } from '../wizard-handler';
import { defaultWizardContext } from '../wizard/wizard.component';
import {
  GqlSearchBucketsOrFeedsQuery,
  GqlSearchBucketsOrFeedsQueryVariables,
  GqlSearchBucketsQuery,
  GqlSearchBucketsQueryVariables,
  SearchBuckets,
  SearchBucketsOrFeeds,
} from '../../../../generated/graphql';

xdescribe('WizardBucketComponent', () => {
  let component: WizardBucketComponent;
  let fixture: ComponentFixture<WizardBucketComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        WizardModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<
              GqlSearchBucketsOrFeedsQuery,
              GqlSearchBucketsOrFeedsQueryVariables
            >(SearchBucketsOrFeeds)
            .and.resolveOnce(async () => {
              return {
                data: {
                  bucketsOrNativeFeeds: [],
                },
              };
            });
          apolloMockController
            .mockQuery<GqlSearchBucketsQuery, GqlSearchBucketsQueryVariables>(
              SearchBuckets,
            )
            .and.resolveOnce(async () => {
              return {
                data: {
                  buckets: {
                    buckets: [],
                    pagination: {} as any,
                  },
                },
              };
            });
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WizardBucketComponent);
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
