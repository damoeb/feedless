import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UntoldNotesProductPage } from './untold-notes-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { UntoldNotesProductModule } from './untold-notes-product.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('RssBuilderProductPage', () => {
  let component: UntoldNotesProductPage;
  let fixture: ComponentFixture<UntoldNotesProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        UntoldNotesProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UntoldNotesProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
