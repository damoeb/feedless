import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SourcePage } from './source.page';
import { SourcePageModule } from './source.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('SourcePage', () => {
  let component: SourcePage;
  let fixture: ComponentFixture<SourcePage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        SourcePageModule,
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
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SourcePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
