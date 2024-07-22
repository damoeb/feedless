import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedBuilderPage } from './feed-builder.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { FeedBuilderPageModule } from './feed-builder.module';

describe('FeedBuilderPage', () => {
  let component: FeedBuilderPage;
  let fixture: ComponentFixture<FeedBuilderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedBuilderPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
