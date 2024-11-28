import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderComponent } from './feed-builder.component';
import {
  AppTestModule,
  mockRecords,
  mockRepositories,
  mockScrape,
} from '../../app-test.module';

describe('FeedBuilderComponent', () => {
  let component: FeedBuilderComponent;
  let fixture: ComponentFixture<FeedBuilderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedBuilderComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockScrape(apolloMockController);
            mockRecords(apolloMockController);
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
