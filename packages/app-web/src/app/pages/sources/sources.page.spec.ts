import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SourcesPage } from './sources.page';
import { SourcesPageModule } from './sources.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('SourcesPage', () => {
  let component: SourcesPage;
  let fixture: ComponentFixture<SourcesPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        SourcesPageModule,
        // AppTestModule.withDefaults((apolloMockController) => {
        //   apolloMockController
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

    fixture = TestBed.createComponent(SourcesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
