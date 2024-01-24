import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryPluginsPage } from './repository-plugins.page';
import { RepositoryPluginsPageModule } from './repository-plugins.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('SourcePage', () => {
  let component: RepositoryPluginsPage;
  let fixture: ComponentFixture<RepositoryPluginsPage>;

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

    fixture = TestBed.createComponent(RepositoryPluginsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
