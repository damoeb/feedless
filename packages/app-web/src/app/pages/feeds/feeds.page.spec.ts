import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedsPage } from './feeds.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';

describe('FeedsPage', () => {
  let component: FeedsPage;
  let fixture: ComponentFixture<FeedsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedsPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedsPage);
    component = fixture.componentInstance;
    component.repositories = [];
    component.documents = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
