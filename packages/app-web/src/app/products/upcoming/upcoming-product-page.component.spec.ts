import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingProductPage } from './upcoming-product-page.component';
import { AppTestModule, mockDocuments, mockScrape } from '../../app-test.module';
import { UpcomingProductModule } from './upcoming-product.module';
import { RouterTestingModule } from '@angular/router/testing';
import { FindEvents, GqlFindEventsQuery, GqlFindEventsQueryVariables } from '../../../generated/graphql';

describe('UpcomingProductPage', () => {
  let component: UpcomingProductPage;
  let fixture: ComponentFixture<UpcomingProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        UpcomingProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
          mockDocuments(apolloMockController);
          apolloMockController
            .mockQuery<
              GqlFindEventsQuery,
              GqlFindEventsQueryVariables
            >(FindEvents)
            .and.resolveOnce(async () => {
            return {
              data: {
                webDocumentsFrequency: [],
              },
            };
          })
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UpcomingProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
