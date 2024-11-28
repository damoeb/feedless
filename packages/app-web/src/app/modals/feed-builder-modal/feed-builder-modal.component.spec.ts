import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import {
  GqlListPluginsQuery,
  GqlListPluginsQueryVariables,
  ListPlugins,
} from '../../../generated/graphql';

describe('FeedBuilderModalComponent', () => {
  let component: FeedBuilderModalComponent;
  let fixture: ComponentFixture<FeedBuilderModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedBuilderModalComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockRepositories(apolloMockController);
            apolloMockController
              .mockQuery<
                GqlListPluginsQuery,
                GqlListPluginsQueryVariables
              >(ListPlugins)
              .and.resolveOnce(async () => {
                return {
                  data: {
                    plugins: [],
                  },
                };
              });
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
