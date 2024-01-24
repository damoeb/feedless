import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryDeliveryPage } from './repository-delivery.page';
import { RepositoryPluginsPageModule } from './repository-delivery.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('SourcePage', () => {
  let component: RepositoryDeliveryPage;
  let fixture: ComponentFixture<RepositoryDeliveryPage>;

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

    fixture = TestBed.createComponent(RepositoryDeliveryPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
