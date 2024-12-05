import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  AppTestModule,
  mockPlugins,
  mockRecords,
  mockRepository,
} from '../../app-test.module';
import { FeedBuilderComponent } from './feed-builder.component';

describe('FeedBuilderComponent', () => {
  let component: FeedBuilderComponent;
  let fixture: ComponentFixture<FeedBuilderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedBuilderComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockPlugins(apolloMockController);
            mockRecords(apolloMockController);
            mockRepository(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    // await mockServerSettings(
    //   TestBed.inject(ApolloMockController),
    //   TestBed.inject(ServerConfigService),
    //   TestBed.inject(ApolloClient),
    // );

    fixture = TestBed.createComponent(FeedBuilderComponent);
    component = fixture.componentInstance;
    // component.repository = { retention: {}, sources: [], plugins: [] } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
