import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReaderProductPage } from './reader-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { ReaderProductModule } from './reader-product.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ReaderProductPage', () => {
  let component: ReaderProductPage;
  let fixture: ComponentFixture<ReaderProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReaderProductModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockScrape(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
