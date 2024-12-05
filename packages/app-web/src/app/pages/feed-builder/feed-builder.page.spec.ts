import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedBuilderPage } from './feed-builder.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';

describe('FeedBuilderPage', () => {
  let component: FeedBuilderPage;
  let fixture: ComponentFixture<FeedBuilderPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedBuilderPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
