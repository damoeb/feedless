import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UntoldNotesProductPage } from './untold-notes-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('RssBuilderProductPage', () => {
  let component: UntoldNotesProductPage;
  let fixture: ComponentFixture<UntoldNotesProductPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        UntoldNotesProductPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockScrape(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UntoldNotesProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
