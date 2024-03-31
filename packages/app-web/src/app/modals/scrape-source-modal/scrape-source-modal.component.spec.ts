import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ScrapeSourceModalComponent } from './scrape-source-modal.component';
import { ScrapeSourceModalModule } from './scrape-source-modal.module';
import { AppTestModule } from '../../app-test.module';
import { GqlListPluginsQuery, GqlListPluginsQueryVariables, ListPlugins } from '../../../generated/graphql';

describe('FeedBuilderModalComponent', () => {
  let component: ScrapeSourceModalComponent;
  let fixture: ComponentFixture<ScrapeSourceModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ScrapeSourceModalModule,
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<GqlListPluginsQuery, GqlListPluginsQueryVariables>(
              ListPlugins,
            )
            .and.resolveOnce(async () => {
              return {
                data: {
                  plugins: [],
                },
              };
            });
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ScrapeSourceModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
