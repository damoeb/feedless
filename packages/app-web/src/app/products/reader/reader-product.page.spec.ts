import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReaderProductPage } from './reader-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ReaderProductPage', () => {
  let component: ReaderProductPage;
  let fixture: ComponentFixture<ReaderProductPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReaderProductPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => mockScrape(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
