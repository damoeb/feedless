import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BucketsPage } from './buckets.page';
import { BucketsPageModule } from './buckets.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import {
  GqlSearchBucketsOrFeedsQuery,
  GqlSearchBucketsOrFeedsQueryVariables,
  SearchBucketsOrFeeds,
} from '../../../generated/graphql';

describe('BucketsPage', () => {
  let component: BucketsPage;
  let fixture: ComponentFixture<BucketsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        BucketsPageModule,
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
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BucketsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
