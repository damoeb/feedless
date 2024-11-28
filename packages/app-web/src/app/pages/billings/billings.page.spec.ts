import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { BillingsPage } from './billings.page';
import {
  AppTestModule,
  mockBillings,
  mockRepositories,
} from '../../app-test.module';

describe('BillingsPage', () => {
  let component: BillingsPage;
  let fixture: ComponentFixture<BillingsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BillingsPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockRepositories(apolloMockController);
            mockBillings(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BillingsPage);
    component = fixture.componentInstance;
    component.orders = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
