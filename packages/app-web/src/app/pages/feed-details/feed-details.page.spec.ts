import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedDetailsPage } from './feed-details.page';
import { AppTestModule, mockPlugins, mockRepository } from '../../app-test.module';
import { FeedDetailsPageModule } from './feed-details.module';

describe('FeedDetailsPage', () => {
  let component: FeedDetailsPage;
  let fixture: ComponentFixture<FeedDetailsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedDetailsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlugins(apolloMockController);
          mockRepository(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedDetailsPage);
    component = fixture.componentInstance;
    component.repository = {} as any;
    // component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
