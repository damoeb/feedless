import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { PcTrackerProductPage } from './pc-tracker-product.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { PcTrackerProductModule } from './pc-tracker-product.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('PcTrackerProductPage', () => {
  let component: PcTrackerProductPage;
  let fixture: ComponentFixture<PcTrackerProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        PcTrackerProductModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PcTrackerProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
