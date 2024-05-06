import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeedsPage } from './feeds.page';
import { AppTestModule, mockRepositories } from '../../../app-test.module';
import { FeedsPageModule } from './feeds.module';

describe('FeedsPage', () => {
  let component: FeedsPage;
  let fixture: ComponentFixture<FeedsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FeedsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedsPage);
    component = fixture.componentInstance;
    component.repositories = [];
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
