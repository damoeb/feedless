import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketPage } from './bucket.page';
import { BucketPageModule } from './bucket.module';
import { AppTestModule } from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import {
  BucketById,
  GqlBucketByIdQuery,
  GqlBucketByIdQueryVariables,
  GqlSearchBucketsOrFeedsQuery,
  GqlSearchBucketsOrFeedsQueryVariables,
  SearchBucketsOrFeeds,
} from '../../../../generated/graphql';
import { Bucket } from '../../../graphql/types';

describe('BucketPage', () => {
  let component: BucketPage;
  let fixture: ComponentFixture<BucketPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        BucketPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<GqlBucketByIdQuery, GqlBucketByIdQueryVariables>(
              BucketById,
            )
            .and.resolveOnce(async () => {
              return {
                data: {
                  bucket: {
                    title: '',
                    websiteUrl: '',
                  } as Bucket,
                },
              };
            })
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
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BucketPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
