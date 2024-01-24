import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositorySettingsPage } from './repository-settings-page.component';
import { RepositoryPluginsPageModule } from './repository-settings.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('SourcePage', () => {
  let component: RepositorySettingsPage;
  let fixture: ComponentFixture<RepositorySettingsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryPluginsPageModule,
        // AppTestModule.withDefaults((apolloMockController) => {
        //   apolloMockController
        //     .mockQuery<GqlBucketByIdQuery, GqlBucketByIdQueryVariables>(
        //       BucketById,
        //     )
        //     .and.resolveOnce(async () => {
        //       return {
        //         data: {
        //           bucket: {
        //             title: '',
        //             websiteUrl: '',
        //           } as Bucket,
        //         },
        //       };
        //     })
        //     .mockQuery<
        //       GqlSearchBucketsOrFeedsQuery,
        //       GqlSearchBucketsOrFeedsQueryVariables
        //     >(SearchBucketsOrFeeds)
        //     .and.resolveOnce(async () => {
        //       return {
        //         data: {
        //           bucketsOrNativeFeeds: [],
        //         },
        //       };
        //     });
        // }),
        RouterTestingModule.withRoutes([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RepositorySettingsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
