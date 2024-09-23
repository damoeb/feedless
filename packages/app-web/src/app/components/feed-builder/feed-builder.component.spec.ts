import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderComponent } from './feed-builder.component';
import {
  AppTestModule,
  mockDocuments,
  mockRepositories,
  mockScrape,
} from '../../app-test.module';
import { FeedBuilderModule } from './feed-builder.module';

describe('FeedBuilderComponent', () => {
  let component: FeedBuilderComponent;
  let fixture: ComponentFixture<FeedBuilderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedBuilderModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockScrape(apolloMockController);
            mockDocuments(apolloMockController);
            mockRepositories(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
