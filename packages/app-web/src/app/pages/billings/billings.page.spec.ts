import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { BillingsPage } from './billings.page';
import { AppTestModule, mockBillings, mockRepositories } from '../../app-test.module';
import { BillingsPageModule } from './billings.module';

describe('BillingsPage', () => {
  let component: BillingsPage;
  let fixture: ComponentFixture<BillingsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        BillingsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
          mockBillings(apolloMockController);
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BillingsPage);
    component = fixture.componentInstance;
    component.orders = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
